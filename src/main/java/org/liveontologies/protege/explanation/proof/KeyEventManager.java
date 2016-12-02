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

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import org.liveontologies.protege.explanation.proof.list.ProofFrameList;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.workspace.WorkspaceFrame;
import org.protege.editor.owl.OWLEditorKit;

/**
 * Forwards key events to the main Protege frame to react on shortcuts like
 * "Undo" and "Synchronize reasoner".
 * 
 * @author Yevgeny Kazakov
 *
 */
class KeyEventManager implements Disposable, KeyEventDispatcher {

	private static final String KEY_ = "org.liveontologies.protege.explanation.proof.keyevents";

	private final OWLEditorKit kit_;

	private final KeyboardFocusManager kfm_;

	private KeyEventManager(OWLEditorKit kit) {
		this.kit_ = kit;
		this.kfm_ = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kfm_.addKeyEventDispatcher(this);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getComponent() instanceof ProofFrameList) {
			WorkspaceFrame protegeFrame = ProtegeManager.getInstance()
					.getFrame(kit_.getWorkspace());
			if (protegeFrame == null) {
				return false;
			}
			KeyEvent copy = new KeyEvent(protegeFrame, e.getID(), e.getWhen(),
					e.getModifiers(), e.getKeyCode(), e.getKeyChar(),
					e.getKeyLocation());
			kfm_.dispatchEvent(copy);
		}
		return false;
	}

	public static synchronized void initialise(OWLEditorKit editorKit)
			throws Exception {
		// reuse one instance
		KeyEventManager m = editorKit.getModelManager().get(KEY_);
		if (m == null) {
			m = new KeyEventManager(editorKit);
			editorKit.put(KEY_, m);
		}
	}

	@Override
	public void dispose() throws Exception {
		kfm_.removeKeyEventDispatcher(this);
	}

}
