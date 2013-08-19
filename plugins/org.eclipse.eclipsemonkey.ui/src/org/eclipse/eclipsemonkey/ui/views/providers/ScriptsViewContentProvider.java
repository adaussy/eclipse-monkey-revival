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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.eclipsemonkey.ScriptService;
import org.eclipse.eclipsemonkey.StoredScript;
import org.eclipse.eclipsemonkey.ui.IScriptAction;
import org.eclipse.eclipsemonkey.ui.IScriptActionSet;
import org.eclipse.eclipsemonkey.ui.IScriptUI;
import org.eclipse.eclipsemonkey.ui.ScriptActionsManager;
import org.eclipse.eclipsemonkey.ui.data.ScriptAction;
import org.eclipse.eclipsemonkey.ui.data.ScriptActionSet;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.collect.Lists;

/**
 * The content provider class is responsible for providing objects to the
 * view. It can wrap existing objects in adapters or simply return objects
 * as-is. These objects may be sensitive to the current input of the view,
 * or ignore it and always show the same content (like Task List, for
 * example).
 */
public class ScriptsViewContentProvider implements ITreeContentProvider {

	private ScriptActionsManager _scriptActionsManager = null;

	public ScriptsViewContentProvider() {
		_scriptActionsManager = ScriptActionsManager.getInstance();
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object parent) {

		updateActionSets();

		// Get actions and action sets
		Collection<IScriptUI> actions = _scriptActionsManager.getAll();
		if(actions != null && !actions.isEmpty()) {
			return actions.toArray();
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

				ArrayList<String> path = Lists.newArrayList(menuName.split(">"));
				ListIterator<String> pathIte = path.listIterator();
				IScriptActionSet parent = null;
				while(pathIte.hasNext()) {
					String string = (String)pathIte.next().trim();
					if(pathIte.hasNext()) {
						if(parent == null) {
							parent = _scriptActionsManager.createOrGetScriptActionSet(string);
						} else {
							parent = parent.addSubActionSet(string);
						}
					} else {
						if(parent == null) {
							_scriptActionsManager.addScriptAction(string, s);
						} else {
							parent.addScriptAction(string, s);
						}
					}
				}
			}
		}

		pruneUnusedActions(foundItems);
	}

	private void pruneUnusedActions(ArrayList<String> foundItems) {

		List<IScriptAction> actions = new ArrayList<IScriptAction>(_scriptActionsManager.getScriptActions());
		List<IScriptActionSet> sets = new ArrayList<IScriptActionSet>(_scriptActionsManager.getScriptActionSets());
		Iterator<IScriptAction> actionIte = actions.iterator();
		while(actionIte.hasNext()) {
			ScriptAction scriptAction = (ScriptAction)actionIte.next();
			String name = scriptAction.getStoredScript().metadata.getMenuName();
			if(foundItems.contains(name) == false) {
				_scriptActionsManager.removeScriptAction(scriptAction);
			}
			actionIte.remove();
		}
		ListIterator<IScriptActionSet> setsIte = sets.listIterator();
		while(setsIte.hasNext()) {
			IScriptActionSet scriptActionSet = (ScriptActionSet)setsIte.next();
			actions = scriptActionSet.getScriptActions();
			while(actionIte.hasNext()) {
				ScriptAction scriptAction = (ScriptAction)actionIte.next();
				String name = scriptAction.getStoredScript().metadata.getMenuName();
				if(foundItems.contains(name) == false) {
					_scriptActionsManager.removeScriptAction(scriptAction);
				}
				actionIte.remove();
			}
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ScriptActionSet) {
			IScriptActionSet actionSet = (IScriptActionSet)parentElement;
			List<IScriptUI> actions = new ArrayList<IScriptUI>(actionSet.getSubSet());
			actions.addAll(actionSet.getScriptActions());
			return actions.toArray();
		} else {
			return new Object[0];
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
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
	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof ScriptActionSet) {
			IScriptActionSet actionSet = (IScriptActionSet)element;

			return actionSet.getScriptActions().size() > 0 || !actionSet.getSubSet().isEmpty();
		} else {
			return false;
		}
	}

}
