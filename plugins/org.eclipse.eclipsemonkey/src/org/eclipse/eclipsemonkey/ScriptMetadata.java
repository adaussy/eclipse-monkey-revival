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
 *     Channing Walton - bug 143456
 *     Jeff Mesnil - bug 132601
 *******************************************************************************/

package org.eclipse.eclipsemonkey;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.eclipsemonkey.utils.UIUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;
import org.osgi.framework.Bundle;

/**
 * ScriptMetadata
 */
public class ScriptMetadata {

	private IPath path;

	private String menuName;

	private String onLoadFunction = null;

	private String scopeName;

	private List doms = new ArrayList();

	private List subscriptions = new ArrayList();

	private String accelerator;

	/**
	 * @param string
	 */
	public void setMenuName(String string) {
		this.menuName = string;
	}

	/**
	 * @param string
	 */
	public void setOnLoadFunction(String string) {
		this.onLoadFunction = string;
	}

	/**
	 * @param path
	 */
	public void setPath(IPath path) {
		this.path = path;

	}

	/**
	 * @return IPath
	 */
	public IPath getPath() {
		return path;
	}

	/**
	 * @return String
	 */
	public String getMenuName() {
		return menuName;
	}

	/**
	 * @return String
	 */
	public String getOnLoadFunction() {
		return this.onLoadFunction;
	}

	/**
	 * @return String
	 */
	public String getScopeName() {
		return scopeName;
	}

	/**
	 * @param s
	 */
	public void setScopeName(String s) {
		scopeName = s;
	}

	/**
	 * @return List
	 */
	public List getDOMs() {
		return doms;
	}

	/**
	 * @return String
	 */
	public String getReasonableFilename() {
		if(path != null)
			return path.toFile().getName();
		if(menuName != null && !menuName.equals("")) {
			String result = menuName;
			result = result.replaceAll(" ", "_");
			Pattern illegalChars = Pattern.compile("[^\\p{Alnum}_-]");
			Matcher match = illegalChars.matcher(result);
			result = match.replaceAll("");
			if(!result.equals(""))
				return result + ".js";
		}
		return "script.js";
	}

	/**
	 * @param plugin_id
	 * @return boolean
	 */
	public boolean containsDOM_by_plugin(String plugin_id) {
		for(Iterator iter = doms.iterator(); iter.hasNext();) {
			DOMDescriptor element = (DOMDescriptor)iter.next();
			if(element.plugin_name.equals(plugin_id))
				return true;
		}
		return false;
	}

	/**
	 * @param window
	 * @return boolean
	 */
	public boolean ensure_doms_are_loaded(IWorkbenchWindow window) {
		String missing_plugin_names = "";
		URLtoPluginMap missing_urls = new URLtoPluginMap();
		for(Iterator iter = doms.iterator(); iter.hasNext();) {
			DOMDescriptor element = (DOMDescriptor)iter.next();
			Bundle b = Platform.getBundle(element.plugin_name);
			if(b == null) {
				missing_plugin_names += "     " + element.plugin_name + "\n";
				missing_urls.add(element);
			} else if(b.getState() == Bundle.UNINSTALLED) {
				missing_plugin_names += "     " + element.plugin_name + "\n";
			}
		}
		if(missing_plugin_names.length() > 0) {
			missing_plugin_names = missing_plugin_names.substring(0, missing_plugin_names.length() - 1);
			String choice = notifyMissingDOMs(missing_plugin_names);
			if(choice.startsWith("Install")) {
				launchUpdateInstaller(missing_urls);
			}
			if(choice.startsWith("Edit")) {
				openEditor();
			}
			return false;
		}
		return true;
	}

	class URLtoPluginMap {

		Map map = new HashMap();

		Iterator iterator() {
			return map.keySet().iterator();
		}

		String getPluginNames(String url) {
			Set ids = (Set)map.get(url);
			String idstr = "";
			for(Iterator iterator = ids.iterator(); iterator.hasNext();) {
				String id = (String)iterator.next();
				idstr += id + ", ";
			}
			idstr = idstr.substring(0, idstr.length() - 2);
			return idstr;
		}

		void add(DOMDescriptor domdesc) {
			Set ids = (Set)map.get(domdesc.url);
			if(ids == null)
				ids = new HashSet();
			ids.add(domdesc.plugin_name);
			map.put(domdesc.url, ids);
		}
	}

	private void openEditor() {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(path.toFile().getName());
			if(desc == null) {
				desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("foo.txt");
			}
			page.openEditor(UIUtils.createEditorInput(path), desc.getId());
		} catch (PartInitException x) {
			MessageDialog.openError(null, "Eclipse::PartInitException", "Unable to open editor on " + path.toFile().getName() + " due to " + x.toString());
		}
	}

	private void launchUpdateInstaller(URLtoPluginMap missing_urls) {
		UpdateSearchScope scope = new UpdateSearchScope();
		String[] skips = {};
		for(Iterator iter = missing_urls.iterator(); iter.hasNext();) {
			String url = (String)iter.next();
			try {
				String idstr = missing_urls.getPluginNames(url);
				String plural2 = idstr.indexOf(",") >= 0 ? "s" : "";
				scope.addSearchSite("Site providing DOM" + plural2 + " (" + idstr + ")", new URL(url), skips);
			} catch (MalformedURLException x) {
				// ignore
			}
		}
		UpdateSearchRequest request = new UpdateSearchRequest(UpdateSearchRequest.createDefaultSiteSearchCategory(), scope);
		UpdateJob job = new UpdateJob("Install Eclipse Monkey DOMs", request);
		Shell shell = Workbench.getInstance().getWorkbenchWindows()[0].getShell();
		UpdateManagerUI.openInstaller(shell, job);
	}

	private String notifyMissingDOMs(String missing_plugin_names) {
		String plural = (missing_plugin_names.indexOf("\n") >= 0 ? "s" : "");
		String[] choices = new String[]{ "Cancel Script", "Edit Script", "Install Plug-in" + plural };
		MessageDialog dialog = new MessageDialog(null, "Missing DOM" + plural, null, "The script " + this.path.toFile().getName() + " requires " + (missing_plugin_names.indexOf("\n") >= 0 ? "these" : "this") + " missing DOM plug-in" + plural + ":\n" + missing_plugin_names, MessageDialog.WARNING, choices, 2);
		int result = dialog.open();
		String choice = choices[result];
		return choice;
	}

	/**
	 * @param key
	 */
	public void setKey(String key) {
		this.accelerator = key;
	}

	/**
	 * @return String
	 */
	public String getAccelerator() {
		return accelerator;
	}

	/**
	 * @return String
	 */
	public boolean hasAccelerator() {
		return accelerator != null;
	}

	/**
	 * @return List
	 */
	public List getSubscriptions() {
		return subscriptions;
	}

	/**
	 * 
	 */
	public void subscribe() {
		for(int i = 0; i < subscriptions.size(); i++) {
			Subscription subscription = (Subscription)subscriptions.get(i);
			subscription.subscribe(path);
		}
	}

	/**
	 * 
	 */
	public void unsubscribe() {
		for(int i = 0; i < subscriptions.size(); i++) {
			Subscription subscription = (Subscription)subscriptions.get(i);
			subscription.unsubscribe();
		}
	}

}
