/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.eclipse.eclipsemonkey.ui.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.eclipsemonkey.ui.IScriptUI;

/**
 * @author Paul Colton
 */
public class ScriptActionsViewEvent {

	/*
	 * Fields
	 */
	private int _eventType = -1;

	private List<IScriptUI> _scriptActions = new ArrayList<IScriptUI>();

	private IPath[] _paths = null;

	private String _name = null;

	/*
	 * Properties
	 */

	/**
	 * getPaths
	 * 
	 * @return Returns the paths.
	 */
	public IPath[] getPaths() {
		return this._paths;
	}

	/**
	 * setPaths
	 * 
	 * @param paths
	 *        The paths to set.
	 */
	public void setPaths(IPath[] paths) {
		this._paths = paths;
	}

	/**
	 * getEventType
	 * 
	 * @return int
	 */
	public int getEventType() {
		return this._eventType;
	}

	/**
	 * getActions
	 * 
	 * @return Action[]
	 */
	public List<IScriptUI> getUIScript() {
		return this._scriptActions;
	}

	//	/**
	//	 * setActions
	//	 * 
	//	 * @param actions
	//	 */
	//	public void setActions(IScriptUI[] actions) {
	//		this._scriptActions = actions;
	//	}

	/**
	 * getName
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return this._name;
	}

	/**
	 * setName
	 * 
	 * @param name
	 *        The name to set.
	 */
	public void setName(String name) {
		this._name = name;
	}

	/*
	 * Constructor
	 */

	/**
	 * ActionsViewEvent
	 * 
	 * @param eventType
	 */
	public ScriptActionsViewEvent(int eventType) {
		this._eventType = eventType;
	}
}
