/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.eclipse.eclipsemonkey.ui.views.scriptsView;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.eclipsemonkey.StoredScript;

/**
 * @author Paul Colton
 */
public class ScriptActionSet implements IScriptActionSet {

	/*
	 * Fields
	 */
	private String _name;

	private ArrayList<IScriptAction> _scriptActions = new ArrayList<IScriptAction>();

	private boolean _executable = false;

	/*
	 * Constructors
	 */

	/**
	 * ActionSet
	 * 
	 * @param name
	 */
	public ScriptActionSet(String name) {
		this._name = name;
	}

	/*
	 * Properties
	 */

	/**
	 * setName
	 * 
	 * @param name
	 *        The name to set.
	 */
	@Override
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * getName
	 * 
	 * @return String
	 */
	@Override
	public String getName() {
		return this._name;
	}

	/**
	 * @return String
	 */
	public String getPath() {
		return "/" + this.getName() + "/";
	}

	/**
	 * getActionCount
	 * 
	 * @return int
	 */
	public int getActionCount() {
		return this._scriptActions.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.eclipsemonkey.ui.views.scriptsView.IScriptActionSet#getScriptActions()
	 */
	@Override
	public List<IScriptAction> getScriptActions() {
		return this._scriptActions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.eclipsemonkey.ui.views.scriptsView.IScriptActionSet#addScriptAction(java.lang.String, org.eclipse.eclipsemonkey.StoredScript)
	 */
	@Override
	public ScriptAction addScriptAction(String name, StoredScript script) {
		ScriptAction a = findScriptAction(name);

		if(a != null) {
			return a;
		}

		a = new ScriptAction(this, name, script);

		this._scriptActions.add(a);
		ScriptActionsManager.getInstance().fireScriptActionsChangeEvent(a);

		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.eclipsemonkey.ui.views.scriptsView.IScriptActionSet#removeScriptAction(java.lang.String)
	 */
	@Override
	public void removeScriptAction(String name) {
		for(int i = 0; i < this._scriptActions.size(); i++) {
			ScriptAction a = (ScriptAction)this._scriptActions.get(i);

			if(a.getName().equals(name)) {
				this._scriptActions.remove(i);
				ScriptActionsManager.getInstance().fireScriptActionsChangeEvent(a);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.eclipsemonkey.ui.views.scriptsView.IScriptActionSet#findScriptAction(java.lang.String)
	 */
	@Override
	public ScriptAction findScriptAction(String name) {
		for(int i = 0; i < this._scriptActions.size(); i++) {
			ScriptAction a = (ScriptAction)this._scriptActions.get(i);

			if(a.getName().equals(name)) {
				return a;
			}
		}

		return null;
	}

	/**
	 * isExecutable
	 * 
	 * @return boolean
	 */
	public boolean isExecutable() {
		return _executable;
	}

	/**
	 * setExecutable
	 * 
	 * @param b
	 */
	public void setExecutable(boolean b) {
		this._executable = b;
	}

}
