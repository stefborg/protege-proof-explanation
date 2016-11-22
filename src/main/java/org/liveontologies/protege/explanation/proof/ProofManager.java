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
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.liveontologies.owlapi.proof.OWLProofNode;
import org.liveontologies.owlapi.proof.util.LeafProofNode;
import org.liveontologies.owlapi.proof.util.ProofNode;
import org.liveontologies.owlapi.proof.util.ProofNodes;
import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object to manage the proof for a particular entailed {@link OWLAxiom}
 * 
 * @author Yevgeny Kazakov
 */
public class ProofManager implements ImportsClosureRecord.ChangeListener,
		OWLProofNode.ChangeListener, Disposable {

	// logger for this class
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(ProofManager.class);

	/**
	 * proof services
	 */
	private final ProofServiceManager proofServiceMan_;

	/**
	 * the entailement for which the proofs are managed
	 */
	private final OWLAxiom entailment_;

	/**
	 * the import closure for the ontology which entails {@link #entailment_}
	 */
	private final ImportsClosureRecord importsClosureRec_;

	/**
	 * the proof node for {@link #entailment_} returned by the proof service;
	 * all inferences can be accessed from that
	 */
	private OWLProofNode proofRoot_ = null;

	/**
	 * the result of applying the transformation (e.g., elimination of cycles)
	 * to {@link #proofRoot_}; its inferences will be actually displayed
	 */
	private ProofNode<OWLAxiom> processedProofRoot_ = null;

	/**
	 * {@code true} if {@link #processedProofRoot_} reflects the one maintained by
	 * {@link #proofService_}
	 */
	private boolean proofRootUpToDate_ = false;

	/**
	 * the listeners to be notified when {@link #processedProofRoot_} is updated
	 */
	private final List<ChangeListener> listeners_ = new ArrayList<ChangeListener>();

	ProofManager(ProofServiceManager proofServiceMan,
			ImportsClosureRecord importsClosureRec, OWLAxiom entailment) {
		this.proofServiceMan_ = proofServiceMan;
		this.importsClosureRec_ = importsClosureRec;
		this.entailment_ = entailment;
		importsClosureRec_.addListener(this);
	}

	/**
	 * @return the axiom for which the proofs are managed
	 */
	public OWLAxiom getEntailment() {
		return entailment_;
	}

	/**
	 * @return the ontology which determines which axioms should be considered
	 *         stated
	 */
	public OWLOntology getOntology() {
		return importsClosureRec_.getRootOntology();
	}

	/**
	 * @return the object that keeps track of proof services and stated axioms
	 *         for ontologies
	 */
	public ProofServiceManager getProofServiceManager() {
		return proofServiceMan_;
	}

	public OWLEditorKit getOWLEditorKit() {
		return proofServiceMan_.getOWLEditorKit();
	}

	/**
	 * Sets the object from which the proofs for entailment are obtained
	 * 
	 * @param proofService
	 * 
	 * @see #getEntailment()
	 */
	public synchronized void setProofService(ProofService proofService) {
		if (proofRoot_ != null) {
			proofRoot_.removeListener(this);
		}
		proofRoot_ = proofService.getProof(entailment_);
		proofRoot_.addListener(this);
		invalidateProofRoot();
	}

	/**
	 * @return the root of the proof for the entailment obtained from the
	 *         current proof service
	 * 
	 * @see #getEntailment()
	 * @see #setProofService(ProofService)
	 */
	public synchronized ProofNode<OWLAxiom> getProofRoot() {
		if (!proofRootUpToDate_) {
			if (proofRoot_ == null) {
				processedProofRoot_ = new LeafProofNode<OWLAxiom>(entailment_);
			} else {
				processedProofRoot_ = ProofNodes.eliminateNotDerivableAndCycles(
						proofRoot_,
						importsClosureRec_.getStatedAxiomsWithoutAnnotations());
			}
			proofRootUpToDate_ = true;
		}
		return processedProofRoot_;
	}

	public synchronized void addListener(ChangeListener listener) {
		listeners_.add(listener);
	}

	public synchronized void removeListener(ChangeListener listener) {
		listeners_.remove(listener);
	}

	/**
	 * @param key
	 * @return the list of ontologies in the import closure which contain the
	 *         given axiom (possibly with different annotations); the matching
	 *         axiom can be found in the corresponding position of
	 *         {@link #getMatchingAxioms(OWLAxiom)}
	 */
	public List<? extends OWLOntology> getHomeOntologies(OWLAxiom key) {
		return importsClosureRec_.getHomeOntologies(key);
	}

	/**
	 * @param key
	 * @return the list of axioms occurring in the import closure that are equal
	 *         to the given axiom modulo annotations; the ontologies in which
	 *         these axioms occur can be found in the corresponding positions of
	 *         {@link #getHomeOntologies(OWLAxiom)}
	 */
	public List<? extends OWLAxiom> getMatchingAxioms(OWLAxiom key) {
		return importsClosureRec_.getMatchingAxioms(key);
	}

	/**
	 * @return the proof services that can provide proofs for the managed
	 *         entailment
	 * 
	 * @see #getEntailment()
	 */
	public Collection<ProofService> getProofServices() {
		List<ProofService> result = new ArrayList<ProofService>();
		for (ProofService service : proofServiceMan_.getProofServices()) {
			if (service.hasProof(entailment_)) {
				result.add(service);
			}
		}
		return result;
	}

	@Override
	public void dispose() {
		importsClosureRec_.removeListener(this);
		if (proofRoot_ != null) {
			proofRoot_.removeListener(this);
		}
	}

	@Override
	public void statedAxiomsChanged() {
		invalidateProofRootLater();
	}

	@Override
	public void nodeChanged() {
		invalidateProofRootLater();
	}

	private void invalidateProofRootLater() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				invalidateProofRoot();
			}
		});
	}

	private synchronized boolean invalidateProofRoot() {
		if (!proofRootUpToDate_) {
			return false;
		}
		// else
		proofRootUpToDate_ = false;
		processedProofRoot_ = null;
		int i = 0;
		try {
			for (; i < listeners_.size(); i++) {
				listeners_.get(i).proofRootChanged();
			}
		} catch (Throwable e) {
			LOGGER_.warn("Remove the listener due to an exception", e);
			removeListener(listeners_.get(i));
		}
		return true;
	}

	public interface ChangeListener {
		/**
		 * fired when a subsequent call to {@link ProofManager#getProofRoot()}
		 * would return a different result
		 */
		void proofRootChanged();
	}

}
