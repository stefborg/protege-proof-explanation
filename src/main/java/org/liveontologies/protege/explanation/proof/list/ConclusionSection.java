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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.liveontologies.protege.explanation.proof.OWLRenderer;
import org.liveontologies.protege.explanation.proof.ProofManager;
import org.liveontologies.protege.explanation.proof.editing.OWLAxiomChecker;
import org.liveontologies.puli.ProofNode;
import org.liveontologies.puli.ProofStep;
import org.protege.editor.core.Disposable;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditorHandler;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

/**
 * Represents an item in {@link ProofFrame} list that corresponds to the
 * {@link OWLAxiom} of a given {@link ProofNode} and the corresponding row in
 * the {@link ProofFrameList}
 * 
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 * 
 * @see ProofNode#getMember()
 */
class ConclusionSection extends AbstractProofFrameListRow<InferenceRow>
		implements ProofFrameListRow,
		OWLFrameSectionRow<ProofRoot, OWLAxiom, OWLAxiom>, Disposable,
		OWLObjectEditorHandler<OWLAxiom> {

	/**
	 * The rendering of the inference to which this section belongs
	 */
	private final InferenceSection parentSection_;

	/**
	 * The conclusion rendered by this section
	 */
	private final ProofNode<OWLAxiom> conclusion_;

	/**
	 * {@code true} if the axiom of this section can be edited
	 */
	private final boolean editable_;

	/**
	 * {@code true} if the axiom in this section is inferred
	 */
	private final boolean inferred_;

	/**
	 * {@code true} if the inferences for the conclusion should be shown
	 */
	private boolean expanded_ = false;

	/**
	 * the maximal number of inferences displayed for this conclusion if
	 * expanded
	 */
	private int visibleChildLimit_;

	/**
	 * original (annotated) axioms corresponding to this conclusion
	 */
	private final List<? extends OWLAxiom> originalAxioms_;

	/**
	 * the respected ontologies to which the {@link #originalAxioms_} belongs
	 */
	private final List<? extends OWLOntology> ontologies_;

	ConclusionSection(InferenceSection parentSection,
			ProofNode<OWLAxiom> conclusion) {
		this.parentSection_ = parentSection;
		this.conclusion_ = conclusion;
		ProofManager wbManager = parentSection.getFrame().getWorkbenchManager();
		OWLAxiom key = conclusion.getMember().getAxiomWithoutAnnotations();
		this.originalAxioms_ = wbManager.getMatchingAxioms(key);
		this.ontologies_ = parentSection.getFrame().getWorkbenchManager()
				.getHomeOntologies(key);
		this.editable_ = isAsserted()
				&& getAxiomChecker().isParsable(getAxiom());
		this.inferred_ = !conclusion.getInferences().isEmpty();
		if (!inferred_) {
			expanded_ = true;
		}
		this.visibleChildLimit_ = parentSection.getFrame()
				.getDisplayedInferencesPerConclusionLimit();
	}

	public ProofNode<OWLAxiom> getConclusion() {
		return conclusion_;
	}

	@Override
	public InferenceRow getParent() {
		return parentSection_.getRow();
	}

	@Override
	public boolean isExpanded() {
		return expanded_;
	}

	@Override
	public boolean isExpandable() {
		return isInferred();
	}

	@Override
	public void toggleExpandState() {
		if (!isExpandable()) {
			return;
		}
		// else
		expanded_ ^= true;
	}

	boolean isAsserted() {
		return !ontologies_.isEmpty();
	}

	OWLRenderer getRenderer() {
		return parentSection_.getFrame().getRenderer();
	}

	OWLAxiomChecker getAxiomChecker() {
		return parentSection_.getFrame().getAxiomChecker();
	}

	/**
	 * @return {@code true} if this section has children that are not yet
	 *         displayed
	 */
	boolean hasMoreChildren() {
		return super.getChildren().size() > visibleChildLimit_;
	}

	void loadMoreChildren() {
		visibleChildLimit_ += parentSection_.getFrame()
				.getDisplayedInferencesPerConclusionLimit();
	}

	@Override
	public synchronized List<InferenceRow> getChildren() {
		List<InferenceRow> allChildren = super.getChildren();
		if (visibleChildLimit_ >= allChildren.size()) {
			return allChildren;
		}
		// else
		return super.getChildren().subList(0, visibleChildLimit_);
	}

	@Override
	public List<? extends OWLObject> getManipulatableObjects() {
		return Arrays.asList(getAxiom());
	}

	@Override
	public OWLFrameSection<ProofRoot, OWLAxiom, OWLAxiom> getFrameSection() {
		return parentSection_;
	}

	@Override
	public boolean isEditable() {
		return editable_;
	}

	@Override
	public boolean isDeleteable() {
		return isAsserted();
	}

	@Override
	public boolean isInferred() {
		return inferred_;
	}

	@Override
	public OWLObjectEditor<OWLAxiom> getEditor() {
		OWLObjectEditor<OWLAxiom> editor = parentSection_.getEditor();
		editor.setEditedObject(getAxiom());
		editor.setHandler(this);
		return editor;
	}

	@Override
	public boolean checkEditorResults(OWLObjectEditor<OWLAxiom> editor) {
		return true;
	}

	@Override
	public boolean canAcceptDrop(List<OWLObject> objects) {
		return false;
	}

	@Override
	public boolean dropObjects(List<OWLObject> objects) {
		return false;
	}

	@Override
	public String getTooltip() {
		StringBuilder sb = new StringBuilder();
		boolean delim = false;
		if (isInferred()) {
			sb.append("Inferred");
			delim = true;
		}
		if (isAsserted()) {
			if (delim) {
				sb.append(" and ");
			}
			sb.append("Asserted in: ");
			sb.append(getRenderer().render(getOntology()));
		}
		return sb.toString();
	}

	@Override
	public void handleEdit() {
		// no-op
	}

	@Override
	public boolean handleDelete() {
		return false;
	}

	@Override
	public ProofRoot getRoot() {
		return parentSection_.getRootObject();
	}

	@Override
	public OWLAxiom getAxiom() {
		if (originalAxioms_.isEmpty()) {
			// must be inferred
			return conclusion_.getMember();
		}
		// else
		return originalAxioms_.get(0);
	}

	@Override
	public Object getUserObject() {
		return null;
	}

	@Override
	public void setUserObject(Object object) {
		// no-op
	}

	@Override
	public OWLOntology getOntology() {
		if (ontologies_.isEmpty()) {
			return null;
		}
		// else
		return ontologies_.get(0);
	}

	@Override
	public OWLOntologyManager getOWLOntologyManager() {
		return getOWLModelManager().getOWLOntologyManager();
	}

	@Override
	public List<? extends OWLOntologyChange> getDeletionChanges() {
		return Collections
				.singletonList(new RemoveAxiom(getOntology(), getAxiom()));
	}

	OWLModelManager getOWLModelManager() {
		return parentSection_.getFrame().getEditorKit().getOWLModelManager();
	}

	public String getRendering() {
		OWLObject ax = getAxiom();
		return getRenderer().render(ax);
	}

	@Override
	public String toString() {
		return getRendering();
	}

	@Override
	public void handleEditingFinished(Set<OWLAxiom> editedObjects) {
		OWLAxiom oldAxiom = getAxiom();
		Set<OWLAnnotation> axiomAnnotations = oldAxiom.getAnnotations();
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		OWLOntology ontology = getOntology();
		changes.add(new RemoveAxiom(ontology, oldAxiom));
		for (OWLAxiom newAxiom : editedObjects) {
			if (!axiomAnnotations.isEmpty()) {
				newAxiom = newAxiom.getAnnotatedAxiom(axiomAnnotations);
			}
			changes.add(new AddAxiom(ontology, newAxiom));
		}
		getOWLModelManager().applyChanges(changes);
	}

	@Override
	List<InferenceRow> computeChildSections() {
		Collection<? extends ProofStep<OWLAxiom>> inferences = conclusion_
				.getInferences();
		List<InferenceRow> result = new ArrayList<InferenceRow>(
				inferences.size());
		for (ProofStep<OWLAxiom> inf : inferences) {
			result.add(
					new InferenceSection(parentSection_.getFrame(), this, inf)
							.getRow());
		}
		// try to keep the order of inferences fixed
		Collections.sort(result);
		return result;
	}

	@Override
	void copySettingsFrom(AbstractProofFrameListRow<?> previous) {
		visibleChildLimit_ = ((ConclusionSection) previous).visibleChildLimit_;
		super.copySettingsFrom(previous);
	}

	@Override
	public boolean matches(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (o instanceof ConclusionSection) {
			ConclusionSection other = (ConclusionSection) o;
			boolean result = conclusion_.getMember()
					.equals(other.conclusion_.getMember());
			return result;
		}
		// else
		return false;
	}

	@Override
	public <O> O accept(Visitor<O> visitor) {
		return visitor.visit(this);
	}

}
