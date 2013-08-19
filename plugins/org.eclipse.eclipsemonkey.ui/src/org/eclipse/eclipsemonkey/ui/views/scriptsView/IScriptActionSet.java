/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.eclipsemonkey.ui.views.scriptsView;

import java.util.List;

import org.eclipse.eclipsemonkey.StoredScript;

public interface IScriptActionSet extends IScriptUI {

	/**
	 * getActions
	 * 
	 * @return Action[]
	 */
	public abstract List<IScriptAction> getScriptActions();

	/**
	 * addScriptAction
	 * 
	 * @param name
	 * @param script
	 * @return Action
	 */
	public abstract ScriptAction addScriptAction(String name, StoredScript script);

	/**
	 * removeScriptAction
	 * 
	 * @param name
	 */
	public abstract void removeScriptAction(String name);

	/**
	 * findScriptAction
	 * 
	 * @param name
	 * @return ScriptAction
	 */
	public abstract ScriptAction findScriptAction(String name);

}
