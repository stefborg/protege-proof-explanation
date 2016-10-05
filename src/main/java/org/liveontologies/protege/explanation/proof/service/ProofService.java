package org.liveontologies.protege.explanation.proof.service;

import java.util.ArrayList;
import java.util.List;

import org.liveontologies.owlapi.proof.OWLProofNode;

/*
 * #%L
 * Proof-Based Explanations
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
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

import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A skeleton for a plugin that can provide proofs for OWL axioms
 * 
 * @author Yevgeny Kazakov
 *
 */
public abstract class ProofService implements ProtegePluginInstance {

	// logger for this class
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(ProofService.class);

	private OWLEditorKit kit_;
	private String pluginId_;
	private String name_;
	private final List<ChangeListener> listeners_ = new ArrayList<ChangeListener>();

	public ProofService setup(OWLEditorKit kit, String pluginId, String name) {
		this.kit_ = kit;
		this.pluginId_ = pluginId;
		this.name_ = name;
		return this;
	}

	public OWLEditorKit getEditorKit() {
		return kit_;
	}

	public String getPluginId() {
		return pluginId_;
	}

	public String getName() {
		return name_;
	}

	@Override
	public String toString() {
		return getName();
	}

	public synchronized void addListener(ChangeListener listener) {
		listeners_.add(listener);
	}

	public synchronized void removeListener(ChangeListener listener) {
		listeners_.remove(listener);
	}

	/**
	 * @param entailment
	 * @return {@code true} if this service can provide a proof for the given
	 *         entailment; the subsequent call of {@link #getProof(OWLAxiom)}
	 *         should return such a proof
	 */
	public abstract boolean hasProof(OWLAxiom entailment);

	/**
	 * @param entailment
	 * @return the root node of the proof for the given entailment
	 * @throws UnsupportedEntailmentTypeException
	 *             if checking entailment of the given axiom is not supported
	 */
	public abstract OWLProofNode getProof(OWLAxiom entailment)
			throws UnsupportedEntailmentTypeException;

	@Override
	public abstract void dispose();

	/**
	 * should be called when some of the previously returned result of
	 * {@link #getProof(OWLAxiom)} may be no longer up to date
	 */
	protected void fireProofChanged() {
		int i = 0;
		try {
			for (; i < listeners_.size(); i++) {
				listeners_.get(i).proofChanged();
			}
		} catch (Throwable e) {
			LOGGER_.warn("Remove the listener due to an exception", e);
			removeListener(listeners_.get(i));
		}
	}

	public static interface ChangeListener {

		/**
		 * signals that some proofs returned by
		 * {@link ProofService#getProof(OWLAxiom)} is no longer up to date
		 */
		void proofChanged();

	}

}
