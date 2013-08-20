/*******************************************************************************
 * Copyright (c) 2005 AIRBUS FRANCE. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Thomas Friol (Anyware Technologies) - initial API and
 * implementation
 ******************************************************************************/
package org.eclipse.eclipsemonkey.integration.modeling.extensions;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

/**
 * An abstract implementation of en extension manager.<br>
 * This manager can initialize itself iterating on all the registered extensions
 * of a given extension point id.<br>
 * It also listens to the platform extension registry changes and makes the
 * appropriate modifications.<br>
 * Clients must implements the <code>addExtension()</code> and the <code>removeExtension()</code> methods in order to define the subclassing
 * manager behavior.<br>
 * Creation : 24 nov. 2005
 * 
 * @author <a href="mailto:thomas@anyware-tech.com">Thomas Friol</a>
 */
public abstract class AbstractExtensionManager implements IRegistryChangeListener {

	/**
	 * The manager extension point id.
	 */
	private String extensionPointId;

	/**
	 * Constructor.
	 * 
	 * @param extensionPointId
	 *        the unique id of the managed extension point
	 *        (e.g., <code>"org.eclipse.core.resources.builders"</code>)
	 */
	protected AbstractExtensionManager(String extensionPointId) {
		if(extensionPointId == null || extensionPointId.length() == 0) {
			throw new IllegalArgumentException("extensionPointId cannot be null or empty.");
		}

		this.extensionPointId = extensionPointId;
		Platform.getExtensionRegistry().addRegistryChangeListener(this);
	}

	/**
	 * Disposes this manager.
	 */
	public void dispose() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}

	/**
	 * Adds the given extension to this manager.
	 * 
	 * @param extension
	 *        a registered extension
	 */
	protected abstract void addExtension(IExtension extension);

	/**
	 * Removes the given extension from this manager.
	 * 
	 * @param extension
	 *        a unregistered extension
	 */
	protected abstract void removeExtension(IExtension extension);

	/**
	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	@Override
	public void registryChanged(IRegistryChangeEvent event) {
		if(!Platform.isRunning()) {
			return;
		}

		// Retrieve any changes relating to the extension point id dealed by
		// this manager
		IExtensionDelta[] delta = event.getExtensionDeltas(extensionPointId);
		for(int i = 0; i < delta.length; i++) {
			switch(delta[i].getKind()) {
			case IExtensionDelta.ADDED:
				addExtension(delta[i].getExtension());
				break;
			case IExtensionDelta.REMOVED:
				removeExtension(delta[i].getExtension());
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Reads the extension registry and add all the registered extensions for
	 * the managed extension point.
	 */
	protected void readRegistry() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(extensionPointId);
		IExtension[] extensions = point.getExtensions();

		for(int i = 0; i < extensions.length; i++) {
			addExtension(extensions[i]);
		}

	}
}
