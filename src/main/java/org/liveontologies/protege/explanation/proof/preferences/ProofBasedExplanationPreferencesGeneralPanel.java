package org.liveontologies.protege.explanation.proof.preferences;

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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import org.liveontologies.protege.explanation.proof.ProofServiceManager;
import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.explanation.SortedPluginsTableModel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProofBasedExplanationPreferencesGeneralPanel extends OWLPreferencesPanel {

	private final Logger logger = LoggerFactory.getLogger(ProofBasedExplanationPreferencesGeneralPanel.class);
	
	private static final long serialVersionUID = 8585913940466665136L;

	private SpinnerNumberModel recursiveExpansionLimitModel_,
			displayedInferencesPerConclusionLimitModel_;

	private JCheckBox removeUnnecessaryInferences_;
	
	private SortedPluginsTableModel tableModel_;

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
		loadFrom(ProofBasedExplPrefs.create().load());
	}

	@Override
	public void dispose() throws Exception {
		// no op
	}

	@Override
	public void applyChanges() {
		ProofBasedExplPrefs prefs = ProofBasedExplPrefs
				.create();
		saveTo(prefs);
		prefs.save();
		try {
			ProofServiceManager.get(getOWLEditorKit()).reload();
		} catch (Exception e) {
			logger.error("An error occurred while reloading proof service plugins.", e);
		}
	}

	private void loadFrom(ProofBasedExplPrefs prefs) {
		recursiveExpansionLimitModel_.setValue(prefs.recursiveExpansionLimit);
		displayedInferencesPerConclusionLimitModel_
				.setValue(prefs.displayedInferencesPerConclusionLimit);
		removeUnnecessaryInferences_
				.setSelected(prefs.removeUnnecessaryInferences);
		tableModel_.setPluginIds(prefs.proofServicesList);
		tableModel_.setDisabledIds(prefs.disabledProofServices);
	}

	private void saveTo(ProofBasedExplPrefs prefs) {
		prefs.recursiveExpansionLimit = recursiveExpansionLimitModel_
				.getNumber().intValue();
		prefs.displayedInferencesPerConclusionLimit = displayedInferencesPerConclusionLimitModel_
				.getNumber().intValue();
		prefs.removeUnnecessaryInferences = removeUnnecessaryInferences_
				.isSelected();
		prefs.proofServicesList = tableModel_.getPluginIds();
		prefs.disabledProofServices = tableModel_.getDisabledIds();
	}

	private void addInstalledProofServicesComponent(
			PreferencesLayoutPanel panel) throws Exception {
		panel.addGroup("Installed proof services");
		Collection<ProofService> proofServices = ProofServiceManager
				.get(getOWLEditorKit()).getProofServices();
		Map<String, String> nameMap = new HashMap<>();
		for (ProofService proofService : proofServices) {
			nameMap.put(proofService.getPluginId(), proofService.getName());
		}
		tableModel_ = new SortedPluginsTableModel(nameMap);
		JTable proofServicesTable = new JTable(tableModel_);
		proofServicesTable.setToolTipText(
				"Plugins that provide proofs that are displayed for explanation of entailments. You can disable and enable plugins and change their order using the buttons below.");
		proofServicesTable.setRowSelectionAllowed(true);
		proofServicesTable.setColumnSelectionAllowed(false);
		proofServicesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		proofServicesTable.getColumnModel().getColumn(0).setMaxWidth(20);
		proofServicesTable.getColumnModel().getColumn(1).setMaxWidth(50);
		proofServicesTable.getColumnModel().getColumn(2).setMinWidth(300);
		proofServicesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane pluginTableScrollPane = new JScrollPane(proofServicesTable);
		pluginTableScrollPane.setPreferredSize(new Dimension(400, 100));
		panel.addGroupComponent(pluginTableScrollPane);
		addUpDownButtons(panel, proofServicesTable);
		panel.addHelpText("<html><p style=\"width:400pt\">The general explanation preferences determine which plugin is used by default when an explanation is requested (either the most recently used one or the first one from the list).</p></html>");
	}

	private void addUpDownButtons(PreferencesLayoutPanel panel, JTable proofServicesTable) {
		JButton buttonUp = new JButton("↑ Move up");
		buttonUp.setToolTipText("Move the selected proof service towards the top of the list");
		buttonUp.addActionListener(e -> {
			int rowIndex = proofServicesTable.getSelectedRow();
			if (rowIndex > 0) {
				tableModel_.swap(rowIndex - 1, rowIndex);
			}
			proofServicesTable.setRowSelectionInterval(rowIndex - 1, rowIndex - 1);
		});

		JButton buttonDown = new JButton("↓ Move down︎");
		buttonDown.setToolTipText("Move the selected proof service towards the bottom of the list");
		buttonDown.addActionListener(e -> {
			int rowIndex = proofServicesTable.getSelectedRow();
			if (rowIndex < proofServicesTable.getRowCount() - 1) {
				tableModel_.swap(rowIndex, rowIndex + 1);
			}
			proofServicesTable.setRowSelectionInterval(rowIndex + 1, rowIndex + 1);
		});

		JPanel buttonsUpDown = new JPanel();
		buttonsUpDown.add(buttonUp);
		buttonsUpDown.add(buttonDown);
		panel.addGroupComponent(buttonsUpDown);

		proofServicesTable.getSelectionModel().addListSelectionListener(e -> {
			int rowIndex = proofServicesTable.getSelectedRow();
			if (rowIndex == -1) {
				buttonUp.setEnabled(false);
				buttonDown.setEnabled(false);
			} else {
				buttonUp.setEnabled(rowIndex > 0);
				buttonDown.setEnabled(rowIndex < proofServicesTable.getRowCount() - 1);
			}
		});
	}
	
	private void addRecursiveExpansionLimitSettings(
			PreferencesLayoutPanel panel) {
		panel.addGroup("Recursive expansion limit");
		recursiveExpansionLimitModel_ = new SpinnerNumberModel(1, 1, 9999, 1);
		JComponent spinner = new JSpinner(recursiveExpansionLimitModel_);
		spinner.setMaximumSize(spinner.getPreferredSize());
		panel.addGroupComponent(spinner);
		String tooltip = ProofBasedExplPrefs.RECURSIVE_EXPANSION_LIMIT_DESCRIPTION;
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
		String tooltip = ProofBasedExplPrefs.DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_DESCRIPTION;
		spinner.setToolTipText(tooltip);
	}

	private void addRemoveUnnecessaryInferencesSettings(
			PreferencesLayoutPanel panel) {
		removeUnnecessaryInferences_ = new JCheckBox(
				"Remove unnecessary inferences");
		panel.addGroupComponent(removeUnnecessaryInferences_);
		removeUnnecessaryInferences_.setToolTipText(
				ProofBasedExplPrefs.REMOVE_UNNECESSARY_INFERENCES_DESCRIPTION);
	}

	private JComponent buildResetComponent() {
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> reset());
		resetButton.setToolTipText("Resets all settings to default values");

		return resetButton;
	}

	private void reset() {
		loadFrom(ProofBasedExplPrefs.create());
	}

}
