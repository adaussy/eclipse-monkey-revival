/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.eclipse.eclipsemonkey.utils;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 
 * @author Paul Colton
 * 
 */
public class UIUtils {

	/*
	 * Are we in Eclipse 3.2 or higher?
	 */
	static boolean inEclipse32orHigher = false;

	static {

		String version = System.getProperty("osgi.framework.version"); //$NON-NLS-1$

		if(version != null && version.startsWith("3.")) //$NON-NLS-1$
		{
			String[] parts = version.split("\\.");
			if(parts.length > 1) {
				try {
					if(Integer.parseInt(parts[1]) > 1)
						inEclipse32orHigher = true;
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * getViewInternal
	 * 
	 * @param id
	 * @param secondaryId
	 * @return IWorkbenchPart
	 */
	public static IWorkbenchPart getViewInternal(final String id, final String secondaryId) {
		/**
		 * Internal class for getting a view.
		 * 
		 * @author Ingo Muschenetz
		 */
		IWorkbenchPart[] parts = getViewsInternal(id, secondaryId);
		if(parts.length == 0) {
			return null;
		} else {
			return parts[0];
		}
	}

	/**
	 * Gets all views with the primary part id, and any secondary part id
	 * 
	 * @param id
	 * @return IWorkbenchPart
	 */
	public static IWorkbenchPart[] getViewsInternal(final String id) {
		return getViewsInternal(id, null);
	}

	/**
	 * getViewInternal
	 * 
	 * @param id
	 * @param secondaryId
	 * @return IWorkbenchPart
	 */
	public static IWorkbenchPart[] getViewsInternal(final String id, final String secondaryId) {
		/**
		 * Internal class for getting a view.
		 * 
		 * @author Ingo Muschenetz
		 */
		class ViewGetterThread implements Runnable {

			/*
			 * Fields
			 */
			public ArrayList targetView = new ArrayList();

			/**
			 * run
			 */
			public void run() {
				IViewReference[] views = null;

				try {
					IWorkbench w = PlatformUI.getWorkbench();
					IWorkbenchWindow ww = w.getActiveWorkbenchWindow();

					if(ww != null) {
						IWorkbenchPage wp = ww.getActivePage();

						if(wp != null) {
							views = wp.getViewReferences();

							for(int i = 0; i < views.length; i++) {
								if(id.equals(views[i].getId())) {
									if(secondaryId != null) {
										if(secondaryId.equals(views[i].getSecondaryId())) {
											targetView.add(views[i].getPart(false));
										}
									} else {
										targetView.add(views[i].getPart(false));
									}
								}
							}
						}
					}
				} catch (Exception e) {
					System.err.println(e.toString());
					return;
				}
			}
		}

		ViewGetterThread getter = new ViewGetterThread();
		Display display = Display.getDefault();
		display.syncExec(getter);

		IWorkbenchPart[] parts = (IWorkbenchPart[])getter.targetView.toArray(new IWorkbenchPart[0]);
		return parts;
	}

	/**
	 * Create an editor input from path
	 * 
	 * @param path
	 * @return
	 */
	public static IEditorInput createEditorInput(IPath path) {
		if(path != null) {
			IFile t = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			return createEditorInput(t);
		}
		return null;
	}

	/**
	 * Create an editor input from file
	 * 
	 * @param t
	 * @return
	 */
	private static IEditorInput createEditorInput(IFile t) {
		if(t != null) {
			return new FileEditorInput(t);
		}
		return null;
	}

}
