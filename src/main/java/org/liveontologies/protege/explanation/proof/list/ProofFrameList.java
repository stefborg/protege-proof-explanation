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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import org.liveontologies.protege.explanation.proof.ProofBasedExplanationPreferences;
import org.protege.editor.core.ProtegeProperties;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrameListener;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.protege.editor.owl.ui.framelist.OWLFrameListInferredSectionRowBorder;
import org.protege.editor.owl.ui.tree.OWLObjectTree;

/**
 * A custom {@link OWLFrameList} to display proof trees. In the proof tree,
 * there are two types of rows: conclusion rows for OWL axioms and inference
 * rows. Each conclusion can be produced my zero or more inferences which are
 * displayed as children of this row. Each inference can have zero or more
 * premises, which are likewise displayed as children. The children for
 * conclusion rows can be expanded and collapsed using the tree navigation
 * buttons. Since {@link OWLFrameList} is based on a {@link JList}, tree
 * navigation elements are painted and handled manually here. We could not use
 * {@link JTree}-based components, such as {@link OWLObjectTree} since axioms
 * would need to be wrapped around (as they can be very long). Also hyper-links
 * (for classes, properties, etc) and action buttons (edit, delete, annotate,
 * explain axioms, etc) would have to be handled manually.
 * 
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 */
@SuppressWarnings("serial")
public class ProofFrameList extends OWLFrameList<ProofRoot> {

	public static final Border INFERRED_BORDER = new OWLFrameListInferredSectionRowBorder();

	public static final Color HIGHLIGHT_COLOR = new Color(230, 215, 246);

	/**
	 * The time it takes for pressing at the navigation icon until recursive
	 * expansion / collapse is triggered
	 */
	public static final int LONG_PRESS_THRESHOLD = 500; // ms

	/**
	 * Action keys
	 */
	private final static String NAV_CLICK = "Navigation Click",
			NAV_ALT_CLICK = "Navigation Button Click",
			NAV_LONG_PRESS = "Navigation Button Long Press",
			ACTION_EXPAND_ROW = "Expand Row",
			ACTION_COLLAPSE_ROW = "Collapse Row",
			ACTION_ENTER_ROW = "Toggle Expand Row";

