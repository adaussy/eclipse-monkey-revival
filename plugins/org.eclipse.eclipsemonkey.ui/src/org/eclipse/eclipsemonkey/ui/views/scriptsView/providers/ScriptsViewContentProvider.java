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

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.eclipsemonkey.ScriptService;
import org.eclipse.eclipsemonkey.StoredScript;
import org.eclipse.eclipsemonkey.ui.views.scriptsView.IScriptAction;
import org.eclipse.eclipsemonkey.ui.views.scriptsView.ScriptAction;
import org.eclipse.eclipsemonkey.ui.views.scriptsView.ScriptActionSet;
import org.eclipse.eclipsemonkey.ui.views.scriptsView.ScriptActionsManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The content provider class is responsible for providing objects to the
 * view. It can wrap existing objects in adapters or simply return objects
 * as-is. These objects may be sensitive to the current input of the view,
 * or ignore it and always show the same content (like Task List, for
 * example).
 */
public class ScriptsViewContentProvider implements ITreeContentProvider {

	private Pattern submenu_pattern = Pattern.compile("^(.+?)>(.*)$");

	private ScriptActionsManager _scriptActionsManager = null;

	public ScriptsViewContentProvider() {
		_scriptActionsManager = ScriptActionsManager.getInstance();
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {

		updateActionSets();

		// Get actions and action sets
		IScriptAction[] actions = _scriptActionsManager.getAll();
		if(actions != null && actions.length > 0) {
			return actions;
		}
		return new Object[0];
	}

	private void updateActionSets() {
		ArrayList<String> foundItems = new ArrayList<String>();

		Map<URI, StoredScript> scriptStore = ScriptService.getInstance().getScriptStore();
		Object[] scripts = scriptStore.values().toArray();

		for(int i = 0; i < scripts.length; i++) {
			if(scripts[i] instanceof StoredScript) {
				StoredScript s = (StoredScript)scripts[i];

				if(s.metadata == null || s.metadata.getMenuName() == null)
					continue;

				String menuName = s.metadata.getMenuName().trim();

				foundItems.add(menuName);

				Matcher match = submenu_pattern.matcher(menuName);

				if(match.find()) {

					String primary_key = match.group(1).trim();
					String secondary_key = match.group(2).trim();

					ScriptActionSet as = _scriptActionsManager.createScriptActionSet(primary_key);
					as.addScriptAction(secondary_key, s);
				} else {
					_scriptActionsManager.addScriptAction(menuName, s);
				}
			}
		}

		pruneUnusedActions(foundItems);
	}

	private void pruneUnusedActions(ArrayList<String> foundItems) {

		ScriptAction[] actions = _scriptActionsManager.getScriptActions();
		ScriptActionSet[] sets = _scriptActionsManager.getScriptActionSets();

		for(int i = 0; i < actions.length; i++) {
			String name = actions[i].getStoredScript().metadata.getMenuName();
			if(foundItems.contains(name) == false)
				_scriptActionsManager.removeScriptAction(actions[i]);
		}

		for(int i = 0; i < sets.length; i++) {
			actions = sets[i].getScriptActions();
			for(int j = 0; j < actions.length; j++) {
				String name = actions[j].getStoredScript().metadata.getMenuName();
				if(foundItems.contains(name) == false)
					_scriptActionsManager.removeScriptActionSet(name);
			}
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ScriptActionSet) {
			ScriptActionSet actionSet = (ScriptActionSet)parentElement;
			ScriptAction[] actions = actionSet.getScriptActions();

			return actions;
		} else {
			return new Object[0];
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if(element instanceof ScriptAction) {
			return ((ScriptAction)element).getParent();
		} else {
			// ActionSets have no parents
			return null;
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if(element instanceof ScriptActionSet) {
			ScriptActionSet actionSet = (ScriptActionSet)element;

			return actionSet.getScriptActions().length > 0;
		} else {
			return false;
		}
	}

}
