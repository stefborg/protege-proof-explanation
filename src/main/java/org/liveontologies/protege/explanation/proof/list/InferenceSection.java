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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.liveontologies.owlapi.proof.util.ProofNode;
import org.liveontologies.owlapi.proof.util.ProofStep;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Represents an item in {@link ProofFrame} list that corresponds to the given
 * {@link ProofStep}.
 * 
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 *
 */
class InferenceSection
		implements OWLFrameSection<ProofRoot, OWLAxiom, OWLAxiom> {

	/**
	 * The main section holding the proof
	 */
	private final ProofFrame frame_;

	/**
	 * The section for the conclusion of {@link #inference_}
	 */
	private final ConclusionSection parentSection_;

	/**
	 * The inference rendered by this section
	 */
	private final ProofStep<OWLAxiom> inference_;

	private final InferenceRow inferenceRow_;

	InferenceSection(ProofFrame frame, ConclusionSection parentSection,
			ProofStep<OWLAxiom> inference) {
		this.frame_ = frame;
		this.parentSection_ = parentSection;
		this.inference_ = inference;
		this.inferenceRow_ = new InferenceRow(this);
	}

	InferenceSection(ProofFrame frame, ProofStep<OWLAxiom> inference) {
		this(frame, null, inference);
	}

	ConclusionSection getParentSection() {
		return parentSection_;
	}

	ProofStep<OWLAxiom> getInference() {
		return inference_;
	}

	@Override
	public String getName() {
		return getLabel();
	}

	InferenceRow getRow() {
		return inferenceRow_;
	}

	@Override
	public boolean checkEditorResults(OWLObjectEditor<OWLAxiom> editor) {
		return true;
	}

	@Override
	public boolean dropObjects(List<OWLObject> objects) {
		return false;
	}

	@Override
	public void dispose() {
		// no-op
	}

	@Override
	public ProofFrame getFrame() {
		return frame_;
	}

	@Override
	public void setRootObject(ProofRoot rootObject) {
		// no-op
	}

	@Override
	public String getLabel() {
		return inference_.getName();
	}

	@Override
	public String getRowLabel(
			OWLFrameSectionRow<ProofRoot, OWLAxiom, OWLAxiom> row) {
		return null;
	}

	@Override
	public ProofRoot getRootObject() {
		return frame_.getRootObject();
	}

	@Override
	public List<OWLFrameSectionRow<ProofRoot, OWLAxiom, OWLAxiom>> getRows() {
		return Collections
				.<OWLFrameSectionRow<ProofRoot, OWLAxiom, OWLAxiom>> unmodifiableList(
						inferenceRow_.getChildren());
	}

	@Override
	public List<OWLAxiom> getAxioms() {
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();

		for (ProofNode<OWLAxiom> premise : inference_.getPremises()) {
			axioms.add(premise.getMember());
		}

		return axioms;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int getRowIndex(OWLFrameSectionRow row) {
		return inferenceRow_.getChildren().indexOf(row);
	}

	@Override
	public OWLObjectEditor<OWLAxiom> getEditor() {
		return frame_.getAxiomEditor();
	}

	@Override
	public Comparator<OWLFrameSectionRow<ProofRoot, OWLAxiom, OWLAxiom>> getRowComparator() {
		return null;
	}

	@Override
	public boolean canAdd() {
		return false;
	}

	@Override
	public boolean canAcceptDrop(List<OWLObject> objects) {
		return false;
	}

	@Override
	public String toString() {
		return inference_.toString();
	}

}
