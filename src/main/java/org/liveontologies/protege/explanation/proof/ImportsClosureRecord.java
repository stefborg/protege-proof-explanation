package org.liveontologies.protege.explanation.proof;

/*-
 * #%L
 * Protege Proof-Based Explanation
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Represents the import closure of an ontology.
 * 
 * @author Yevgeny Kazakov
 */
public class ImportsClosureRecord {

	// logger for this class
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(ImportsClosureRecord.class);

	/**
	 * the ontology which import closure is maintained by this object
	 */
	private final OWLOntology rootOntology_;

	/**
	 * {@link #rootOntology_} plus the ontologies (indirectly) imported by
	 * {@link #rootOntology_}
	 */
	private Set<OWLOntology> importsClosure_;

	/**
	 * a multimap from axioms without annotations to corresponding axioms
	 * (possibly with annotations) occurring in the import closure
	 */
	private final ListMultimap<OWLAxiom, OWLAxiom> originalAxioms_ = ArrayListMultimap
			.create();

	/**
	 * a multimap from axioms without annotations to corresponding ontologies
	 * containing such axioms possibly with annotations
	 */
	private final ListMultimap<OWLAxiom, OWLOntology> axiomOccurrences_ = ArrayListMultimap
			.create();

	private final List<ChangeListener> listeners_ = new ArrayList<ChangeListener>(
			2);

	/**
	 * {@code true} if {@link #axiomOccurrences_} is up to date w.r.t.
	 * {@link #rootOntology_}
	 */
	private boolean axiomsUpToDate_ = false;

	public ImportsClosureRecord(OWLOntology rootOntology) {
		this.rootOntology_ = rootOntology;
	}

	/**
	 * @return the ontology which import closure is maintained by this object
	 */
	public OWLOntology getRootOntology() {
		return rootOntology_;
	}

	/**
	 * @param axiom
	 *            axiom to search for
	 * @return the list of ontologies in the import closure which contain the
	 *         given axiom (possibly with different annotations); the matching
	 *         axiom can be found in the corresponding position of
	 *         {@link #getMatchingAxioms(OWLAxiom)}
	 */
	public synchronized List<OWLOntology> getHomeOntologies(OWLAxiom axiom) {
		updateIfNeeded();
		return axiomOccurrences_.get(axiom.getAxiomWithoutAnnotations());
	}

	/**
	 * @param axiom
	 *            axioms to search for
	 * @return the list of axioms occurring in the import closure that are equal
	 *         to the given axiom modulo annotations; the ontologies in which
	 *         these axioms occur can be found in the corresponding positions of
	 *         {@link #getHomeOntologies(OWLAxiom)}
	 */
	public synchronized List<OWLAxiom> getMatchingAxioms(OWLAxiom axiom) {
		updateIfNeeded();
		return originalAxioms_.get(axiom.getAxiomWithoutAnnotations());
	}

	/**
	 * @return all axioms in the import closure with removed annotations
	 */
	public synchronized Set<OWLAxiom> getStatedAxiomsWithoutAnnotations() {
		updateIfNeeded();
		return axiomOccurrences_.keySet();
	}

	synchronized Set<? extends OWLOntology> getImportsClosure() {
		return importsClosure_;
	}

	boolean isUpToDate() {
		return axiomsUpToDate_;
	}

	public synchronized void addListener(ChangeListener listener) {
		listeners_.add(listener);
	}

	public synchronized void removeListener(ChangeListener listener) {
		listeners_.remove(listener);
	}

	synchronized boolean updateIfNeeded() {
		if (axiomsUpToDate_) {
			return false;
		}
		// else
		importsClosure_ = rootOntology_.getImportsClosure();
		for (OWLOntology ontology : importsClosure_) {
			for (OWLAxiom axiom : ontology.getAxioms()) {
				OWLAxiom key = axiom.getAxiomWithoutAnnotations();
				originalAxioms_.put(key, axiom);
				axiomOccurrences_.put(key, ontology);
			}
		}
		axiomsUpToDate_ = true;
		return true;
	}

	synchronized boolean invalidate() {
		if (!axiomsUpToDate_) {
			return false;
		}
		// else
		axiomsUpToDate_ = false;
		importsClosure_ = null;
		originalAxioms_.clear();
		axiomOccurrences_.clear();
		int i = 0;
		try {
			for (; i < listeners_.size(); i++) {
				listeners_.get(i).statedAxiomsChanged();
			}
		} catch (Throwable e) {
			LOGGER_.warn("Remove the listener due to an exception", e);
			removeListener(listeners_.get(i));
		}
		return true;
	}

	void invalidateLater() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	public interface ChangeListener {

		void statedAxiomsChanged();

	}

}