	/**
	 * Settings for dashed navigation help lines
	 */
	public static final BasicStroke DASH = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f,
			new float[] { 1.0f }, 0.0f);

	/**
	 * Navigation icons
	 */
	public static Icon TREE_EXPANDED, TREE_COLLAPSED;

	/**
	 * The space left / right from the navigation
	 */
	public static int LEFT_CHILD_INDENT, RIGHT_CHILD_INDENT;

	/**
	 * The horizontal space between the conclusion and inference rows
	 */
	public static final int INFERENCE_INDENT = 4;

	/**
	 * {@code true} if the navigation help lines should be painted and if they
	 * are dashed
	 */
	public static boolean PAINT_LINES, LINE_TYPE_DASHED;

	/**
	 * The color used for painting of the navigation help lines
	 */
	public static Color LINE;

	{
		initNavigationProperties();
		UIManager.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				initNavigationProperties();
			}
		});
	}

	/**
	 * The instance used to paint the navigation buttons
	 */
	private final NavButton navButton_ = new NavButton(this);
	
	/**
	 * The maximal number of rows expanded / collapsed recursively
	 */
	private final int recursiveExpansionLimit_; // rows

	public ProofFrameList(OWLEditorKit editorKit, ProofFrame proofFrame) {
		super(editorKit, proofFrame);
		ProofBasedExplanationPreferences prefs = new ProofBasedExplanationPreferences()
				.load();
		recursiveExpansionLimit_ = prefs.recursiveExpansionLimit;
		setModel(new ProofFrameListModel(proofFrame));
		setUI(new ProofFrameListUI());
		setCellRenderer(new ProofFrameListRenderer(editorKit));
		getSelectionModel()
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				getProofListModel().setHighlightsFor(getSelectedIndex());
			}
		});
		getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), ACTION_EXPAND_ROW);
		getInputMap().put(KeyStroke.getKeyStroke("LEFT"), ACTION_COLLAPSE_ROW);
		getInputMap().put(KeyStroke.getKeyStroke("ENTER"), ACTION_ENTER_ROW);
		getActionMap().put(ACTION_EXPAND_ROW, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProofFrameListRow row = getSelectedValue();
				if (row == null) {
					return;
				}
				if (!row.isExpanded()) {
					toggleExpandState(row);
				}
				ensureIndexIsVisible(getSelectedIndex());
			}
		});
		getActionMap().put(ACTION_COLLAPSE_ROW, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProofFrameListRow row = getSelectedValue();
				if (row == null) {
					return;
				}
				if (row.isExpanded() && row.isExpandable()) {
					toggleExpandState(row);
				} else {
					ProofFrameListRow parent = row.getParent();
					if (parent != null) {
						int index = parent.getIndex();
						setSelectedIndex(index);
					}
				}
				ensureIndexIsVisible(getSelectedIndex());
			}
		});
		getActionMap().put(ACTION_ENTER_ROW, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleEnterKey(getSelectedValue());
				ensureIndexIsVisible(getSelectedIndex());
			}
		});
		addMouseListener(new ProofFrameListMouseListener());
	}

	/**
	 * sets the properties used to paint tree navigation
	 */
	private static void initNavigationProperties() {
		TREE_EXPANDED = (Icon) UIManager.get("Tree.expandedIcon");
		TREE_COLLAPSED = (Icon) UIManager.get("Tree.collapsedIcon");
		LEFT_CHILD_INDENT = (int) UIManager.get("Tree.leftChildIndent");
		RIGHT_CHILD_INDENT = (int) UIManager.get("Tree.rightChildIndent");
		PAINT_LINES = UIManager.getBoolean("Tree.paintLines");
		// TODO: switch when supported to: PAINT_LINES =
		// OWLTreePreferences.getInstance().isPaintLines();
		LINE_TYPE_DASHED = UIManager.getBoolean("Tree.lineTypeDashed")
				// the plastic UI does not set dash lines although uses them
				|| UIManager.getLookAndFeel().getClass().getName()
						.equals(ProtegeProperties.PLASTIC_LAF_NAME);
		LINE = UIManager.getColor("Tree.hash");
	}

	/**
	 * @param row
	 * @return the distance between the left border and where the content of the
	 *         row should begin; this space is used for navigation lines
	 */
	static int getRowIndent(ProofFrameListRow row) {
		return row.accept(new ProofFrameListRow.Visitor<Integer>() {

			@Override
			public Integer visit(InferenceRow row) {
				return row.getRowDepth() * (LEFT_CHILD_INDENT
						+ RIGHT_CHILD_INDENT + INFERENCE_INDENT) / 2
						+ INFERENCE_INDENT;
			}

			@Override
			public Integer visit(ConclusionSection row) {
				return (row.getRowDepth() + 1) * (LEFT_CHILD_INDENT
						+ RIGHT_CHILD_INDENT + INFERENCE_INDENT) / 2;
			}
		}) - INFERENCE_INDENT; // should be 0 for the first row
	}

	/**
	 * @return the result of {@link #getModel()} after it is set to
	 *         {@link ProofFrameListModel}
	 */
	ProofFrameListModel getProofListModel() {
		return (ProofFrameListModel) super.getModel();
	}

	@Override
	public void setListData(final Object[] listData) {
		// OWLFrameList resets the model after every content change (why??),
		// I did not find a clean way to prevent this from happening other
		// than by overriding this method
	}

	@Override
	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		if (newValue == oldValue) {
			// don't fire if there is no change
			return;
		}
		// else
		super.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	protected void clearCellHeightCache() {
		// why is this needed??
	}

	@Override
	public void refreshComponent() {
		// should happen automatically via change listeners
	}

	@Override
	public ProofFrameListRow getSelectedValue() {
		return (ProofFrameListRow) super.getSelectedValue();
	}

	@Override
	public ProofFrame getFrame() {
		return (ProofFrame) super.getFrame();
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (event == null) {
			return super.getToolTipText();
		}
		// else
		Point location = event.getPoint();
		int index = locationToIndex(location);
		if (index < 0 || !getCellBound(index).contains(location)) {
			return super.getToolTipText();
		}
		// else
		ProofFrameListRow row = getProofListModel().getElementAt(index);
		if (row instanceof InferenceRow) {
			return row.getTooltip();	
		}
		// else
		return super.getToolTipText(event);
	}

	protected void handleEnterKey(ProofFrameListRow row) {
		if (row instanceof ConclusionSection) {
			ConclusionSection conclRow = (ConclusionSection) row;
			if (conclRow.isEditable()) {
				handleEdit();
			} else {
				toggleExpandState(conclRow);
			}
		}
	}

	protected void handleDoubleClick(ProofFrameListRow row) {
		if (row instanceof ConclusionSection) {
			ConclusionSection conclRow = (ConclusionSection) row;
			if (conclRow.isEditable()) {
				// already handled by the standard mouse listener;
			} else {
				toggleExpandState(conclRow);
			}
		}
	}

	protected void toggleExpandState(ProofFrameListRow row) {
		getProofListModel().toggleExpandState(row);
	}

	protected void toggleExpandRecursive(ProofFrameListRow row) {
		getProofListModel().toggleExpandRecursive(row);
	}

	/**
	 * @param index
	 * @return the bounding rectangle for the contents of the cell at the given
	 *         index (excluding the left border)
	 */
	Rectangle getCellBound(int index) {
		Rectangle bounds = getCellBounds(index, index);
		if (bounds != null) {
			// excluding the margin from the bound
			int oldX = bounds.x;
			bounds.x = getRowIndent(getProofListModel().getElementAt(index));
			bounds.width = bounds.width + oldX - bounds.x;
		}
		return bounds;
	}

	@Override
	protected Border createPaddingBorder(
			@SuppressWarnings("rawtypes") JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		return BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE);
	}

	@Override
	protected Border createListItemBorder(
			@SuppressWarnings("rawtypes") JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		if (value instanceof ConclusionSection) {
			ConclusionSection row = (ConclusionSection) value;

			Border internalPadding = BorderFactory.createEmptyBorder(1, 1, 1,
					1);
			Border line = BorderFactory.createMatteBorder(0, 0, 1, 0,
					new Color(240, 240, 240));
			Border externalBorder = BorderFactory.createMatteBorder(0,
					getRowIndent(row), 0, 0, list.getBackground());
			Border border = BorderFactory.createCompoundBorder(externalBorder,
					BorderFactory.createCompoundBorder(line, internalPadding));

			if (row.isInferred()) {
				border = BorderFactory.createCompoundBorder(border,
						INFERRED_BORDER);
			}
			return border;
		}
		// else
		return super.createListItemBorder(list, value, index, isSelected,
				cellHasFocus);
	}

	@Override
	protected String getRowName(Object rowObject) {
		if (rowObject instanceof ConclusionSection) {
			ConclusionSection conclusionSection = (ConclusionSection) rowObject;
			if (conclusionSection.isAsserted()) {
				return "Asserted axiom";
			} else {
				return "Entailed axiom";
			}
		}
		// else
		return super.getRowName(rowObject);
	}

	@Override
	protected Color getItemBackgroundColor(MListItem item) {
		if (item instanceof ConclusionSection) {
			ConclusionSection exprRow = (ConclusionSection) item;

			if (exprRow.isHighlighted()) {
				return HIGHLIGHT_COLOR;
			}

			if (!exprRow.isAsserted()) {
				return INFERRED_BG_COLOR;
			}
		}

		return super.getItemBackgroundColor(item);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Color oldColor = g.getColor();
		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();
		if (PAINT_LINES) {
			if (LINE_TYPE_DASHED) {
				g2.setStroke(DASH);
			}
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.setColor(LINE);
			paintLines(g2);
		}
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		paintNavButtons(g2);
		g.setColor(oldColor);
		g2.setStroke(oldStroke);
	}

	@Override
	public void repaint() {
		super.repaint();
	}

	private void paintNavButtons(final Graphics2D g2) {
		for (int index = 0; index < getModel().getSize(); index++) {
			NavButton button = getNavButton(index);
			if (button != null
					&& button.getBounds().intersects(g2.getClipBounds())) {
				button.paintButtonContent(g2);
			}
		}
	}

	/**
	 * @param row
	 * @return the x coordinate of the the navigation icon for the given row
	 */
	private int getNavX(ProofFrameListRow row) {
		return getRowIndent(row) - RIGHT_CHILD_INDENT;
	}

	/**
	 * @param row
	 * @return the y coordinate of the the navigation icon for the given row
	 */
	private int getNavY(ProofFrameListRow row) {
		int index = row.getIndex();
		Rectangle rowBounds = getCellBounds(index, index);
		return rowBounds.y + getButtonDimension() / 2 + 1;
	}

	/**
	 * Paints the navigation help lines
	 * 
	 * @param g2
	 */
	private void paintLines(final Graphics2D g2) {
		for (int i = 0; i < this.getModel().getSize(); i++) {
			final int index = i;
			ProofFrameListRow row = getProofListModel().getElementAt(index);
			row.accept(new ProofFrameListRow.Visitor<Void>() {

				@Override
				public Void visit(InferenceRow row) {
					// vertical lines
					if (!row.isExpanded() || row.getIndex() == 0) {
						return null;
					}
					List<? extends ProofFrameListRow> children = row
							.getChildren();
					int size = children.size();
					if (size == 0) {
						return null;
					}
					ProofFrameListRow lastChild = children.get(size - 1);
					int lastIndex = lastChild.getIndex();
					Rectangle childBounds = getCellBounds(index + 1, lastIndex);
					int x = getNavX(row) + LEFT_CHILD_INDENT
							+ RIGHT_CHILD_INDENT;
					int y1 = childBounds.y;
					int y2 = getNavY(lastChild);
					// draw only if the line intersects with the clipping area
					Rectangle clipBound = g2.getClipBounds();
					if (clipBound.x <= x && x <= clipBound.x + clipBound.width
							&& clipBound.y <= y2
							&& y1 <= clipBound.y + clipBound.height) {
						g2.drawLine(x, y1, x, y2);
					}
					return null;
				}

				@Override
				public Void visit(ConclusionSection row) {
					// horizontal lines
					int x1 = getNavX(row);
					int x2 = x1 + RIGHT_CHILD_INDENT - 1;
					int y = getNavY(row);
					// draw only if the line intersects with the clipping area
					Rectangle clipBound = g2.getClipBounds();
					if (clipBound.x <= x2 && x1 <= clipBound.x + clipBound.width
							&& clipBound.y <= y
							&& y <= clipBound.y + clipBound.height) {
						g2.drawLine(x1, y, x2, y);
					}
					return null;
				}
			});
		}
	}

	NavButton getNavButton(int index) {
		final ProofFrameListRow row = getProofListModel().getElementAt(index);
		if (row == null) {
			return null;
		}
		if (!row.isExpandable()) {
			// cannot be expanded
			return null;
		}
		int x = getNavX(row);
		int y = getNavY(row);
		if (row.isExpanded()) {
			navButton_.setIcon(TREE_EXPANDED, x, y);
		} else {
			navButton_.setIcon(TREE_COLLAPSED, x, y);
		}
		navButton_.setActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getActionCommand()) {
				case NAV_CLICK:
					toggleExpandState(row);
					break;
				case NAV_ALT_CLICK:
					toggleExpandRecursive(row);
					break;
				case NAV_LONG_PRESS:
					toggleExpandRecursive(row);
					break;
				}
			}
		});
		return navButton_;
	}

	NavButton getNavButton(Point location) {
		int index = locationToIndex(location);
		NavButton button = getNavButton(index);
		if (button != null && button.getBounds().contains(location)) {
			return button;
		}
		// else
		return null;
	}

	/**
	 * A custom list model that updates the list of visible rows of the proof
	 * 
	 * @author Yevgeny Kazakov
	 */
	class ProofFrameListModel extends AbstractListModel<ProofFrameListRow>
			implements OWLFrameListener {

		private final ProofFrame frame_;

		private List<ProofFrameListRow> sections_;

		/**
		 * The index of the currently selected row; should be in sync with
		 * {@link ProofFrameList#getSelectedIndex()}; this index is used for
		 * highlighting rows
		 */
		private int selectedIndex_ = -1;

		/**
		 * the path leading to the previously selected row; used to update the
		 * selected index
		 */
		private final Deque<ProofFrameListRow> previousSelectionPath_ = new ArrayDeque<ProofFrameListRow>();

		ProofFrameListModel(ProofFrame frame) {
			this.frame_ = frame;
			frame_.addFrameListener(this);
		}

		private List<ProofFrameListRow> getSections() {
			initialize();
			return sections_;
		}

		private boolean invalidate() {
			if (sections_ == null) {
				return false;
			}
			ProofFrameListRow next = getElementAt(selectedIndex_);
			while (next != null) {
				previousSelectionPath_.addFirst(next);
				next = next.getParent();
			}
			setHighlightsFor(-1);
			sections_ = null;
			return true;
		}

		private boolean initialize() {
			if (sections_ != null) {
				return false;
			}
			sections_ = new ArrayList<ProofFrameListRow>();
			Deque<ProofFrameListRow> todo = new ArrayDeque<ProofFrameListRow>();
			todo.add(frame_.getRootSection().getRow());
			int newSelectedIndex = -1;
			ProofFrameListRow nextSelectedCandidate = previousSelectionPath_
					.poll();
			for (;;) {
				ProofFrameListRow next = todo.poll();
				if (next == null) {
					break;
				}
				int index = sections_.size();
				next.setIndex(index);
				if (getIndex(next.getParent()) == newSelectedIndex
						&& next.matches(nextSelectedCandidate)) {
					newSelectedIndex = index;
					nextSelectedCandidate = previousSelectionPath_.poll();
				}
				sections_.add(next);
				if (!next.isExpanded()) {
					continue;
				}
				// else if expanded
				List<? extends ProofFrameListRow> children = next.getChildren();
				// push children preserving the order
				for (int i = children.size() - 1; i >= 0; i--) {
					todo.push(children.get(i));
				}
			}
			previousSelectionPath_.clear();

			setSelectedIndex(newSelectedIndex);
			// even if the selected index did not change, we still need to
			// update the highlights since they have been removed
			setHighlightsFor(newSelectedIndex);
			return true;
		}

		private int getIndex(ProofFrameListRow row) {
			if (row == null) {
				return -1;
			}
			// else
			return row.getIndex();
		}

		private void setHighlightsFor(int newSelectedIndex) {
			int oldSelectedIndex = selectedIndex_;
			if (oldSelectedIndex == newSelectedIndex) {
				return;
			}
			selectedIndex_ = newSelectedIndex;
			handleHighlightChange(oldSelectedIndex);
			handleHighlightChange(selectedIndex_);
		}

		private void handleHighlightChange(int index) {
			if (index < 0 || index >= getSize()) {
				return;
			}
			ProofFrameListRow item = getElementAt(index);
			item.accept(new ProofFrameListRow.Visitor<Void>() {

				@Override
				public Void visit(ConclusionSection row) {
					return null;
				}

				@Override
				public Void visit(InferenceRow row) {
					toggleHighlightChildren(row);
					ConclusionSection conclusion = row.getParent();
					if (conclusion != null) {
						toggleHighlight(conclusion);
					}
					return null;
				}
			});
		}

		private void toggleHighlight(ProofFrameListRow row) {
			row.toggleHighlight();
			int changedIndex = row.getIndex();
			// just repaint the cell since nothing is moved
			Rectangle bounds = getCellBounds(changedIndex, changedIndex);
			if (bounds != null) {
				// excluding the margin from the bound
				int oldX = bounds.x;
				bounds.x = getRowIndent(row);
				bounds.width = bounds.width + oldX - bounds.x;
				repaint(bounds);
			}
		}

		private void toggleHighlightChildren(ProofFrameListRow row) {
			for (ProofFrameListRow child : row.getChildren()) {
				toggleHighlight(child);
			}
		}

		private void toggleExpandState(ProofFrameListRow row) {
			if (!row.isExpandable()) {
				return;
			}
			int intervalBegin = row.getIndex() + 1;
			if (row.isExpanded()) {
				int intervalEnd = getLowestExpandedDescendant(row).getIndex();
				row.toggleExpandState();
				invalidate();
				fireIntervalRemoved(this, intervalBegin, intervalEnd);
			} else {
				row.toggleExpandState();
				invalidate();
				int intervalEnd = getLowestExpandedDescendant(row).getIndex();
				fireIntervalAdded(this, intervalBegin, intervalEnd);
			}
		}

		private void toggleExpandRecursive(ProofFrameListRow row) {
			if (!row.isExpandable()) {
				return;
			}
			int intervalBegin = row.getIndex() + 1;
			if (row.isExpanded()) {
				int intervalEnd = getLowestExpandedDescendant(row).getIndex();
				row.collapseRecursively(Integer.MAX_VALUE);
				invalidate();
				fireIntervalRemoved(this, intervalBegin, intervalEnd);
			} else {
				row.expandRecursively(recursiveExpansionLimit_);
				invalidate();
				int intervalEnd = getLowestExpandedDescendant(row).getIndex();
				fireIntervalAdded(this, intervalBegin, intervalEnd);
			}
		}

		/**
		 * @param parent
		 * @return the lowest row in the list that is a descendant of the given
		 *         parent row
		 */
		private ProofFrameListRow getLowestExpandedDescendant(
				ProofFrameListRow parent) {
			ProofFrameListRow lowest = parent;
			for (;;) {
				if (!lowest.isExpanded()) {
					return lowest;
				}
				List<? extends ProofFrameListRow> children = lowest
						.getChildren();
				int childCount = children.size();
				if (childCount == 0) {
					return lowest;
				} else {
					lowest = children.get(childCount - 1);
				}
			}
		}

		@Override
		public int getSize() {
			return getSections().size();
		}

		@Override
		public ProofFrameListRow getElementAt(int index) {
			if (index < 0 || index >= getSections().size()) {
				return null;
			}
			// else
			return getSections().get(index);
		}

		@Override
		public void frameContentChanged() throws Exception {
			if (!invalidate()) {
				return;
			}
			initialize();
			fireContentsChanged(this, 0, Integer.MAX_VALUE);
		}

	}

	/**
	 * A custom list UI that (1) ignores mouse position at the navigation border
	 * (tooltips are not shown, rows are not selected on click), and (2) caches
	 * the rendered row heights so that calculation of row positions is fast.
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	class ProofFrameListUI extends OWLFrameListUI {

		/**
		 * the maximal height of the row for which it is considered to be
		 * without line wrap, i.e., it has only one line
		 */
		private static final int MAX_LINE_HEIGHT_ = 24;

		private int[] cumulativeCellHeight;

		@Override
		protected MouseInputListener createMouseInputListener() {

			final MouseInputListener oldListener = super.createMouseInputListener();

			// ignore mouse positions on the navigation border
			return new MouseInputListener() {

				boolean shouldProcess(MouseEvent e) {
					Point location = e.getPoint();
					int index = ProofFrameList.this.locationToIndex(location);
					return getCellBound(index).contains(location);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if (shouldProcess(e)) {
						oldListener.mousePressed(e);
					}
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					if (shouldProcess(e)) {
						oldListener.mouseClicked(e);
					}

				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (shouldProcess(e)) {
						oldListener.mouseReleased(e);
					}

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					if (shouldProcess(e)) {
						oldListener.mouseEntered(e);
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if (shouldProcess(e)) {
						oldListener.mouseExited(e);
					}
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					if (shouldProcess(e)) {
						oldListener.mouseDragged(e);
					}
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					if (shouldProcess(e)) {
						oldListener.mouseMoved(e);
					}
				}

			};
		}

		@Override
		protected void updateLayoutState() {
			cumulativeCellHeight = new int[list.getModel().getSize()];
			/*
			 * If both JList fixedCellWidth and fixedCellHeight have been set,
			 * then initialize cellWidth and cellHeight, and set cellHeights to
			 * null.
			 */
			int fixedCellHeight = list.getFixedCellHeight();
			int fixedCellWidth = list.getFixedCellWidth();
			cellWidth = fixedCellWidth != -1 ? fixedCellWidth : -1;
			if (fixedCellHeight != -1) {
				cellHeight = fixedCellHeight;
				cellHeights = null;
			} else {
				cellHeight = -1;
				cellHeights = new int[list.getModel().getSize()];
			}
			/*
			 * If either of JList fixedCellWidth and fixedCellHeight haven't
			 * been set, then initialize cellWidth and cellHeights by scanning
			 * through the entire model. Note: if the renderer is null, we just
			 * set cellWidth and cellHeights[*] to zero, if they're not set
			 * already.
			 */
			if (fixedCellWidth == -1 || fixedCellHeight == -1) {
				ListModel<?> dataModel = list.getModel();
				int dataModelSize = dataModel.getSize();
				ListCellRenderer renderer = list.getCellRenderer();
				if (renderer != null) {
					Insets insets = list.getInsets();
					int listWidth = list.getWidth()
							- (insets.left + insets.right);
					int cumulativeHeight = 0;
					for (int index = 0; index < dataModelSize; index++) {
						Object value = dataModel.getElementAt(index);
						boolean useCachedHeight = false;

						if (value instanceof ProofFrameListRow) {
							ProofFrameListRow row = (ProofFrameListRow) value;
							if (row.getListMinWidth() <= listWidth
									&& listWidth <= row.getListMaxWidth()) {
								useCachedHeight = true;
								cellHeights[index] = row.getCachedRowHeight();
								if (fixedCellWidth == -1) {
									cellWidth = Math.max(listWidth, cellWidth);
								}
							}
						}

						if (!useCachedHeight) {
							Component c = renderer.getListCellRendererComponent(
									list, value, index, false, false);
							rendererPane.add(c);
							Dimension cellSize = c.getPreferredSize();
							if (fixedCellWidth == -1) {
								cellWidth = Math.max(cellSize.width, cellWidth);
							}
							if (fixedCellHeight == -1) {
								cellHeights[index] = cellSize.height;
							}

							if (value instanceof ProofFrameListRow) {
								ProofFrameListRow row = (ProofFrameListRow) value;
								if (cellSize.height != row
										.getCachedRowHeight()) {
									row.setCachedRowHeight(cellSize.height);
									row.setListMinWidth(listWidth);
									if (cellSize.height <= MAX_LINE_HEIGHT_) {
										row.setListMaxWidth(Integer.MAX_VALUE);
									} else {
										row.setListMaxWidth(listWidth);
									}
								} else if (listWidth < row.getListMinWidth()) {
									row.setListMinWidth(listWidth);
								} else if (listWidth > row.getListMaxWidth()) {
									row.setListMaxWidth(listWidth);
								}
							}
						}
						cumulativeHeight += cellHeights[index];
						cumulativeCellHeight[index] = cumulativeHeight;
					}
				} else {
					if (cellWidth == -1) {
						cellWidth = 0;
					}
					if (cellHeights == null) {
						cellHeights = new int[dataModelSize];
					}
					for (int index = 0; index < dataModelSize; index++) {
						cellHeights[index] = 0;
					}
				}
			}
		}

		@Override
		public Rectangle getCellBounds(JList list, int index1, int index2) {
			maybeUpdateLayoutState();
			int minIndex = Math.min(index1, index2);
			int maxIndex = Math.max(index1, index2);
			if (minIndex >= list.getModel().getSize()) {
				return null;
			}
			Rectangle minBounds = getCellBounds(list, minIndex);
			if (minBounds == null) {
				return null;
			}
			if (minIndex == maxIndex) {
				return minBounds;
			}
			Rectangle maxBounds = getCellBounds(list, maxIndex);
			if (maxBounds != null) {
				if (minBounds.x != maxBounds.x) {
					// Different columns
					minBounds.y = 0;
					minBounds.height = list.getHeight();
				}
				minBounds.add(maxBounds);
			}
			return minBounds;
		}

		/**
		 * Gets the bounds of the specified model index, returning the resulting
		 * bounds, or null if <code>index</code> is not valid.
		 */
		private Rectangle getCellBounds(JList list, int index) {
			if (index < 0) {
				return new Rectangle();
			}
			maybeUpdateLayoutState();
			if (index >= cumulativeCellHeight.length) {
				return null;
			}
			Insets insets = list.getInsets();
			int x;
			int w;
			int y;
			int h;
			x = insets.left;
			if (index >= cellHeights.length) {
				y = 0;
			} else {
				y = cumulativeCellHeight[index] - cellHeights[index];
			}
			w = list.getWidth() - (insets.left + insets.right);
			h = cellHeights[index];
			return new Rectangle(x, y, w, h);
		}

	}

	class ProofFrameListMouseListener extends MouseAdapter
			implements ActionListener {

		/**
		 * the timer to initiate the long press action after the mouse has been
		 * pressed for #LONG_PRESS_THRESHOLD ms
		 */
		private final Timer longPressTimer_;

		/**
		 * {@code true} if action following long pressed has been activated but
		 * not finished yet
		 */
		private boolean longPressActionTriggered_ = false;

		ProofFrameListMouseListener() {
			longPressTimer_ = new Timer(LONG_PRESS_THRESHOLD, this);
			longPressTimer_.setRepeats(false);
		}

		@Override
		public void mousePressed(MouseEvent event) {
			NavButton button = getNavButton(event.getPoint());
			if (button == null) {
				return;
			}
			// else
			longPressTimer_.start();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// long press action triggered
			Point point = getMousePosition();
			if (point == null) {
				return;
			}
			NavButton button = getNavButton(point);
			if (button == null) {
				return;
			}
			// else
			button.getActionListener().actionPerformed(new ActionEvent(this,
					ActionEvent.ACTION_PERFORMED, NAV_LONG_PRESS));
			longPressActionTriggered_ = true;
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (longPressActionTriggered_) {
				longPressActionTriggered_ = false;
				return;
			}
			longPressTimer_.stop(); // if it was activated
			Point location = event.getPoint();
			NavButton button = getNavButton(location);
			if (button != null) {
				button.getActionListener()
						.actionPerformed(new ActionEvent(this,
								ActionEvent.ACTION_PERFORMED, event.isAltDown()
										? NAV_ALT_CLICK : NAV_CLICK));
			} else if (event.getClickCount() == 2) {
				int index = locationToIndex(location);
				if (getCellBound(index).contains(location)) {
					handleDoubleClick(getProofListModel().getElementAt(index));
				}
			}
		}

	}

}
