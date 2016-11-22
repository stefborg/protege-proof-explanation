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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.liveontologies.protege.explanation.proof.list.ProofFrame;
import org.liveontologies.protege.explanation.proof.list.ProofFrameList;
import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.explanation.ExplanationResult;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * The component that displays a proof-based explanation for an entailment
 * 
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 *
 */
public class ProofBasedExplanationResult extends ExplanationResult
		implements ProofManager.ChangeListener {

	private static final long serialVersionUID = -4072183414834233365L;

	private final ProofManager proofManager_;

	private final OWLEditorKit kit_;

	private final ProofFrame frame_;

	private final ProofFrameList frameList_;

	private final JScrollPane scrollPane;

	private ProofBasedExplanationResult(ProofManager proofManager) {
		setLayout(new BorderLayout());
		this.proofManager_ = proofManager;
		this.kit_ = proofManager.getOWLEditorKit();

		Collection<ProofService> proofServices = proofManager_
				.getProofServices();
		switch (proofServices.size()) {
		case 0:
			break;
		case 1:
			proofManager_.setProofService(proofServices.iterator().next());
			break;
		default:
			JComboBox<ProofService> proofServiceSelector = createComboBox(
					proofServices);
			add(proofServiceSelector, BorderLayout.NORTH);
		}
		proofManager_.addListener(this);
		frame_ = new ProofFrame(proofManager_, kit_);
		frameList_ = new ProofFrameList(kit_, frame_);
		scrollPane = new JScrollPane(frameList_);
		scrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

	}

	public ProofBasedExplanationResult(ProofServiceManager proofServiceMan,
			ImportsClosureManager importsClosureMan, OWLAxiom entailment) {
		this(new ProofManager(proofServiceMan,
				importsClosureMan.getImportsClosure(
						proofServiceMan.getOWLEditorKit().getModelManager()
								.getActiveOntology()),
				entailment));
	}

	@Override
	public void dispose() {
		frame_.dispose();
		frameList_.dispose();
		proofManager_.removeListener(this);
		proofManager_.dispose();
	}

	private JComboBox<ProofService> createComboBox(
			Collection<ProofService> proofServices) {
		final ProofService[] services = proofServices
				.toArray(new ProofService[proofServices.size()]);
		final JComboBox<ProofService> selector = new JComboBox<ProofService>(
				services);
		if (services.length > 0) {
			selector.setSelectedItem(services[0]);
			proofManager_.setProofService(services[0]);
		}
		selector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				proofManager_.setProofService(
						(ProofService) selector.getSelectedItem());
			}
		});
		return selector;
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(10, 10);
	}

	public void updateProofRoot() {
		frame_.updateProof();
		scrollPane.validate();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension workspaceSize = kit_.getWorkspace().getSize();
		int width = (int) (workspaceSize.getWidth() * 0.8);
		int height = (int) (workspaceSize.getHeight() * 0.7);
		return new Dimension(width, height);
	}

	@Override
	public void proofRootChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateProofRoot();
			}
		});
	}

}
