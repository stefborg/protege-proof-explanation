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

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.core.ui.preferences.PreferencesPanel;

public class ProofBasedExplanationPreferencesPanel extends PreferencesPanel {

	private static final long serialVersionUID = 8585913940466665136L;

	private SpinnerNumberModel recursiveExpansionLimitModel_,
			displayedInferencesPerConclusionLimitModel_;

	@Override
	public void initialise() throws Exception {
		setLayout(new BorderLayout());
		PreferencesLayoutPanel panel = new PreferencesLayoutPanel();
		add(panel, BorderLayout.NORTH);
		addRecursiveExpansionLimitSettings(panel);
		addDisplayedInferencesPerConclusionLimitSettings(panel);
		panel.addGroup("");
		panel.addGroupComponent(buildResetComponent());
		loadFrom(ProofBasedExplanationPreferences.create().load());
	}

	@Override
	public void dispose() throws Exception {
		// no op
	}

	@Override
	public void applyChanges() {
		ProofBasedExplanationPreferences prefs = ProofBasedExplanationPreferences
				.create();
		saveTo(prefs);
		prefs.save();
	}

	private void loadFrom(ProofBasedExplanationPreferences prefs) {
		recursiveExpansionLimitModel_.setValue(prefs.recursiveExpansionLimit);
		displayedInferencesPerConclusionLimitModel_
				.setValue(prefs.displayedInferencesPerConclusionLimit);
	}

	private void saveTo(ProofBasedExplanationPreferences prefs) {
		prefs.recursiveExpansionLimit = recursiveExpansionLimitModel_
				.getNumber().intValue();
		prefs.displayedInferencesPerConclusionLimit = displayedInferencesPerConclusionLimitModel_
				.getNumber().intValue();
	}

	private void addRecursiveExpansionLimitSettings(
			PreferencesLayoutPanel panel) {
		panel.addGroup("Recursive expansion limit");
		recursiveExpansionLimitModel_ = new SpinnerNumberModel(1, 1, 9999, 1);
		JComponent spinner = new JSpinner(recursiveExpansionLimitModel_);
		spinner.setMaximumSize(spinner.getPreferredSize());
		panel.addGroupComponent(spinner);
		String tooltip = ProofBasedExplanationPreferences.RECURSIVE_EXPANSION_LIMIT_DESCRIPTION;
		spinner.setToolTipText(tooltip);
	}

	private void addDisplayedInferencesPerConclusionLimitSettings(
			PreferencesLayoutPanel panel) {
		panel.addGroup("Displayed inferences per conclusion limit");
		displayedInferencesPerConclusionLimitModel_ = new SpinnerNumberModel(1,
				1, 9999, 1);
		JComponent spinner = new JSpinner(
				displayedInferencesPerConclusionLimitModel_);
		displayedInferencesPerConclusionLimitModel_.setMaximum(999);
		spinner.setMaximumSize(spinner.getPreferredSize());
		panel.addGroupComponent(spinner);
		String tooltip = ProofBasedExplanationPreferences.DISPLAYED_INFERENCES_PER_CONCLUSION_DESCRIPTION;
		spinner.setToolTipText(tooltip);
	}

	private JComponent buildResetComponent() {
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> reset());
		resetButton.setToolTipText("Resets all settings to default values");

		return resetButton;
	}

	private void reset() {
		loadFrom(ProofBasedExplanationPreferences.create());
	}

}
