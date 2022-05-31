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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.liveontologies.protege.explanation.proof.preferences.ProofBasedExplPrefs;
import org.liveontologies.protege.explanation.proof.service.ProofPlugin;
import org.liveontologies.protege.explanation.proof.service.ProofPluginLoader;
import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;

/**
 * Keeps track of the available {@link ProofService} plugins.
 * 
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 */
public class ProofServiceManager implements Disposable {

	private static final String KEY_ = "org.liveontologies.protege.explanation.proof.services";

	private final OWLEditorKit kit_;

	private final Collection<ProofService> services_;
	
	private final Collection<ProofService> enabledServices_;

	private ProofServiceManager(OWLEditorKit kit) throws Exception {
		this.kit_ = kit;
		this.services_ = new ArrayList<>();
		this.enabledServices_ = new ArrayList<>();
		reload();
	}

	public void reload() throws Exception {
		ProofPluginLoader loader = new ProofPluginLoader(kit_);
		// use TreeMap for alphabetical ordering
		Map<String, ProofService> sortedProofServices = new TreeMap<>();
		for (ProofPlugin plugin : loader.getPlugins()) {
			ProofService service = plugin.newInstance();
			service.initialise();
			sortedProofServices.put(service.getPluginId(), service);
		}

		// add ProofServices in the order defined in the preferences
		final ProofBasedExplPrefs prefs = ProofBasedExplPrefs.create().load();
		services_.clear();
		for (String id : prefs.proofServicesList) {
			ProofService service = sortedProofServices.get(id);
			if (service != null) {
				services_.add(service);
				sortedProofServices.remove(id);
			}
		}
		
		if (!sortedProofServices.isEmpty()) {
			// add new ProofServices (which do not occur in the preferences yet) in
			// alphabetical order at the end
			for (ProofService service: sortedProofServices.values()) {
				services_.add(service);
			}
		}
		
		// update preferences according to current list (adding new and removing old
		// ProofServices)
		prefs.proofServicesList = new ArrayList<>();
		for (ProofService service : services_) {
			prefs.proofServicesList.add(service.getPluginId());
		}
		prefs.save();
		
		enabledServices_.clear();
		for (ProofService service : services_) {
			if (!prefs.disabledProofServices.contains(service.getPluginId())) {
				enabledServices_.add(service);
			}
		}
	}

	public static synchronized ProofServiceManager get(OWLEditorKit editorKit)
			throws Exception {
		// reuse one instance
		ProofServiceManager m = editorKit.getModelManager().get(KEY_);
		if (m == null) {
			m = new ProofServiceManager(editorKit);
			editorKit.put(KEY_, m);
		}
		return m;
	}

	@Override
	public void dispose() {
		for (ProofService proofService : services_) {
			proofService.dispose();
		}
	}

	public OWLEditorKit getOWLEditorKit() {
		return kit_;
	}

	public Collection<ProofService> getProofServices() {
		return services_;
	}
	
	public Collection<ProofService> getEnabledProofServices() {
		return enabledServices_;
	}

}
