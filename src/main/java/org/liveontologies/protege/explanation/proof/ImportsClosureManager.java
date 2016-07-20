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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Keeps track of the import closures for relevant ontologies
 * 
 * @author Yevgeny Kazakov
 *
 */
public class ImportsClosureManager
		implements OWLOntologyChangeListener, Disposable {

	private static final String KEY_ = "org.liveontologies.protege.explanation.proof.imports";

	private final OWLOntologyManager ontologyManager_;

	private final Map<OWLOntology, ImportsClosureRecord> ontologyClosureManagers_ = new HashMap<OWLOntology, ImportsClosureRecord>();

	private ImportsClosureManager(OWLOntologyManager ontologyManager) {
		this.ontologyManager_ = ontologyManager;
		ontologyManager.addOntologyChangeListener(this);
	}

	public static synchronized ImportsClosureManager get(OWLEditorKit editorKit)
			throws Exception {
		// reuse one instance
		ImportsClosureManager m = editorKit.getModelManager().get(KEY_);
		if (m == null) {
			m = new ImportsClosureManager(
					editorKit.getModelManager().getOWLOntologyManager());
			editorKit.put(KEY_, m);
		}
		return m;
	}

	synchronized ImportsClosureRecord getImportsClosure(OWLOntology ontology) {
		ImportsClosureRecord result = ontologyClosureManagers_.get(ontology);
		if (result == null) {
			result = new ImportsClosureRecord(ontology);
			ontologyClosureManagers_.put(ontology, result);
		}
		return result;
	}

	@Override
	public void dispose() {
		ontologyManager_.removeOntologyChangeListener(this);
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {
		for (ImportsClosureRecord record : ontologyClosureManagers_.values()) {
			if (!record.isUpToDate()) {
				continue;
			}
			for (OWLOntologyChange change : changes) {
				OWLOntology changedOntology = change.getOntology();
				// else
				if (record.getImportsClosure().contains(changedOntology)) {
					// TODO: optimize by applying changes directly
					record.invalidate();
					return;
				}
			}
		}
	}

}
