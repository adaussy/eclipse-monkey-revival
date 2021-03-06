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
 *******************************************************************************/

package org.eclipse.eclipsemonkey.internal;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.eclipse.eclipsemonkey.dom.IDynamicState;

/**
 * DynamicState
 */
public class DynamicState implements IDynamicState {

	private static final String CONTEXT_ID_KEY = "The jaws that bite, the claws that catch!";

	Stack<Map<String, Object>> context_stack = null;

	/**
	 * DynamicState
	 */
	public DynamicState() {
		context_stack = new Stack<Map<String, Object>>();
		context_stack.push(new Hashtable<String, Object>());
	}

	/**
	 * @see org.eclipse.eclipsemonkey.dom.IDynamicState#set(java.lang.String, java.lang.Object)
	 */
	public void set(String name, Object value) {
		top().put(name, value);
	}

	private Map<String, Object> top() {
		return (context_stack.lastElement());
	}

	/**
	 * @see org.eclipse.eclipsemonkey.dom.IDynamicState#get(java.lang.String)
	 */
	public Object get(String name) {
		return top().get(name);
	}

	/**
	 * @see org.eclipse.eclipsemonkey.dom.IDynamicState#begin(java.lang.Object)
	 */
	public void begin(Object id) {
		Map<String, Object> oldtop = top();
		Map<String, Object> top = new Hashtable<String, Object>();
		for(Iterator<String> iter = oldtop.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			top.put(key, oldtop.get(key));
		}
		top.put(CONTEXT_ID_KEY, id);
		context_stack.add(top);
	}

	/**
	 * @see org.eclipse.eclipsemonkey.dom.IDynamicState#end(java.lang.Object)
	 */
	public void end(Object id) {
		Object to_remove = null;
		for(Iterator<Map<String, Object>> iter = context_stack.iterator(); iter.hasNext();) {
			Map<String, Object> element = iter.next();
			if(element.get(CONTEXT_ID_KEY) == id)
				to_remove = element;
		}
		if(to_remove != null) {
			Object x;
			do {
				x = context_stack.pop();
			} while(x != to_remove);
		}
	}

}
