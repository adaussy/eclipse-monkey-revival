/*******************************************************************************
 * Copyright (c) 2008 AIRBUS FRANCE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Charles David (Obeo) - initial API and implementation
 *******************************************************************************/
package org.eclipse.eclipsemonkey.integration.modeling.uml;

import org.eclipse.eclipsemonkey.wizards.AbstractEclipseMonkeyNewSampleWizard;
import org.osgi.framework.Bundle;

/**
 * A creation wizard to create a new project in the user's workspace with sample UML scripts.
 * 
 * @author <a href="mailto:pierre-charles.david@obeo.fr">Pierre-Charles David</a>
 */
public class SamplesProjectWizard extends AbstractEclipseMonkeyNewSampleWizard {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.eclipsemonkey.integration.modeling.uml.AbstractSamplesProjectWizard#getBundle()
	 */
	@Override
	protected Bundle getBundle() {
		return org.eclipse.eclipsemonkey.integration.modeling.uml.Activator.getDefault().getBundle();
	}

	@Override
	protected String getInitalProjectName() {
		return "Eclipse Monkey Example UML Scripts";
	}

	@Override
	protected String getManifestPath() {
		return "samples/manifest.txt";
	}

	@Override
	protected String getScriptContainerFolder() {
		return "samples/";
	}
}
