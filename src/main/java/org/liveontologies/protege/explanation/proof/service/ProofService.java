package org.liveontologies.protege.explanation.proof.service;

import org.liveontologies.proof.util.DynamicInferenceSet;
import org.liveontologies.proof.util.InferenceExampleProvider;

/*
 * #%L
 * Proof-Based Explanations
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
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

import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;

/**
 * A skeleton for a plugin that can provide proofs for OWL axioms
 * 
 * @author Yevgeny Kazakov
 *
 */
public abstract class ProofService implements ProtegePluginInstance {

	private OWLEditorKit kit_;
	private String pluginId_;
	private String name_;

	public ProofService setup(OWLEditorKit kit, String pluginId, String name) {
		this.kit_ = kit;
		this.pluginId_ = pluginId;
		this.name_ = name;
		return this;
	}

	public OWLEditorKit getEditorKit() {
		return kit_;
	}

	public String getPluginId() {
		return pluginId_;
	}

	public String getName() {
		return name_;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @param entailment
	 * @return {@code true} if this service can provide a proof for the given
	 *         entailment; the subsequent call of {@link #getProof(OWLAxiom)}
	 *         should return such a proof
	 */
	public abstract boolean hasProof(OWLAxiom entailment);

	/**
	 * @param entailment
	 * @return the inferences using which the entailment can be derived from the
	 *         axioms in the ontology
	 * @throws UnsupportedEntailmentTypeException
	 *             if checking entailment of the given axiom is not supported
	 */
	public abstract DynamicInferenceSet<OWLAxiom> getProof(OWLAxiom entailment)
			throws UnsupportedEntailmentTypeException;

	public abstract InferenceExampleProvider<OWLAxiom> getExampleProvider();
	
	@Override
	public abstract void dispose();

}
