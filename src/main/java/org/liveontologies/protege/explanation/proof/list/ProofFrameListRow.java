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

import java.util.List;

import org.protege.editor.core.Disposable;

/**
 * Represents a row in {@link ProofFrameList}
 * 
 * @author Yevgeny Kazakov
 *
 */
public interface ProofFrameListRow extends Disposable {

	/**
	 * @return the index position of this row in the {@link ProofFrameList}; the
	 *         first row starts at the position 0
	 * 
	 */
	int getIndex();

	/**
	 * @return the row corresponding to the parent element of this row
	 */
	ProofFrameListRow getParent();

	/**
	 * @return the rows corresponding to the child elements of this row
	 */
	List<? extends ProofFrameListRow> getChildren();

	/**
	 * @return the nesting depth of this row; this is the number of time
	 *         {@link #getParent()} can be iterated until returning {@code null}
	 */
	int getRowDepth();

	/**
	 * @return {@code true} if this row is expanded and {@code false} otherwise
	 */
	boolean isExpanded();

	/**
	 * @return {@code true} if {@link #toggleExpandState()} changes the value of
	 *         {@link #isExpanded()}
	 */
	boolean isExpandable();

	/**
	 * @return {@code true} if this row should be highlighted in the
	 *         {@link ProofFrameList}
	 */
	boolean isHighlighted();

	/**
	 * @return the text to be shown on mouse over
	 */
	String getTooltip();

	/**
	 * Sets the index position of this row to the given value
	 * 
	 * @param index
	 *            the new index value of this row
	 * 
	 * @see #getIndex()
	 */
	void setIndex(int index);

	/**
	 * changes {@link #isExpanded()} to the complementary value
	 */
	void toggleExpandState();

	/**
	 * Sets {@link #isExpanded()} to {@code true} for this row and recursively
	 * for its children until reaching the given limit (that is, at most the
	 * limit number of descendants will be expanded). If limit is 0 or less,
	 * nothing changes.
	 * 
	 * @param limit
	 *            the maximal number of descendant rows which can be expanded
	 * 
	 * @see #isExpanded()
	 * @see #getChildren()
	 */
	void expandRecursively(int limit);

	/**
	 * Sets {@link #isExpanded()} to {@code false} for this row and recursively
	 * for its children until reaching the given limit (that is, at most the
	 * limit number of descendants will be collapsed). If limit is 0 or less,
	 * nothing changes.
	 * 
	 * @param limit
	 *            the maximal number of descendant rows which can be collapsed
	 * 
	 * @see #getChildren()
	 */
	void collapseRecursively(int limit);

	/**
	 * changes {@link #isHighlighted()} to the complementary value
	 */
	void toggleHighlight();

	/**
	 * Matches this row to the given object to check if they are similar (that
	 * is, the object is a row and it has the same content when displayed)
	 * 
	 * @param o
	 *            the object to be matched against this row
	 * @return {@code true} if this row matches the given object and
	 *         {@code false} otherwise
	 */
	boolean matches(Object o);

	/**
	 * @return the height of the last rendered component for this row
	 */
	int getCachedRowHeight();

	/**
	 * @return the minimum width of the list for which the height of the
	 *         rendering of this row equals to {@link #getCachedRowHeight()}
	 */
	int getListMinWidth();

	/**
	 * @return the maximum width of the list for which the height of the
	 *         rendering of this row equals to {@link #getCachedRowHeight()}
	 */
	int getListMaxWidth();

	/**
	 * Ensure that {@link #getCachedRowHeight} is equal to the given value
	 * 
	 * @param height
	 *            the row height value to be set
	 * 
	 * @see #getCachedRowHeight()
	 */
	void setCachedRowHeight(int height);

	/**
	 * Ensure that {@link #getListMinWidth()} is equal to the given value
	 * 
	 * @param width
	 *            the row minimum width value to be set
	 * 
	 * @see #getListMinWidth()
	 */
	void setListMinWidth(int width);

	/**
	 * ensures that {@link #getListMaxWidth()} is equal to the given value
	 * 
	 * @param width
	 *            the row maximum width value to be set
	 * @see #getListMaxWidth()
	 */
	void setListMaxWidth(int width);

	<O> O accept(Visitor<O> visitor);

	/**
	 * The visitor pattern for instances of {@link ProofFrameListRow}
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <O>
	 *            the type of the output for the visitor methods
	 */
	interface Visitor<O> {

		O visit(InferenceRow row);

		O visit(ConclusionSection row);

		O visit(MoreInferencesRow row);

	}

}
