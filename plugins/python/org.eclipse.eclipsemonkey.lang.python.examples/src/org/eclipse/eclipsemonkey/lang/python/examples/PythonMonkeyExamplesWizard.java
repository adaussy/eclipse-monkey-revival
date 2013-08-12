package org.eclipse.eclipsemonkey.lang.python.examples;

import org.eclipse.eclipsemonkey.wizards.AbstractEclipseMonkeyNewSampleWizard;
import org.osgi.framework.Bundle;


public class PythonMonkeyExamplesWizard extends AbstractEclipseMonkeyNewSampleWizard {


	@Override
	protected Bundle getBundle() {
		return Activator.getDefault().getBundle();
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
