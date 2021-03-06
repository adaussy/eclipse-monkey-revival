/*******************************************************************************
 * Copyright (c) 2008 AIRBUS FRANCE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Charles David (Obeo) - initial API and implementation
 *******************************************************************************/
package org.eclipse.eclipsemonkey.doms.output;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.eclipsemonkey.console.ScriptConsole;
import org.eclipse.eclipsemonkey.console.ScriptConsoleFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * This DOM provides services for Topcased scripts to show information to the user.
 * 
 * @author <a href="mailto:pierre-charles.david@obeo.fr">Pierre-Charles David</a>
 */
public class OutputDOM {

	private IConsoleManager consoleManager;

	/**
	 * Create a new text file in the workspace with the specified contents. This method is provided for convenience to
	 * directly handle the result of <code>input.selectContainer()</code>.
	 * 
	 * @param location
	 *        an array of one element which denotes the the container (project or folder) where to put the new
	 *        file. It can be either an IFile or an IPath.
	 * @param name
	 *        the name of the new file.
	 * @param contents
	 *        the textual contents to store in the new file.
	 * @return <code>true</code> if the file was successfully created, <code>false</code> if there was en error.
	 */
	public boolean createNewFile(Object[] location, String name, String contents) {
		if(location != null && location.length != 1) {
			return false;
		} else {
			return createNewFile(location[0], name, contents);
		}
	}

	/**
	 * Create a new text file in the workspace with the specified contents.
	 * 
	 * @param dest
	 *        the container (project or folder) where to put the new file. It can be either an IFile or an IPath.
	 * @param name
	 *        the name of the new file.
	 * @param contents
	 *        the textual contents to store in the new file.
	 * @return <code>true</code> if the file was successfully created, <code>false</code> if there was en error.
	 */
	public boolean createNewFile(Object dest, String name, String contents) {
		IFile eclipseFile = null;
		if(dest instanceof IContainer) {
			eclipseFile = ((IContainer)dest).getFile(new Path(name));
		} else if(dest instanceof IPath) {
			eclipseFile = ResourcesPlugin.getWorkspace().getRoot().getFile(((IPath)dest).append(name));
		}

		return createNewfile(eclipseFile, contents);
	}

	/**
	 * Create a new text file in the workspace with the specified contents.
	 * 
	 * @param workspacePath
	 *        the workspace-relative path of the new file to create (e.g. <code>"MyProject/myfile.txt"</code>).
	 * @param contents
	 *        the textual contents to store in the new file.
	 * @return <code>true</code> if the file was successfuly created, <code>false</code> if there was en error.
	 */
	public boolean createNewFile(String workspacePath, String contents) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(workspacePath));
		return createNewfile(file, contents);
	}

	private boolean createNewfile(IFile eclipseFile, String contents) {
		if(eclipseFile.exists()) {
			return false;
		}
		try {
			InputStream input = new ByteArrayInputStream(contents.getBytes());
			eclipseFile.create(input, true, new NullProgressMonitor());
			return true;
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * Appends a message to the Topcased Scripts console.
	 * 
	 * @param message
	 *        the message to append to the console.
	 */
	public void console(String message) {
		ScriptConsole console = getScriptsConsole();
		String doc = console.getDocument().get();
		console.getDocument().set(doc + message + "\n");
		getConsoleManager().showConsoleView(console);
	}

	/**
	 * Clears the Topcased Scripts console.
	 */
	public void clearConsole() {
		ScriptConsole console = getScriptsConsole();
		console.getDocument().set("");
		getConsoleManager().showConsoleView(console);
	}

	private IConsoleManager getConsoleManager() {
		if(consoleManager == null) {
			consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		}
		return consoleManager;
	}

	private ScriptConsole getScriptsConsole() {

		for(IConsole console : getConsoleManager().getConsoles()) {
			if(console instanceof ScriptConsole && console.getType().equals("topcasedScriptConsole")) {
				return (ScriptConsole)console;
			}
		}
		ScriptConsoleFactory factory = new ScriptConsoleFactory();
		factory.openConsole();
		return factory.getConsole();
	}

	/**
	 * Open a dialog box showing an information message to the user.
	 * 
	 * @param title
	 *        the title of the dialog box.
	 * @param message
	 *        the information message.
	 */
	public void info(String title, String message) {
		MessageDialog.openInformation(getShell(), title, message);
	}

	/**
	 * Open a dialog box showing an information message to the user using a default title.
	 * 
	 * @param message
	 *        the information message.
	 */
	public void info(String message) {
		info("Script information", message);
	}

	/**
	 * Open a dialog box showing a warning message to the user.
	 * 
	 * @param title
	 *        the title of the dialog box.
	 * @param message
	 *        the warning message.
	 */
	public void warning(String title, String message) {
		MessageDialog.openWarning(getShell(), title, message);
	}

	/**
	 * Open a dialog box showing a warning message to the user using a default title.
	 * 
	 * @param message
	 *        the warning message.
	 */
	public void warning(String message) {
		warning("Script warning", message);
	}

	/**
	 * Open a dialog box showing an error message to the user.
	 * 
	 * @param title
	 *        the title of the dialog box.
	 * @param message
	 *        the error message.
	 */
	public void error(String title, String message) {
		MessageDialog.openError(getShell(), title, message);
	}

	/**
	 * Open a dialog box showing an error message to the user and a default title.
	 * 
	 * @param message
	 *        the error message.
	 */
	public void error(String message) {
		error("Script error", message);
	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
}
