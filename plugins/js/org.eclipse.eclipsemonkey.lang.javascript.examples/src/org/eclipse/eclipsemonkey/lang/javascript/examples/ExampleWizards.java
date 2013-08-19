/**
 * Copyright (c) 2013 Atos
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 * 
 *  * Contributors:
 *     Arthur Daussy - initial implementation
 */
package org.eclipse.eclipsemonkey.lang.javascript.examples;

import org.eclipse.eclipsemonkey.wizards.AbstractEclipseMonkeyNewSampleWizard;
import org.eclipse.ui.INewWizard;
import org.osgi.framework.Bundle;

/**
 * Wizard to create the default eclipse monkey scrip
 * 
 * @author adaussy
 * 
 */
public class ExampleWizards extends AbstractEclipseMonkeyNewSampleWizard implements INewWizard {

	public ExampleWizards() {
	}

	@Override
	protected String getManifestPath() {
		return "samples/manifest.txt";
	}

	@Override
	protected String getScriptContainerFolder() {
		return "samples/";
	}

	@Override
	protected Bundle getBundle() {
		return Activator.getDefault().getBundle();
	}

	@Override
	protected String getInitalProjectName() {
		return "Eclipse Monkey Java Script Example";
	}

}
