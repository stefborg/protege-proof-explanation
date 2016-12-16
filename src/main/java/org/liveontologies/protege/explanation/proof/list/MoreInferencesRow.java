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

import java.util.Collections;
import java.util.List;

import org.protege.editor.owl.ui.frame.OWLFrameSection;

/**
 * A row in a {@link ProofFrameList} that can be used for loading more
 * inferences for the parent conclusion section. This class is used for
 * rendering of the row in {@link ProofFrameListRenderer} since it is not
 * possible to change the rendering of a {@link OWLFrameSection}.
 * 
 * @author Yevgeny Kazakov
 */
class MoreInferencesRow extends AbstractProofFrameListRow<ConclusionSection> {

	private final ConclusionSection parent_;

	MoreInferencesRow(ConclusionSection parent) {
		this.parent_ = parent;
	}

	@Override
	public ConclusionSection getParent() {
		return parent_;
	}

	@Override
	List<ConclusionSection> computeChildSections() {
		// cannot have children
		return Collections.emptyList();
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
		return MoreInferencesButton.NAME;
	}

	@Override
	public void toggleExpandState() {
		// no-op
	}

	@Override
	public String toString() {
		return "...";
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public boolean matches(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (o instanceof MoreInferencesRow) {
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
