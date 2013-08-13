/*******************************************************************************
 * Copyright (c) 2006 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bjorn Freeman-Benson - initial implementation
 *     Ward Cunningham - initial implementation
 *******************************************************************************/

package org.eclipse.eclipsemonkey;

/**
 * RunMonkeyException
 */
public class RunMonkeyException extends Exception {

	/**
	 * serialVersionUID
	 */
	public static final long serialVersionUID = 1;

	/**
	 * exceptionName
	 */
	public String exceptionName;

	/**
	 * fileName
	 */
	public String fileName;

	/**
	 * lineNumber
	 */
	public Integer lineNumber;

	/**
	 * errorMessage
	 */
	public String errorMessage;

	/**
	 * RunMonkeyException
	 * 
	 * @param e
	 *        exceptionName
	 * @param f
	 *        fileName
	 * @param n
	 *        lineNumber
	 * @param m
	 *        errorMessage
	 */
	public RunMonkeyException(String e, String f, Integer n, String m) {
		super(e + ": " + m + " " + f + (n != null ? " #" + n.intValue() : ""));
		exceptionName = e;
		fileName = f;
		lineNumber = n;
		errorMessage = m;
	}

	/**
	 * @return The line number
	 */
	public String optionalLineNumber() {
		if(lineNumber == null)
			return "";
		if(lineNumber.intValue() <= 0)
			return "";
		return " line " + lineNumber.intValue();
	}
}
