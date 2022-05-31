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

import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.explanation.ExplanationResult;
import org.protege.editor.owl.ui.explanation.ExplanationService;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * An {@link ExplanationService} for displaying proof-based explanations. This
 * is the main class required by an explanation plugin.
 * 
 * @author Yevgeny Kazakov
 *
 */
public class ProofBasedExplanationService extends ExplanationService {

	private ProofServiceManager proofServiceMan_;

	private ImportsClosureManager importsClosureMan_;

	@Override
	public void initialise() throws Exception {
		OWLEditorKit kit = getOWLEditorKit();
		proofServiceMan_ = ProofServiceManager.get(kit);
		importsClosureMan_ = ImportsClosureManager.get(kit);
		KeyEventManager.initialise(kit);
	}

	@Override
	public void dispose() {
		// managers should dispose automatically by OWLEditorKit
	}

	@Override
	public boolean hasExplanation(OWLAxiom axiom) {
		for (ProofService service : proofServiceMan_.getEnabledProofServices()) {
			if (service.hasProof(axiom)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ExplanationResult explain(OWLAxiom entailment) {
		return new ProofBasedExplanationResult(proofServiceMan_,
				importsClosureMan_, entailment);
	}

}
