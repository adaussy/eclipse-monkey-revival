/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.eclipsemonkey;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory;
import org.osgi.framework.Bundle;

/**
 * Service to centralize all action about scripts
 * 
 * @author adaussy
 * 
 */
public class ScriptService {

	/**
	 * Store all the script found
	 */
	private static Map<URI, StoredScript> _scriptStore = new HashMap<URI, StoredScript>();

	/**
	 * Store all listener of Script stored
	 */
	private static Set<IScriptStoreListener> _storeListeners = new HashSet<IScriptStoreListener>();

	/**
	 * Store all handled langage
	 */
	private static Map<String, IMonkeyLanguageFactory> _languageStore = new HashMap<String, IMonkeyLanguageFactory>();

	/**
	 * Store all scope
	 */
	private static Map<String, Object> _scopeStore = new HashMap<String, Object>();

	private static class SingletonHolder {

		private static final ScriptService INSTANCE = new ScriptService();
	}

	public static ScriptService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public ScriptService() {
		loadLanguageSupport();
	}

	/**
	 * All loaded languages
	 * 
	 * @return as Immutable map of loaded languages
	 */
	public Map<String, IMonkeyLanguageFactory> getLanguageStore() {
		return java.util.Collections.unmodifiableMap(_languageStore);
	}

	/**
	 * All loaded scripts
	 * 
	 * @return as Immutable map of loaded scripts
	 */
	public Map<URI, StoredScript> getScriptStore() {
		return java.util.Collections.unmodifiableMap(_scriptStore);
	}

	/**
	 * All loaded scopes
	 * 
	 * @return an Immutable map of loaded scopes
	 */
	public Map<String, Object> getScopeStore() {
		return java.util.Collections.unmodifiableMap(_scopeStore);
	}

	/**
	 * @return Immutable Set of IScriptStoreListener
	 */
	public Set<IScriptStoreListener> get_storeListeners() {
		return Collections.unmodifiableSet(_storeListeners);
	}

	/**
	 * @param name
	 * @param script
	 */
	public void addScript(URI name, StoredScript script) {
		addScript(name, script, true);
	}

	public void addScript(URI name, StoredScript script, boolean notify) {
		/*
		 * we are using the full file path as the key into the store
		 * the consequence is that renames or moves are considered deletes and adds
		 * is this what we want?
		 */
		Map<URI, StoredScript> store = _scriptStore;
		StoredScript oldScript = (StoredScript)store.get(name);
		if(oldScript != null) {
			oldScript.metadata.unsubscribe();
		}
		store.put(name, script);
		script.metadata.subscribe();
		if(notify) {
			this.notifyScriptsChanged();
		}
	}

	/**
	 * @param name
	 */
	public void removeScript(URI name) {
		removeScript(name, true);
	}

	public void removeScript(URI name, boolean notify) {
		Map<URI, StoredScript> store = _scriptStore;
		StoredScript oldScript = (StoredScript)store.remove(name);
		if(oldScript == null)
			return;
		oldScript.metadata.unsubscribe();
		if(notify) {
			this.notifyScriptsChanged();
		}
	}

	/**
	 * 
	 */
	public void clearScripts() {
		for(Iterator<StoredScript> iter = _scriptStore.values().iterator(); iter.hasNext();) {
			StoredScript script = iter.next();
			script.metadata.unsubscribe();
		}
		_scriptStore.clear();
		this.notifyScriptsChanged();
	}

	/**
	 * 
	 */
	public void notifyScriptsChanged() {
		for(Iterator<IScriptStoreListener> iter = _storeListeners.iterator(); iter.hasNext();) {
			IScriptStoreListener element = (IScriptStoreListener)iter.next();
			element.storeChanged();
		}
	}

	/**
	 * @param listener
	 */
	public void addScriptStoreListener(IScriptStoreListener listener) {
		_storeListeners.add(listener);
	}

	/**
	 * @param listener
	 */
	public void removeScriptStoreListener(IScriptStoreListener listener) {
		_storeListeners.remove(listener);
	}

	/**
	 * loadLanguageSupport
	 * 
	 * @return String[]
	 */
	private Set<String> loadLanguageSupport() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint("org.eclipse.eclipsemonkey.language");

		if(point != null) {
			IExtension[] extensions = point.getExtensions();

			for(int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurations = extension.getConfigurationElements();

				for(int j = 0; j < configurations.length; j++) {
					IConfigurationElement element = configurations[j];
					try {
						IExtension declaring = element.getDeclaringExtension();

						//						String declaring_plugin_id = declaring
						//								.getDeclaringPluginDescriptor()
						//								.getUniqueIdentifier();

						String declaringPluginID = declaring.getNamespaceIdentifier();

						String languageName = element.getAttribute("languageName");
						String languageExtension = element.getAttribute("languageExtension");
						String[] languageExtensions = null;

						if(languageExtension != null) {
							languageExtensions = languageExtension.split("\\,");

							Object object = element.createExecutableExtension("class");

							IMonkeyLanguageFactory langFactory = (IMonkeyLanguageFactory)object;

							for(int k = 0; k < languageExtensions.length; k++) {
								_languageStore.put(languageExtensions[k], langFactory);
							}

							langFactory.init(declaringPluginID, languageName);
						}
					} catch (InvalidRegistryObjectException x) {
						// ignore bad extensions
					} catch (CoreException x) {
						// ignore bad extensions
					}
				}
			}
		}

		Set<String> extensions = getLanguageStore().keySet();

		if(extensions == null) {
			return java.util.Collections.emptySet();
		} else {
			return extensions;
		}
	}

	private static List<URI> alternateScriptsPaths = null;

	/**
	 * findAlternateScriptPaths
	 * 
	 * @return List of alternate paths to use to find scripts
	 */
	public List<URI> findAlternateScriptPaths() {
		if(alternateScriptsPaths == null) {
			alternateScriptsPaths = new ArrayList<URI>();

			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry.getExtensionPoint("org.eclipse.eclipsemonkey.scriptpath");

			if(point != null) {
				IExtension[] extensions = point.getExtensions();

				for(int i = 0; i < extensions.length; i++) {
					IExtension extension = extensions[i];
					IConfigurationElement[] configurations = extension.getConfigurationElements();

					for(int j = 0; j < configurations.length; j++) {
						IConfigurationElement element = configurations[j];
						try {
							IExtension declaring = element.getDeclaringExtension();

							String declaringPluginID = declaring.getDeclaringPluginDescriptor().getUniqueIdentifier();

							String fullPath = element.getAttribute("directory");

							Bundle b = Platform.getBundle(declaringPluginID);

							URL url = Platform.find(b, new Path(fullPath));
							if(url != null) {
								try {
									alternateScriptsPaths.add(url.toURI());
								} catch (URISyntaxException e) {
									e.printStackTrace();
								}
							}
						} catch (InvalidRegistryObjectException x) {
							// ignore bad extensions
						}
					}
				}
			}
		}
		return alternateScriptsPaths;

	}
}
