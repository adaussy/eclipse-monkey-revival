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
package org.eclipse.eclipsemonkey.ui;

import java.util.Collection;
import java.util.List;

import org.eclipse.eclipsemonkey.StoredScript;
import org.eclipse.eclipsemonkey.ui.data.ScriptAction;

public interface IScriptActionSet extends IScriptUI {

	/**
	 * getActions
	 * 
	 * @return Action[]
	 */
	public List<IScriptAction> getScriptActions();

	public Collection<IScriptActionSet> getSubSet();

	public IScriptActionSet addSubActionSet(String setName);

	public void removeSubActionSet(String name);

	public IScriptActionSet findSubSet(String name);

	/**
	 * addScriptAction
	 * 
	 * @param name
	 * @param script
	 * @return Action
	 */
	public ScriptAction addScriptAction(String name, StoredScript script);

	/**
	 * removeScriptAction
	 * 
	 * @param name
	 */
	public void removeScriptAction(String name);

	/**
	 * findScriptAction
	 * 
	 * @param name
	 * @return ScriptAction
	 */
	public ScriptAction findScriptAction(String name);

}
