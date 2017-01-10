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

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

public class ProofBasedExplanationPreferences {

	private static final String PREFS_KEY_ = "PROOF_BASED_EXPLANATION_PREFS",
			RECURSIVE_EXPANSION_LIMIT_KEY_ = "RECURSIVE_EXPANSION_LIMIT",
			DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_KEY = "DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT";

	public final static String RECURSIVE_EXPANSION_LIMIT_DESCRIPTION = "The maximal number of inferences expanded upon long press or alt + click",
			DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_DESCRIPTION = "The maximal number of inferences displayed at once for each conclusion";

	private final static int DEFAULT_RECURSIVE_EXPANSION_LIMIT_ = 300; // inferences

	private final static int DEFAULT_DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_ = 5;

	/**
	 * {@value #RECURSIVE_EXPANSION_LIMIT_DESCRIPTION}
	 */
	public int recursiveExpansionLimit = DEFAULT_RECURSIVE_EXPANSION_LIMIT_; // inferences

	/**
	 * {@value #DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_DESCRIPTION}
	 */
	public int displayedInferencesPerConclusionLimit = DEFAULT_DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_;

	private ProofBasedExplanationPreferences() {

	}

	private static Preferences getPrefs() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		return prefMan.getPreferencesForSet(PREFS_KEY_,
				ProofBasedExplanationPreferences.class);
	}

	/**
	 * @return the preferences initialized with default values
	 */
	public static ProofBasedExplanationPreferences create() {
		return new ProofBasedExplanationPreferences();
	}

	public ProofBasedExplanationPreferences load() {
		Preferences prefs = getPrefs();
		recursiveExpansionLimit = prefs.getInt(RECURSIVE_EXPANSION_LIMIT_KEY_,
				DEFAULT_RECURSIVE_EXPANSION_LIMIT_);
		displayedInferencesPerConclusionLimit = prefs.getInt(
				DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_KEY,
				DEFAULT_DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_);
		return this;
	}

	public ProofBasedExplanationPreferences save() {
		Preferences prefs = getPrefs();
		prefs.putInt(RECURSIVE_EXPANSION_LIMIT_KEY_, recursiveExpansionLimit);
		prefs.putInt(DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_KEY,
				displayedInferencesPerConclusionLimit);
		return this;
	}

	public ProofBasedExplanationPreferences reset() {
		recursiveExpansionLimit = DEFAULT_RECURSIVE_EXPANSION_LIMIT_;
		displayedInferencesPerConclusionLimit = DEFAULT_DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_;
		return this;
	}

}
