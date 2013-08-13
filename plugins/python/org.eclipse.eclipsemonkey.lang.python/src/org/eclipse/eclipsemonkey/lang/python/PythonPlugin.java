/*******************************************************************************
 * Copyright (c) 2007 José Fonseca
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     José Fonseca - initial implementation
 *******************************************************************************/

package org.eclipse.eclipsemonkey.lang.python;

import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.python.core.PySystemState;

/**
 * The activator class controls the plug-in life cycle
 */
public class PythonPlugin extends AbstractUIPlugin {

	/**
	 * The constructor
	 */
	public PythonPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

		Properties preProperties = System.getProperties();

		Properties postProperties = new Properties();
		postProperties.put("python.home", getPluginRootDir());

		PythonClassLoader classLoader = new PythonClassLoader();

		PySystemState.initialize(preProperties, postProperties, new String[0], classLoader);

		Bundle[] bundles = context.getBundles();
		for(int i = 0; i < bundles.length; ++i) {
			classLoader.addBundle(bundles[i]);
		}
	}

	private String getPluginRootDir() {
		try {
			Bundle bundle = getBundle();
			URL bundleURL = Platform.find(bundle, new Path("."));
			URL fileURL = Platform.asLocalURL(bundleURL);
			return fileURL.getPath();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
