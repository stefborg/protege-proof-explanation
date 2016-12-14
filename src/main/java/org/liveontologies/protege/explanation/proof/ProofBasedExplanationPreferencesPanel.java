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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.protege.editor.core.ui.preferences.PreferencesPanel;

public class ProofBasedExplanationPreferencesPanel extends PreferencesPanel {

	private static final long serialVersionUID = 8585913940466665136L;

	private SpinnerNumberModel recursiveExpansionLimitModel_;

	@Override
	public void initialise() throws Exception {
		ProofBasedExplanationPreferences prefs = new ProofBasedExplanationPreferences()
				.load();
		add(buildRecursiveExpansionLimitComponent(
				prefs.recursiveExpansionLimit));
		add(Box.createVerticalGlue());
		add(buildResetComponent());
	}

	@Override
	public void dispose() throws Exception {
		// no op
	}

	@Override
	public void applyChanges() {
		ProofBasedExplanationPreferences prefs = new ProofBasedExplanationPreferences()
				.load();
		prefs.recursiveExpansionLimit = recursiveExpansionLimitModel_
				.getNumber().intValue();
		prefs.save();
	}

	private Component buildRecursiveExpansionLimitComponent(
			int recursiveExpansionLimit) {
		JPanel workersPane = new JPanel();
		workersPane.setLayout(new BoxLayout(workersPane, BoxLayout.LINE_AXIS));
		JLabel label = new JLabel("Recursive expansion limit:");
		recursiveExpansionLimitModel_ = new SpinnerNumberModel(
				recursiveExpansionLimit, 1, 99999, 1);
		JComponent spinner = new JSpinner(recursiveExpansionLimitModel_);
		spinner.setMaximumSize(spinner.getPreferredSize());
		workersPane.add(label);
		workersPane.add(Box.createRigidArea(new Dimension(10, 0)));
		workersPane.add(spinner);
		label.setLabelFor(spinner);
		String tooltip = "The maximal number of inferences expanded upon long press or alt + click";
		workersPane.setToolTipText(tooltip);
		spinner.setToolTipText(tooltip);
		workersPane.setAlignmentX(LEFT_ALIGNMENT);
		return workersPane;
	}

	private Component buildResetComponent() {
		JButton resetButton = new JButton(new AbstractAction() {
			private static final long serialVersionUID = 6257131701636338334L;

			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		resetButton.setText("Reset");
		resetButton.setToolTipText("Resets all settings to default values");

		return resetButton;
	}

	private void reset() {
		ProofBasedExplanationPreferences prefs = new ProofBasedExplanationPreferences()
				.reset();
		recursiveExpansionLimitModel_.setValue(prefs.recursiveExpansionLimit);
	}

}
