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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;

/**
 * 
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 *
 */
public class ProofFrameListRenderer
		implements ListCellRenderer<ProofFrameListRow> {

	private static final Color FRAME_SECTION_HEADER_FOREGROUND = Color.GRAY;

	private static final Color FRAME_SECTION_HEADER_HIGH_CONTRAST_FOREGROUND = new Color(
			40, 40, 40);

	private OWLCellRenderer owlCellRenderer;

	private ListCellRenderer<Object> separatorRenderer;

	public ProofFrameListRenderer(OWLEditorKit owlEditorKit) {
		owlCellRenderer = new OWLCellRenderer(owlEditorKit);
		separatorRenderer = new DefaultListCellRenderer();
	}

	@Override
	public Component getListCellRendererComponent(
			final JList<? extends ProofFrameListRow> list,
			ProofFrameListRow value, final int index, final boolean isSelected,
			final boolean cellHasFocus) {

		return value.accept(new ProofFrameListRow.Visitor<Component>() {

			@Override
			public Component visit(ConclusionSection row) {
				Object valueToRender = getValueToRender(list, row, index,
						isSelected, cellHasFocus);

				owlCellRenderer.setCommentedOut(false);
				owlCellRenderer.setOntology(row.getOntology());
				owlCellRenderer.setInferred(row.isInferred());
				owlCellRenderer.setHighlightKeywords(true);
				owlCellRenderer.setHighlightUnsatisfiableClasses(true);
				owlCellRenderer.setHighlightUnsatisfiableProperties(true);
				owlCellRenderer.setIconObject(row.getAxiom());

				return (JPanel) owlCellRenderer.getListCellRendererComponent(
						list, valueToRender, index, isSelected, cellHasFocus);

			}

			@Override
			public Component visit(InferenceRow row) {
				JLabel label = (JLabel) separatorRenderer
						.getListCellRendererComponent(list, row.getName(),
								index, isSelected, cellHasFocus);
				Font font = label.getFont();
				font = font.deriveFont(font.getStyle(), font.getSize() * 0.9f);
				label.setFont(font);
				label.setForeground(isSelected ? list.getSelectionForeground()
						: getSectionHeaderForeground());
				label.setVerticalAlignment(JLabel.TOP);

				int padding = ProofFrameList.getRowIndent(row);
				Border externalBorder = BorderFactory.createMatteBorder(0,
						padding, 0, 0, list.getBackground());
				label.setBorder(externalBorder);

				return label;
			}
		});

	}

	private Color getSectionHeaderForeground() {
		if (UIUtil.isHighContrastOn()) {
			return FRAME_SECTION_HEADER_HIGH_CONTRAST_FOREGROUND;
		} else {
			return FRAME_SECTION_HEADER_FOREGROUND;
		}
	}

	public void setWrap(boolean b) {
		owlCellRenderer.setWrap(b);
	}

	protected Object getValueToRender(JList<?> list, ConclusionSection value,
			int index, boolean isSelected, boolean cellHasFocus) {
		return value.getRendering();
	}

}
