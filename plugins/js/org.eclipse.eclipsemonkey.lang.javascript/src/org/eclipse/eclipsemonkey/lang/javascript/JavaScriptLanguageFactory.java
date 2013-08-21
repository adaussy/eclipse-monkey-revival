/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.eclipse.eclipsemonkey.lang.javascript;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.eclipsemonkey.DOMDescriptor;
import org.eclipse.eclipsemonkey.IMonkeyScriptRunner;
import org.eclipse.eclipsemonkey.ScriptMetadata;
import org.eclipse.eclipsemonkey.Subscription;
import org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author Paul Colton (Aptana, Inc.)
 * 
 */
public class JavaScriptLanguageFactory implements IMonkeyLanguageFactory {

	/**
	 * @see org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory#getRunMonkeyScript(org.eclipse.core.runtime.IPath,
	 *      org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public IMonkeyScriptRunner getRunMonkeyScript(IPath path, IWorkbenchWindow window) {
		return new JavaScriptRunner(path, window);
	}

	/**
	 * @see org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory#init(java.lang.String, java.lang.String)
	 */
	@Override
	public void init(String pluginID, String languageName) {
	}

	/**
	 * @param contents
	 * @return ScriptMetadata
	 */
	@Override
	public ScriptMetadata getScriptMetadata(String contents) {
		ScriptMetadata metadata = new ScriptMetadata();
		Pattern p = Pattern.compile("^\\s*\\/\\*.*?\\*\\/", Pattern.DOTALL);
		Matcher m = p.matcher(contents);
		if(m.find()) {
			String comment = m.group();
			p = Pattern.compile(IMetadaPattern.MENU_METADA_PATTERN, Pattern.DOTALL);
			m = p.matcher(comment);
			if(m.find()) {
				metadata.setMenuName(m.group(1));
			}
			p = Pattern.compile(IMetadaPattern.ON_LOAD_METADATA_PATTERN, Pattern.DOTALL);
			m = p.matcher(comment);
			if(m.find()) {
				String funct = m.group(1);
				// [IM] Listener takes an ending (), so we allow it here just to be consistent
				if(funct.endsWith("()"))
					;
				{
					funct = funct.substring(0, funct.length() - 2);
					metadata.setOnLoadFunction(funct);
				}
			}
			p = Pattern.compile(IMetadaPattern.KEY_METADA_PATTERN, Pattern.DOTALL);
			m = p.matcher(comment);
			if(m.find()) {
				metadata.setKey(m.group(1));
			}
			p = Pattern.compile(IMetadaPattern.SCOPE_SCOPE_METADA_PATTERN, Pattern.DOTALL);
			m = p.matcher(comment);
			if(m.find()) {
				metadata.setScopeName(m.group(1));
			}
			p = Pattern.compile(IMetadaPattern.DOM_METADATA_PATTERN, Pattern.DOTALL);
			m = p.matcher(comment);
			while(m.find()) {
				metadata.getDOMs().add(new DOMDescriptor(m.group(1), m.group(2)));
			}
			p = Pattern.compile(IMetadaPattern.LISTENER_METADA_PATTERN, Pattern.DOTALL);
			m = p.matcher(comment);
			while(m.find()) {
				metadata.getSubscriptions().add(new Subscription(m.group(1), m.group(2)));
			}
			p = Pattern.compile(IMetadaPattern.DESCRIPTION_METADA_PATTERN, Pattern.DOTALL);
			m = p.matcher(comment);
			while(m.find()) {
				metadata.setDescription(m.group(1));
			}
		} else {
			// no meta-data comment - do nothing
		}
		return metadata;
	}
}
