package org.liveontologies.protege.explanation.proof.list;

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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.liveontologies.owlapi.proof.util.Inference;
import org.liveontologies.owlapi.proof.util.ProofNode;
import org.liveontologies.owlapi.proof.util.ProofStep;
import org.liveontologies.protege.explanation.proof.OWLRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;

public class ProofRoot implements ProofStep<OWLAxiom>, ProofNode<OWLAxiom> {

	static final String SOME_PROOF = "Proof tree for entailment";

	static final String NO_PROOF = "No proof for the entailment found.";

	private final OWLAxiom member_;

	private final List<? extends ProofNode<OWLAxiom>> premises_;

	private final OWLRenderer renderer_;

	ProofRoot(final OWLAxiom member,
			List<? extends ProofNode<OWLAxiom>> premises,
			OWLRenderer renderer) {
		this.member_ = member;
		this.premises_ = premises;
		this.renderer_ = renderer;
	}

	@Override
	public String getName() {
		return premises_.isEmpty() ? NO_PROOF : SOME_PROOF;
	}

	@Override
	public ProofNode<OWLAxiom> getConclusion() {
		return this;
	}

	@Override
	public List<? extends ProofNode<OWLAxiom>> getPremises() {
		return premises_;
	}

	@Override
	public OWLAxiom getMember() {
		return member_;
	}

	@Override
	public Collection<? extends ProofStep<OWLAxiom>> getInferences() {
		return Collections.singleton(this);
	}

	@Override
	public String toString() {
		return "Proof for " + renderer_.render(getMember());
	}

	@Override
	public Inference<OWLAxiom> getExample() {
		return null;
	}

}
