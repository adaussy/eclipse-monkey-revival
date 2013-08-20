/*****************************************************************************
 * Copyright (c) 2011 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Vincent Hemery (Atos Origin) {vincent.hemery@atosorigin.com} - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.eclipsemonkey.integration.modeling.extensions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.eclipsemonkey.integration.modeling.Activator;

/**
 * This manager handles the scriptProjectNature extension point to provide the suitable project nature for scripts
 * created for a specific editor.
 * 
 * @author vhemery
 */
public class ScriptProjectNatureManager extends AbstractExtensionManager {

	/** the extension point for getting the selected model element from editor selection */
	private static final String GET_NATURE_EXTENSION_POINT = Activator.PLUGIN_ID.concat(".scriptProjectNature"); //$NON-NLS-1$

	/** Value of the extension point attribute corresponding to the editor name. */
	private static final String ATT_EDITOR_NAME = "editorName"; //$NON-NLS-1$

	/** Value of the extension point attribute corresponding to the nature id. */
	private static final String ATT_NATURE_ID = "natureId"; //$NON-NLS-1$

	/** the shared instance */
	private static ScriptProjectNatureManager manager;

	/** Map with nature id as key and the associated editor name as value. */
	public Map<String, String> mapNatureEditor;

	/**
	 * Private constructor
	 */
	private ScriptProjectNatureManager() {
		super(GET_NATURE_EXTENSION_POINT);
		mapNatureEditor = new HashMap<String, String>();
		readRegistry();
	}

	/**
	 * Gets the shared instance.
	 * 
	 * @return the extension point manager
	 */
	public static ScriptProjectNatureManager getInstance() {
		if(manager == null) {
			manager = new ScriptProjectNatureManager();
		}
		return manager;
	}

	/**
	 * @see org.eclipse.eclipsemonkey.integration.modeling.extensions.AbstractExtensionManager#addExtension(org.eclipse.core.runtime.IExtension)
	 */
	@Override
	protected void addExtension(IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for(IConfigurationElement confElt : elements) {
			String nature = confElt.getAttribute(ATT_NATURE_ID);
			String editor = confElt.getAttribute(ATT_EDITOR_NAME);
			mapNatureEditor.put(nature, editor);
		}
	}

	/**
	 * @see org.eclipse.eclipsemonkey.integration.modeling.extensions.AbstractExtensionManager#removeExtension(org.eclipse.core.runtime.IExtension)
	 */
	@Override
	protected void removeExtension(IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for(IConfigurationElement confElt : elements) {
			String nature = confElt.getAttribute(ATT_NATURE_ID);
			mapNatureEditor.remove(nature);
		}
	}

	/**
	 * Returns the scripting project natures and the editors which require them.
	 * 
	 * @return map with nature id as key and editor name as value.
	 */
	public Map<String, String> getNaturesForEditors() {
		// ensure original map is not modified
		return new HashMap<String, String>(mapNatureEditor);
	}
}
