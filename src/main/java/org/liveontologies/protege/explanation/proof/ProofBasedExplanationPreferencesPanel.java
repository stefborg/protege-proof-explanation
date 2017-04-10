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
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

public class ProofBasedExplanationPreferencesPanel extends OWLPreferencesPanel {

	private static final long serialVersionUID = 8585913940466665136L;

	private SpinnerNumberModel recursiveExpansionLimitModel_,
			displayedInferencesPerConclusionLimitModel_;

	private JCheckBox removeUnnecessaryInferences_;

	@Override
	public void initialise() throws Exception {
		setLayout(new BorderLayout());
		PreferencesLayoutPanel panel = new PreferencesLayoutPanel();
		add(panel, BorderLayout.NORTH);
		addInstalledProofServicesComponent(panel);
		addRecursiveExpansionLimitSettings(panel);
		addDisplayedInferencesPerConclusionLimitSettings(panel);
		addRemoveUnnecessaryInferencesSettings(panel);
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
		removeUnnecessaryInferences_
				.setSelected(prefs.removeUnnecessaryInferences);
	}

	private void saveTo(ProofBasedExplanationPreferences prefs) {
		prefs.recursiveExpansionLimit = recursiveExpansionLimitModel_
				.getNumber().intValue();
		prefs.displayedInferencesPerConclusionLimit = displayedInferencesPerConclusionLimitModel_
				.getNumber().intValue();
		prefs.removeUnnecessaryInferences = removeUnnecessaryInferences_
				.isSelected();
	}

	private void addInstalledProofServicesComponent(
			PreferencesLayoutPanel panel) throws Exception {
		panel.addGroup("Installed proof services");
		DefaultListModel<ProofService> proofServicesModel = new DefaultListModel<>();
		Collection<ProofService> proofServices = ProofServiceManager
				.get(getOWLEditorKit()).getProofServices();
		for (ProofService proofService : proofServices) {
			proofServicesModel.addElement(proofService);
		}
		JList<ProofService> proofServicesList = new JList<>(proofServicesModel);
		proofServicesList.setToolTipText(
				"Plugins that provide proofs that are displayed for explanation of entailments");
		JScrollPane pluginInfoScrollPane = new JScrollPane(proofServicesList);
		pluginInfoScrollPane.setPreferredSize(new Dimension(300, 100));
		panel.addGroupComponent(pluginInfoScrollPane);
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
		panel.addGroup("Displayed inferences per conclusion");
		displayedInferencesPerConclusionLimitModel_ = new SpinnerNumberModel(1,
				1, 9999, 1);
		JComponent spinner = new JSpinner(
				displayedInferencesPerConclusionLimitModel_);
		displayedInferencesPerConclusionLimitModel_.setMaximum(999);
		spinner.setMaximumSize(spinner.getPreferredSize());
		panel.addGroupComponent(spinner);
		String tooltip = ProofBasedExplanationPreferences.DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_DESCRIPTION;
		spinner.setToolTipText(tooltip);
	}

	private void addRemoveUnnecessaryInferencesSettings(
			PreferencesLayoutPanel panel) {
		removeUnnecessaryInferences_ = new JCheckBox(
				"Remove unnecessary inferences");
		panel.addGroupComponent(removeUnnecessaryInferences_);
		removeUnnecessaryInferences_.setToolTipText(
				ProofBasedExplanationPreferences.REMOVE_UNNECESSARY_INFERENCES_DESCRIPTION);
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
