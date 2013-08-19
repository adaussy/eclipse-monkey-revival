/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.eclipse.eclipsemonkey.lang.javascript.events;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author Kevin Lindsey
 */
public abstract class EventTarget extends ScriptableObject implements IEventTarget {

	/*
	 * Fields
	 */
	private Hashtable<String, ArrayList> _events;

	/*
	 * Constructors
	 */

	/**
	 * Create a new instance of EventTarget
	 */
	public EventTarget() {
		String[] names = new String[]{ "addEventListener", "removeEventListener" };

		this.defineFunctionProperties(names, EventTarget.class, READONLY | PERMANENT);
	}

	/*
	 * Methods
	 */

	/**
	 * @param event
	 */
	@Override
	public void fireEventListeners(Event event) {
		this.fireEventListeners(event.getType(), new Object[]{ event });
	}

	/**
	 * @param eventType
	 * @param args
	 */
	@Override
	public void fireEventListeners(String eventType, Object[] args) {
		if(this._events != null && this._events.containsKey(eventType)) {
			ArrayList handlers = _events.get(eventType);

			for(int i = 0; i < handlers.size(); i++) {
				Object eventHandler = handlers.get(i);

				if(eventHandler instanceof Function) {
					final Object[] fArgs = args;
					final Function f = (Function)eventHandler;
					final Scriptable scope = f.getParentScope();

					final IWorkbench workbench = PlatformUI.getWorkbench();
					Display display = workbench.getDisplay();

					// execute callback in the correct thread
					display.syncExec(new Runnable() {

						@Override
						public void run() {
							Context cx = Context.enter();

							try {
								f.call(cx, scope, scope, fArgs);
							} finally {
								Context.exit();
							}
						}

					});
				}
			}
		}

	}

	/**
	 * @param eventType
	 * @param eventHandler
	 */
	@Override
	public void addEventListener(String eventType, Object eventHandler) {
		if(this._events == null) {
			this._events = new Hashtable<String, ArrayList>();
		}

		if(this._events.containsKey(eventType) == false) {
			this._events.put(eventType, new ArrayList());
		}

		ArrayList<Object> handlers = this._events.get(eventType);

		handlers.add(eventHandler);
	}

	/**
	 * @param eventType
	 * @param eventHandler
	 */
	@Override
	public void removeEventListener(String eventType, Object eventHandler) {
		if(this._events != null) {
			if(this._events.containsKey(eventType)) {
				ArrayList handlers = this._events.get(eventType);

				handlers.remove(eventHandler);
			}
		}
	}
}
