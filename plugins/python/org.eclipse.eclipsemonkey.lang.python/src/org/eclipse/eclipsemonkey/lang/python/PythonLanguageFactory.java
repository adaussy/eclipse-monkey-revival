/*******************************************************************************
 * Copyright (c) 2005-2006 Aptana, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 * 
 * Contributors:
 *     Paul Colton - initial implementation
 *     José Fonseca - adapted for python
 *******************************************************************************/

package org.eclipse.eclipsemonkey.lang.python;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.eclipsemonkey.DOMDescriptor;
import org.eclipse.eclipsemonkey.IMetadaPattern;
import org.eclipse.eclipsemonkey.IMonkeyScriptRunner;
import org.eclipse.eclipsemonkey.ScriptMetadata;
import org.eclipse.eclipsemonkey.Subscription;
import org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author José Fonseca
 * @author Paul Colton (Aptana, Inc.)
 * 
 */
public class PythonLanguageFactory implements IMonkeyLanguageFactory {

	/**
	 * @see org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory#getRunMonkeyScript(org.eclipse.core.runtime.IPath,
	 *      org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public IMonkeyScriptRunner getRunMonkeyScript(IPath path, IWorkbenchWindow window) {
		return new PythonRunner(path, window);
	}

	/**
	 * @see org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory#init(java.lang.String, java.lang.String)
	 */
	@Override
	public void init(String pluginID, String languageName) {
	}

	/**
	 * @see org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory#getScriptMetadata(java.lang.String)
	 */
	@Override
	public ScriptMetadata getScriptMetadata(String contents) {
		ScriptMetadata metadata = new ScriptMetadata();
		Pattern commentPattern = Pattern.compile("^\\s*#.*$", Pattern.MULTILINE);
		Pattern menuPattern = Pattern.compile("Menu:\\s*((\\p{Graph}| )+)", Pattern.DOTALL);
		Pattern onloadPattern = Pattern.compile("OnLoad:\\s*((\\p{Graph}| )+)", Pattern.DOTALL);
		Pattern keyPattern = Pattern.compile("Key:\\s*((\\p{Graph}| )+)", Pattern.DOTALL);
		Pattern scopePattern = Pattern.compile("Scope:\\s*((\\p{Graph}| )+)", Pattern.DOTALL);
		Pattern domPattern = Pattern.compile("DOM:\\s*(\\p{Graph}+)\\/((\\p{Alnum}|\\.)+)", Pattern.DOTALL);
		Pattern listenerPattern = Pattern.compile("Listener:\\s*(\\w+)\\(\\)\\.(\\w+)", Pattern.DOTALL);
		Pattern descriptionMetadata = Pattern.compile(IMetadaPattern.DESCRIPTION_METADA_PATTERN, Pattern.DOTALL);

		Matcher cm = commentPattern.matcher(contents);
		while(cm.find()) {
			String comment = cm.group();
			Matcher m = menuPattern.matcher(comment);
			if(m.find()) {
				metadata.setMenuName(m.group(1));
			}
			m = onloadPattern.matcher(comment);
			if(m.find()) {
				String funct = m.group(1);
				// [IM] Listener takes an ending (), so we allow it here just to be consistent
				if(funct.endsWith("()")) {
					funct = funct.substring(0, funct.length() - 2);
				}
				metadata.setOnLoadFunction(funct);
			}
			m = keyPattern.matcher(comment);
			if(m.find()) {
				metadata.setKey(m.group(1));
			}
			m = scopePattern.matcher(comment);
			if(m.find()) {
				metadata.setScopeName(m.group(1));
			}
			m = domPattern.matcher(comment);
			while(m.find()) {
				metadata.getDOMs().add(new DOMDescriptor(m.group(1), m.group(2)));
			}
			m = listenerPattern.matcher(comment);
			while(m.find()) {
				metadata.getSubscriptions().add(new Subscription(m.group(1), m.group(2)));
			}
			m = descriptionMetadata.matcher(comment);
			while(m.find()) {
				metadata.setDescription(m.group(1));
			}
		}
		return metadata;
	}
}
