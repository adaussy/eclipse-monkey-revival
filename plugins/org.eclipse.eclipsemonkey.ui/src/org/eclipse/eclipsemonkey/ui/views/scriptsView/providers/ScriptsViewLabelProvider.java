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
 *     Arthur Daussy - Contributor
 *******************************************************************************/
package org.eclipse.eclipsemonkey.ui.views.scriptsView.providers;

import org.eclipse.eclipsemonkey.ui.EclipseMonkeyUIActivator;
import org.eclipse.eclipsemonkey.ui.IconPath;
import org.eclipse.eclipsemonkey.ui.views.scriptsView.IScriptAction;
import org.eclipse.eclipsemonkey.ui.views.scriptsView.ScriptAction;
import org.eclipse.eclipsemonkey.ui.views.scriptsView.ScriptActionSet;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * ViewLabelProvider
 * 
 * @author Ingo Muschenetz
 * 
 */
public class ScriptsViewLabelProvider extends LabelProvider {

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if(element instanceof IScriptAction) {
			IScriptAction profile = (IScriptAction)element;
			return profile.getName();
		} else {
			return null;
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		if(obj instanceof ScriptActionSet) {
			ScriptActionSet set = (ScriptActionSet)obj;

			if(set.isExecutable()) {
				return EclipseMonkeyUIActivator.getLocalPluginIconImage(IconPath.PROFILE_DYNAMIC_ICON_PATH);
			} else {
				return EclipseMonkeyUIActivator.getLocalPluginIconImage(IconPath.PROFILE_ICON_PATH);
			}
		} else if(obj instanceof ScriptAction) {
			return EclipseMonkeyUIActivator.getLocalPluginIconImage(IconPath.PROFILE_FILE_ICON_PATH);
		} else {
			return null;
		}
	}
}
