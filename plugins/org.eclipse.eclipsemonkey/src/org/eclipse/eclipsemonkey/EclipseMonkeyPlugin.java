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

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

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

	public EclipseMonkeyPlugin() {
		plugin = this;
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
		Set<String> extensions = ScriptService.getInstance().getLanguageStore().keySet();
		List<URI> alternateScriptPaths = ScriptService.getInstance().findAlternateScriptPaths();

		UpdateMonkeyActionsResourceChangeListener listener = new UpdateMonkeyActionsResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		try {
			listener.rescanAllFiles(extensions, alternateScriptPaths, true);
		} catch (CoreException e) {
			e.printStackTrace();
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Error searching for script", "An error occured during searching script in the workspace", new Status(Status.ERROR, EclipseMonkeyPlugin.PLUGIN_ID, e.getMessage()));
		}

		UpdateMonkeyActionsResourceChangeListener.createTheMonkeyMenu();

		runStartupScripts();
	}

	/**
	 * runStartupScripts
	 */
	private void runStartupScripts() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				for(Iterator<StoredScript> iter = ScriptService.getInstance().getScriptStore().values().iterator(); iter.hasNext();) {
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

}
