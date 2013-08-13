/*******************************************************************************
 * Copyright (c) 2005, 2006 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bjorn Freeman-Benson - initial implementation
 *     Ward Cunningham - initial implementation
 *******************************************************************************/

package org.eclipse.eclipsemonkey;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseMonkeyPlugin extends AbstractUIPlugin implements IStartup {

	/**
	 * Marker indicating the start of an Eclipse Monkey script
	 */
	public static final String PUBLISH_BEFORE_MARKER = "--- Came wiffling through the eclipsey wood ---";

	/**
	 * Marker indicating the end of an Eclipse Monkey script
	 */
	public static final String PUBLISH_AFTER_MARKER = "--- And burbled as it ran! ---";

	public static final String PLUGIN_ID = "org.eclipse.eclipsemonkey";

	// The shared instance.
	private static EclipseMonkeyPlugin plugin;

	private static Map<URI, StoredScript> _scriptStore = new HashMap<URI, StoredScript>();

	private static Set<IScriptStoreListener> _storeListeners = new HashSet<IScriptStoreListener>();

	private static Map<String, IMonkeyLanguageFactory> _languageStore = new HashMap<String, IMonkeyLanguageFactory>();

	private static Map<String, Object> _scopeStore = new HashMap<String, Object>();

	/**
	 * 
	 *
	 */
	public EclipseMonkeyPlugin() {
		plugin = this;
	}

	/**
	 * All loaded langagues
	 * 
	 * @return a map of loaded languages
	 */
	public Map<String, IMonkeyLanguageFactory> getLanguageStore() {
		return _languageStore;
	}

	/**
	 * All loaded scripts
	 * 
	 * @return a map of loaded scripts
	 */
	public Map<URI, StoredScript> getScriptStore() {
		return _scriptStore;
	}

	/**
	 * All loaded scopes
	 * 
	 * @return a map of loaded scopes
	 */
	public Map<String, Object> getScopeStore() {
		return _scopeStore;
	}

	/**
	 * This method is called upon plug-in activation
	 * 
	 * @param context
	 * @throws Exception
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 * 
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return EclipseMonkeyPlugin
	 */
	public static EclipseMonkeyPlugin getDefault() {
		return plugin;
	}

	/**
	 * @param path
	 * @return ImageDescriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.eclipsemonkey", path);
	}

	/**
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		ArrayList<String> extensions = Lists.newArrayList(loadLanguageSupport());
		String[] alternateScriptPaths = findAlternateScriptPaths();

		UpdateMonkeyActionsResourceChangeListener listener = new UpdateMonkeyActionsResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		try {
			listener.rescanAllFiles(extensions, alternateScriptPaths);
		} catch (CoreException e) {
			e.printStackTrace();
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Error searching for script", "An error occured during searching script in the workspace", new Status(Status.ERROR, EclipseMonkeyPlugin.PLUGIN_ID, e.getMessage()));
		}

		UpdateMonkeyActionsResourceChangeListener.setExtensions(extensions);
		UpdateMonkeyActionsResourceChangeListener.createTheMonkeyMenu();

		runStartupScripts();
	}

	/**
	 * @param name
	 * @param script
	 */
	public void addScript(URI name, StoredScript script) {
		/*
		 * we are using the full file path as the key into the store
		 * the consequence is that renames or moves are considered deletes and adds
		 * is this what we want?
		 */
		Map<URI, StoredScript> store = getScriptStore();
		StoredScript oldScript = (StoredScript)store.get(name);
		if(oldScript != null) {
			oldScript.metadata.unsubscribe();
		}
		store.put(name, script);
		script.metadata.subscribe();
		this.notifyScriptsChanged();
	}

	/**
	 * @param name
	 */
	public void removeScript(URI name) {
		Map<URI, StoredScript> store = getScriptStore();
		StoredScript oldScript = (StoredScript)store.remove(name);
		if(oldScript == null)
			return;
		oldScript.metadata.unsubscribe();
		this.notifyScriptsChanged();
	}

	/**
	 * 
	 */
	public void clearScripts() {
		for(Iterator<StoredScript> iter = getScriptStore().values().iterator(); iter.hasNext();) {
			StoredScript script = iter.next();
			script.metadata.unsubscribe();
		}
		getScriptStore().clear();
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
	 * runStartupScripts
	 */
	private void runStartupScripts() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				for(Iterator<StoredScript> iter = getDefault().getScriptStore().values().iterator(); iter.hasNext();) {
					StoredScript script = iter.next();
					String onLoadFunction = script.metadata.getOnLoadFunction();
					if(onLoadFunction != null) {
						MenuRunMonkeyScript runner = new MenuRunMonkeyScript(script.scriptPath);
						try {
							runner.run(onLoadFunction, new Object[0]);
						} catch (RunMonkeyException e) {
							// Do nothing
						}
					}
				}
			}
		});
	}

	/**
	 * findAlternateScriptPaths
	 * 
	 * @return List of alternate paths to use to find scripts
	 */
	private String[] findAlternateScriptPaths() {
		ArrayList<String> list = new ArrayList<String>();

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

						//						String declaringPluginID = declaring.getNamespaceIdentifier();

						String fullPath = element.getAttribute("directory");

						Bundle b = Platform.getBundle(declaringPluginID);

						URL url = Platform.find(b, new Path(fullPath));

						if(url != null) {
							try {

								URL localUrl = Platform.asLocalURL(url);

								if(localUrl != null) {
									String filename = localUrl.getFile();
									list.add(filename);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} catch (InvalidRegistryObjectException x) {
						// ignore bad extensions
					}
				}
			}
		}

		return (String[])list.toArray(new String[0]);
	}

	/**
	 * loadLanguageSupport
	 * 
	 * @return String[]
	 */
	private String[] loadLanguageSupport() {
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
								EclipseMonkeyPlugin.getDefault().getLanguageStore().put(languageExtensions[k], langFactory);
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

		String[] extensions = (String[])EclipseMonkeyPlugin.getDefault().getLanguageStore().keySet().toArray(new String[0]);

		if(extensions == null) {
			return new String[0];
		} else {
			return extensions;
		}
	}

}
