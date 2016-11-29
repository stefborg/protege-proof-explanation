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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.liveontologies.proof.util.Inference;
import org.liveontologies.proof.util.ProofNode;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * A row in a {@link ProofFrameList} that displays information about the
 * inferences. This class is used for rendering of the row in
 * {@link ProofFrameListRenderer} since it is not possible to change the
 * rendering of a {@link OWLFrameSection}.
 * 
 * @author Yevgeny Kazakov
 */
class InferenceRow extends AbstractProofFrameListRow<ConclusionSection>
		implements Comparable<InferenceRow> {

	/**
	 * The {@link InferenceSection} coupled with this {@link InferenceRow}
	 */
	private final InferenceSection section_;

	InferenceRow(InferenceSection section) {
		this.section_ = section;
	}

	/**
	 * @return the {@link InferenceSection} coupled with this
	 *         {@link InferenceRow}
	 */
	InferenceSection getInferenceSection() {
		return section_;
	}

	/**
	 * @return the name of the inference
	 */
	String getName() {
		return section_.getName();
	}

	/**
	 * The cached hash value
	 */
	private int hash_ = 0;

	@Override
	public ConclusionSection getParent() {
		return section_.getParentSection();
	}

	@Override
	List<ConclusionSection> computeChildSections() {
		Collection<? extends ProofNode<OWLAxiom>> premises = section_
				.getInference().getPremises();
		List<ConclusionSection> result = new ArrayList<ConclusionSection>(
				premises.size());
		for (ProofNode<OWLAxiom> premise : premises) {
			result.add(new ConclusionSection(section_, premise));
		}
		return result;
	}

	@Override
	public boolean isExpanded() {
		return true;
	}

	@Override
	public boolean isExpandable() {
		return false;
	}

	@Override
	public String getTooltip() {
		Inference<OWLAxiom> example = section_.getInference().getExample();
		if (example == null) {
			return null;
		}
		OWLModelManager man = section_.getFrame().getEditorKit()
				.getOWLModelManager();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<style>");
		sb.append("ol li { margin: 4px 0; font-weight: bold; }");
		sb.append("p { font-weight: bold; }");
		sb.append("</style>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<p>");		
		sb.append(man.getRendering(example.getConclusion()));
		sb.append("</p>");
		sb.append("<br>");
		sb.append("&emsp;");
		List<? extends OWLAxiom> premises = example.getPremises();
		if (premises.isEmpty()) {
			sb.append("is a tautology");
		} else {
			sb.append("is inferred from:");
		}
		sb.append("<ol>");
		for (int i = 0; i < premises.size(); i++) {
			sb.append("<li>");
			sb.append(man.getRendering(premises.get(i)));
		}
		sb.append("</ol>");
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}

	@Override
	public void toggleExpandState() {
		return;
	}

	@Override
	public int hashCode() {
		if (hash_ == 0) {
			hash_ += section_.getName().hashCode();
			for (ProofNode<OWLAxiom> premise : section_.getInference()
					.getPremises()) {
				hash_ += premise.getMember().hashCode();
			}
		}
		return hash_;
	}

	@Override
	public String toString() {
		return section_.toString();
	}

	@Override
	public int compareTo(InferenceRow other) {
		return hashCode() - other.hashCode();
	}

	@Override
	public void dispose() {
		super.dispose();
		section_.dispose();
	}

	@Override
	public boolean matches(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (hashCode() != o.hashCode()) {
			return false;
		}
		if (o instanceof InferenceRow) {
			InferenceRow other = (InferenceRow) o;
			if (!section_.getName().equals(other.section_.getName())) {
				return false;
			}
			Collection<? extends ProofNode<OWLAxiom>> premises = section_
					.getInference().getPremises();
			Collection<? extends ProofNode<OWLAxiom>> otherPremises = other.section_
					.getInference().getPremises();
			if (premises.size() != otherPremises.size()) {
				return false;
			}
			Iterator<? extends ProofNode<OWLAxiom>> premiseIterator = premises
					.iterator();
			Iterator<? extends ProofNode<OWLAxiom>> otherPremisesIterator = otherPremises
					.iterator();
			while (premiseIterator.hasNext()) {
				if (!premiseIterator.next().getMember()
						.equals(otherPremisesIterator.next().getMember())) {
					return false;
				}
			}
			// else
			return true;
		}
		// else
		return false;
	}

	@Override
	public <O> O accept(Visitor<O> visitor) {
		return visitor.visit(this);
	}
}
