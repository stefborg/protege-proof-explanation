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

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import org.protege.editor.core.Disposable;

/**
 * A skeleton implementation of a {@link ProofFrameListRow}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 */
public abstract class AbstractProofFrameListRow<C extends AbstractProofFrameListRow<?>>
		implements ProofFrameListRow, Disposable {

	/**
	 * The section corresponding to the premises of {@link #inference_};
	 * computed on request
	 */
	private List<C> childSections_ = null;

	private int index_ = 0;

	private boolean isHighlighted_ = false;

	/**
	 * cached rendering height and the list width range for which it is valid
	 */
	private int cachedRowHeight_ = 0, listMinWidth_ = 1, listMaxWidth_ = 0;

	@Override
	public int getIndex() {
		return index_;
	}

	@Override
	public boolean isHighlighted() {
		return isHighlighted_;
	}

	@Override
	public void setIndex(int index) {
		index_ = index;
	}

	@Override
	public void toggleHighlight() {
		isHighlighted_ ^= true;
	}

	abstract List<C> computeChildSections();

	@Override
	public synchronized List<C> getChildren() {
		if (childSections_ == null) {
			childSections_ = computeChildSections();
		}
		return childSections_;
	}

	@Override
	public int getRowDepth() {
		int result = 0;
		ProofFrameListRow row = this;
		while ((row = row.getParent()) != null) {
			result++;
		}
		return result;
	}

	@Override
	public void dispose() {
		if (childSections_ == null) {
			return;
		}
		for (C child : childSections_) {
			child.dispose();
		}
		childSections_ = null;
	}

	@Override
	public void expandRecursively(int limit) {
		setExpandedRecursive(true, limit);
	}

	@Override
	public void collapseRecursively(int limit) {
		setExpandedRecursive(false, limit);
	}

	@Override
	public int getCachedRowHeight() {
		return cachedRowHeight_;
	}

	@Override
	public int getListMinWidth() {
		return listMinWidth_;
	}

	@Override
	public int getListMaxWidth() {
		return listMaxWidth_;
	}

	@Override
	public void setCachedRowHeight(int height) {
		this.cachedRowHeight_ = height;
	}

	@Override
	public void setListMinWidth(int width) {
		this.listMinWidth_ = width;
	}

	@Override
	public void setListMaxWidth(int width) {
		this.listMaxWidth_ = width;
	}

	private void setExpandedRecursive(boolean expanded, int limit) {
		if (!isExpandable()) {
			return;
		}
		Queue<AbstractProofFrameListRow<?>> todo = new ArrayDeque<>();
		todo.add(this);
		for (;;) {
			if (--limit < 0) {
				return;
			}
			AbstractProofFrameListRow<?> next = todo.poll();
			if (next == null) {
				return;
			}
			if (next.isExpanded() != expanded) {
				next.toggleExpandState();
			}
			if (!expanded && next.childSections_ == null) {
				// no children, hence already fully collapsed
				continue;
			}
			for (AbstractProofFrameListRow<?> child : next.getChildren()) {
				todo.add(child);
			}
		}
	}

	/**
	 * Recursively copy settings like the expanded flag and cached row height
	 * values from the matching sections
	 * 
	 * @param previous
	 */
	void copySettingsFrom(AbstractProofFrameListRow<?> previous) {
		if (isExpanded() != previous.isExpanded()) {
			toggleExpandState();
		}
		cachedRowHeight_ = previous.cachedRowHeight_;
		listMinWidth_ = previous.listMinWidth_;
		listMaxWidth_ = previous.listMaxWidth_;
		if (previous.childSections_ == null) {
			return;
		}
		for (AbstractProofFrameListRow<?> previousChild : previous.childSections_) {
			// find the row which corresponds to the updated premise
			int pos = 0;
			int childCount = getChildren().size();
			while (pos < childCount) {
				C child = getChildren().get(pos);
				if (previousChild.matches(child)) {
					child.copySettingsFrom(previousChild);
					break;
				}
				// else
				pos++;
			}
		}
	}

}
