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

/**
 * All ui element that shoul be represent in ui
 * 
 * @author adaussy
 * 
 */
public interface IScriptUI {

	/**
	 * getName
	 * 
	 * @return String
	 */
	String getName();

	/**
	 * setName
	 * 
	 * @param name
	 */
	void setName(String name);

}
