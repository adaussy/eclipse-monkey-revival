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
package org.eclipse.eclipsemonkey.ui.views.providers;

import org.eclipse.eclipsemonkey.ui.IScriptAction;
import org.eclipse.eclipsemonkey.ui.IScriptUI;
import org.eclipse.eclipsemonkey.ui.data.ScriptAction;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * ViewerSorterProvider
 * 
 * @author Ingo Muschenetz
 * 
 */
public class ScriptsViewSorterProvider extends ViewerSorter {

	/**
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	@Override
	public int category(Object element) {
		if(element instanceof ScriptAction) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if(cat1 == cat2) {
			IScriptUI action1 = (IScriptUI)e1;
			IScriptUI action2 = (IScriptUI)e2;

			return action1.getName().compareTo(action2.getName());
		} else {
			return cat1 - cat2;
		}
	}
}
