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

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.eclipsemonkey.integration.modeling.Activator;
import org.eclipse.eclipsemonkey.integration.modeling.ISelectionGetter;
import org.eclipse.emf.ecore.EObject;

/**
 * This manager handles the getSelection extension point to provide the selected model element from an editor's
 * selection
 * 
 * @author vhemery
 */
public class GetSelectionManager extends AbstractExtensionManager {

	/** the extension point for getting the selected model element from editor selection */
	private static final String GET_SELECTION_EXTENSION_POINT = Activator.PLUGIN_ID.concat(".getSelection"); //$NON-NLS-1$

	/** Value of the extension point attribute corresponding to the class. */
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$

	/** Value of the extension point attribute corresponding to the name. */
	private static final String ATT_NAME = "name"; //$NON-NLS-1$

	/** Value of the extension point attribute corresponding to the priority. */
	private static final String ATT_PRIORITY = "priority"; //$NON-NLS-1$

	/** Value for low priority. */
	private static final String LOW_PRIORITY = "LOW"; //$NON-NLS-1$

	/** Value for medium priority. */
	private static final String MEDIUM_PRIORITY = "MEDIUM"; //$NON-NLS-1$

	/** Value for high priority. */
	private static final String HIGH_PRIORITY = "HIGH"; //$NON-NLS-1$

	/** the shared instance */
	private static GetSelectionManager manager;

	/** Map with name prefixed by the priority as key and the associated getter as value. Sorted by descending priority. */
	public SortedMap<String, ISelectionGetter> mapClass;

	/**
	 * Private constructor
	 */
	private GetSelectionManager() {
		super(GET_SELECTION_EXTENSION_POINT);
		mapClass = new TreeMap<String, ISelectionGetter>(new Comparator<String>() {

			/**
			 * Compare the two strings according to their priority prefix.
			 * 
			 * @param arg0
			 *        first string
			 * @param arg1
			 *        second string
			 * @return -1 if arg0 is prefixed by a higher priority, +1 if arg0 is prefixed by a lower or same priority.
			 *         When the priority is the same, -1 or +1 or 0 is returned according to alphanumeric order.
			 */
			@Override
			public int compare(String arg0, String arg1) {
				return comparePriorityPrefixes(arg0, arg1);
			}
		});
		readRegistry();
	}

	/**
	 * Compare the two strings according to their priority prefix.
	 * 
	 * @param arg0
	 *        first string
	 * @param arg1
	 *        second string
	 * @return -1 if arg0 is prefixed by a higher priority, +1 if arg0 is prefixed by a lower or same priority. When the
	 *         priority is the same, -1 or +1 or 0 is returned according to alphanumeric order.
	 */
	public int comparePriorityPrefixes(String arg0, String arg1) {
		if(arg0.startsWith(HIGH_PRIORITY)) {
			if(arg1.startsWith(HIGH_PRIORITY)) {
				// same priority
				return arg0.compareTo(arg1);
			} else {
				// arg0 has higher priority
				return -1;
			}
		} else if(arg0.startsWith(MEDIUM_PRIORITY)) {
			if(arg1.startsWith(MEDIUM_PRIORITY)) {
				// same priority
				return arg0.compareTo(arg1);
			} else if(arg1.startsWith(HIGH_PRIORITY)) {
				// arg0 has lower priority
				return 1;
			} else {
				// arg0 has higher priority
				return -1;
			}
		} else if(arg0.startsWith(LOW_PRIORITY)) {
			if(arg1.startsWith(LOW_PRIORITY)) {
				// same priority
				return arg0.compareTo(arg1);
			} else if(arg1.startsWith(MEDIUM_PRIORITY) || arg1.startsWith(HIGH_PRIORITY)) {
				// arg0 has lower priority
				return 1;
			} else {
				// arg0 has higher priority
				return -1;
			}
		} else if(arg1.startsWith(LOW_PRIORITY) || arg1.startsWith(MEDIUM_PRIORITY) || arg1.startsWith(HIGH_PRIORITY)) {
			// arg0 has lower (undefined) priority
			return 1;
		} else {
			// same undefined priority
			return arg0.compareTo(arg1);
		}
	}

	/**
	 * Gets the shared instance.
	 * 
	 * @return the extension point manager
	 */
	public static GetSelectionManager getInstance() {
		if(manager == null) {
			manager = new GetSelectionManager();
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
			String name = confElt.getAttribute(ATT_NAME);
			String priority = confElt.getAttribute(ATT_PRIORITY);
			if(!LOW_PRIORITY.equals(priority) && !MEDIUM_PRIORITY.equals(priority) && !HIGH_PRIORITY.equals(priority)) {
				// replace with a valid priority
				priority = LOW_PRIORITY;
			}
			try {
				Object getter = confElt.createExecutableExtension(ATT_CLASS);
				if(getter instanceof ISelectionGetter) {
					mapClass.put(priority.concat(name), (ISelectionGetter)getter);
				}
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	/**
	 * @see org.eclipse.eclipsemonkey.integration.modeling.extensions.AbstractExtensionManager#removeExtension(org.eclipse.core.runtime.IExtension)
	 */
	@Override
	protected void removeExtension(IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for(IConfigurationElement confElt : elements) {
			String name = confElt.getAttribute(ATT_NAME);
			String priority = confElt.getAttribute(ATT_PRIORITY);
			if(!LOW_PRIORITY.equals(priority) && !MEDIUM_PRIORITY.equals(priority) && !HIGH_PRIORITY.equals(priority)) {
				// replace with a valid priority
				priority = LOW_PRIORITY;
			}
			mapClass.remove(priority.concat(name));
		}
	}

	/**
	 * Returns the currently selected model element, from the editor selected object.
	 * 
	 * @param element
	 *        the first object in editor selection
	 * @return the currently selected model element.
	 */
	public EObject getSelection(Object element) {
		for(ISelectionGetter getter : mapClass.values()) {
			EObject result = getter.getSelection(element);
			if(result != null) {
				return result;
			}
		}
		return null;
	}
}
