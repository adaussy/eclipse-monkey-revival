/**
 * Copyright (c) 2013 Atos
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 * Contributors :
 * 	Arthur Daussy
 */
package org.eclipse.eclipsemonkey.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class EclipseMonkeyUIActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.eclipsemonkey.ui"; //$NON-NLS-1$

	// The shared instance
	private static EclipseMonkeyUIActivator plugin;

	/**
	 * The constructor
	 */
	public EclipseMonkeyUIActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static EclipseMonkeyUIActivator getDefault() {
		return plugin;
	}

	/**
	 * 
	 * This method returns an <code>org.eclipse.swt.graphics.Image</code> identified by its pluginId and iconPath.<BR>
	 */
	public static Image getPluginIconImage(String pluginId, String iconPath) {
		String key = pluginId + iconPath;
		ImageRegistry registry = getDefault().getImageRegistry();
		Image image = registry.get(key);
		if(image == null) {
			ImageDescriptor desc = getImageDescriptor(pluginId, iconPath);
			registry.put(key, desc);
			image = registry.get(key);
		}
		return image;
	}

	public static Image getLocalPluginIconImage(String iconPath) {
		return getPluginIconImage(PLUGIN_ID, iconPath);
	}

	public static ImageDescriptor getImageDescriptor(String pluginId, String iconPath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, iconPath);
	}

	public static ImageDescriptor getLocalImageDescriptor(String iconPath) {
		return getImageDescriptor(PLUGIN_ID, iconPath);
	}

}
