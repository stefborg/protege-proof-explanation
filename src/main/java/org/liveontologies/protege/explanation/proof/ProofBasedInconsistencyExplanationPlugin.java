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

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.explanation.io.InconsistentOntologyPluginInstance;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A plugin to display proof-based explanation for ontology inconsistency
 * 
 * @author Yevgeny Kazakov
 *
 */
public class ProofBasedInconsistencyExplanationPlugin
		implements InconsistentOntologyPluginInstance {

	private OWLEditorKit kit_;

	private ProofServiceManager proofServiceMan_;

	private ImportsClosureManager importsClosureMan_;

	@Override
	public void setup(OWLEditorKit kit) {
		this.kit_ = kit;
	}

	@Override
	public void initialise() throws Exception {
		proofServiceMan_ = ProofServiceManager.get(kit_);
		importsClosureMan_ = ImportsClosureManager.get(kit_);
		KeyEventManager.initialise(kit_);
	}

	@Override
	public void dispose() throws Exception {
		// managers should dispose automatically by OWLEditorKit
	}

	@Override
	public void explain(OWLOntology ontology) {
		OWLDataFactory factory = ontology.getOWLOntologyManager()
				.getOWLDataFactory();

		final ProofBasedExplanationResult panel = new ProofBasedExplanationResult(
				proofServiceMan_, importsClosureMan_,
				factory.getOWLSubClassOfAxiom(factory.getOWLThing(),
						factory.getOWLNothing()));
		JOptionPane op = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.DEFAULT_OPTION);

		final JDialog dlg = op
				.createDialog("Inconsistent ontology explanation");

		dlg.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				panel.dispose();
				dlg.dispose();
			}
		});
		dlg.setModal(false);
		dlg.setResizable(true);
		dlg.setVisible(true);
	}

}
