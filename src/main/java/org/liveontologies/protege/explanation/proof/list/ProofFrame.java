/**
 * 
 */
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.liveontologies.owlapi.proof.util.ProofNode;
import org.liveontologies.protege.explanation.proof.OWLRenderer;
import org.liveontologies.protege.explanation.proof.ProofManager;
import org.liveontologies.protege.explanation.proof.editing.OWLAxiomChecker;
import org.liveontologies.protege.explanation.proof.editing.OWLAxiomEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameListener;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link OWLFrame} to manage a proof as a list consisting of inferences and
 * OWL axioms (premises and conclusions of such inferences). Inferences are
 * represented using an {@link OWLFrameSection} and axioms using an
 * {@link OWLFrameSectionRow}.
 * 
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 */
public class ProofFrame implements OWLFrame<ProofRoot> {

	private final static Logger LOGGER_ = LoggerFactory
			.getLogger(ProofFrame.class);

	private final ProofManager man_;

	private final OWLEditorKit kit_;

	private final OWLRenderer renderer_;

	private final OWLAxiomChecker checker_;

	private final OWLObjectEditor<OWLAxiom> editor_;

	private ProofRoot root_;

	private InferenceSection rootSection_;

	private final List<OWLFrameListener> listeners_ = new ArrayList<OWLFrameListener>(
			2);

	public ProofFrame(ProofManager man, final OWLEditorKit kit) {
		this.man_ = man;
		this.kit_ = kit;
		this.renderer_ = new OWLRenderer() {

			@Override
			public String render(OWLObject obj) {
				return kit.getOWLModelManager().getRendering(obj);
			}
		};
		this.checker_ = new OWLAxiomChecker(kit_.getModelManager());
		this.editor_ = new OWLAxiomEditor(kit_, checker_);
		updateProof();
	}

	ProofManager getWorkbenchManager() {
		return man_;
	}

	OWLAxiomChecker getAxiomChecker() {
		return checker_;
	}

	OWLEditorKit getEditorKit() {
		return kit_;
	}

	OWLRenderer getRenderer() {
		return renderer_;
	}

	OWLObjectEditor<OWLAxiom> getAxiomEditor() {
		return editor_;
	}

	InferenceSection getRootSection() {
		if (rootSection_ == null) {
			this.rootSection_ = new InferenceSection(this, root_);
		}
		return rootSection_;
	}

	public void updateProof() {
		ProofNode<OWLAxiom> newProof = man_.getProofRoot();
		setRootObject(new ProofRoot(man_.getEntailment(),
				newProof == null ? Collections.<ProofNode<OWLAxiom>> emptyList()
						: Collections.singletonList(newProof),
				renderer_));
	}

	@Override
	public void dispose() {
		if (rootSection_ != null) {
			rootSection_.dispose();
		}
		editor_.dispose();
	}

	@Override
	public void setRootObject(ProofRoot newRoot) {
		if (root_ == newRoot) {
			// already set;
			return;
		}
		root_ = newRoot;
		InferenceSection previousRootSection = rootSection_;
		rootSection_ = null;
		if (previousRootSection != null) {
			getRootSection().getRow()
					.copySettingsFrom(previousRootSection.getRow());
			previousRootSection.dispose();
		}
		fireContentChanged();
	}

	@Override
	public ProofRoot getRootObject() {
		return root_;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<OWLFrameSection> getFrameSections() {
		return Collections.<OWLFrameSection> singletonList(getRootSection());
	}

	@Override
	public synchronized void addFrameListener(OWLFrameListener listener) {
		listeners_.add(listener);
	}

	@Override
	public synchronized void removeFrameListener(OWLFrameListener listener) {
		listeners_.remove(listener);
	}

	@Override
	public synchronized void fireContentChanged() {
		int i = 0;
		try {
			for (; i < listeners_.size(); i++) {
				listeners_.get(i).frameContentChanged();
			}
		} catch (Throwable e) {
			LOGGER_.warn("Remove the listener due to an exception", e);
			removeFrameListener(listeners_.get(i));
		}
	}

}
