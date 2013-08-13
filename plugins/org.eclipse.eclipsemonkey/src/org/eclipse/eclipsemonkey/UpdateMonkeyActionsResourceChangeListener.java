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

package org.eclipse.eclipsemonkey;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.eclipsemonkey.actions.RecreateMonkeyMenuAction;
import org.eclipse.eclipsemonkey.dom.Utilities;
import org.eclipse.eclipsemonkey.language.IMonkeyLanguageFactory;
import org.eclipse.eclipsemonkey.utils.URIScriptUtils;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * UpdateMonkeyActionsResourceChangeListener
 */
public class UpdateMonkeyActionsResourceChangeListener implements IResourceChangeListener {

	protected static Set<String> extensionsSet = null;
	static {
		extensionsSet = new HashSet<String>();
		extensionsSet.add("js");
		extensionsSet.add("em");
	}

	/**
	 * @param exts
	 */
	public static void setExtensions(Collection<String> exts) {
		if(exts == null)
			return;
		extensionsSet.addAll(exts);
	}

	/**
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		final Boolean changes[] = new Boolean[1];
		changes[0] = new Boolean(false);

		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

			private void found_a_change() {
				changes[0] = new Boolean(true);
			}

			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				if(resource instanceof IFile) {
					IFile file = (IFile)resource;
					if(EclipseMonkeyProjectNature.isEclipseMonkeyResource(file)) {
						handleScriptResourceChange(delta, file);
					} else if(".project".equals(file.getName())) {
						//When the .project is modify then look for scripts
						if(delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED)
							findScriptsInContainer(resource.getProject());
					}
				}
				return true;
			}

			private void handleScriptResourceChange(IResourceDelta delta, IFile file) {
				if(extensionsSet.contains(file.getFileExtension())) {
					URI fileURI = URIScriptUtils.getAbsoluteURI(delta);
					switch(delta.getKind()) {
					case IResourceDelta.ADDED:
						processNewOrChangedScript(fileURI, file.getLocation());
						found_a_change();
						break;
					case IResourceDelta.REMOVED:
						processRemovedScript(fileURI, file.getLocation());
						found_a_change();
						break;
					case IResourceDelta.CHANGED:
						if((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
							processRemovedScript(URIUtil.toURI(delta.getMovedFromPath()), file.getLocation());
							processNewOrChangedScript(fileURI, file.getLocation());
							found_a_change();
						}
						if((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
							processRemovedScript(fileURI, file.getLocation());
							processNewOrChangedScript(URIUtil.toURI(delta.getMovedToPath()), file.getLocation());
							found_a_change();
						}
						if((delta.getFlags() & IResourceDelta.REPLACED) != 0) {
							processNewOrChangedScript(fileURI, file.getLocation());
							found_a_change();
						}
						if((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
							processNewOrChangedScript(fileURI, file.getLocation());
							found_a_change();
						}
						break;
					}
				}
			}
		};
		try {
			event.getDelta().accept(visitor);
		} catch (CoreException x) {
			// log an error in the error log
		}
		boolean anyMatches = ((Boolean)(changes[0])).booleanValue();
		if(anyMatches) {
			createTheMonkeyMenu();
		}
	}

	private void processNewOrChangedScript(URI uri, IPath path) {
		StoredScript store = new StoredScript();
		store.scriptPath = path;
		try {
			store.metadata = getMetadataFrom(path);
		} catch (CoreException x) {
			store.metadata = new ScriptMetadata();
			// log an error in the error log
		} catch (IOException x) {
			store.metadata = new ScriptMetadata();
			// log an error in the error log
		}
		EclipseMonkeyPlugin.getDefault().addScript(uri, store);
	}

	private void processRemovedScript(URI name, IPath path) {
		EclipseMonkeyPlugin.getDefault().removeScript(name);
	}

	/**
	 * @param extensions
	 * @param alternatePaths
	 * @throws CoreException
	 */
	public void rescanAllFiles(Collection<String> extensions, String[] alternatePaths) throws CoreException {
		EclipseMonkeyPlugin.getDefault().clearScripts();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		findScriptsInContainer(extensions, workspace.getRoot());
		findScriptsInalternatePath(extensions, alternatePaths);
	}

	private void findScriptsInalternatePath(Collection<String> extensions, String[] alternatePaths) {
		for(int i = 0; i < alternatePaths.length; i++) {
			String path = alternatePaths[i];

			File folder = new File(path);
			String[] files = folder.list();

			for(int j = 0; j < files.length; j++) {

				String fullPath = folder.getAbsolutePath() + File.separator + files[j];
				File f = new File(fullPath);

				if(f.isFile()) {
					Iterator<String> extensionIterator = extensions.iterator();
					while(extensionIterator.hasNext()) {
						String ext = (String)extensionIterator.next();
						if(f.getName().toLowerCase().endsWith("." + ext)) {
							Path p = new Path(f.getAbsolutePath());
							processNewOrChangedScript(URIUtil.toURI(f.getAbsolutePath()), p);
						}
					}
				}
			}
		}
	}

	protected void findScriptsInContainer(IContainer container) throws CoreException {
		findScriptsInContainer(extensionsSet, container);
	}

	protected void findScriptsInContainer(final Collection<String> extensions, IContainer container) throws CoreException {
		container.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) throws CoreException {
				if(resource instanceof IProject) {
					IProject p = (IProject)resource;
					return EclipseMonkeyProjectNature.isEclipseMonkeyProject(p);
				} else if(resource instanceof IFile) {
					IFile file = (IFile)resource;
					if(extensions.contains(file.getFileExtension())) {
						IPath location = file.getLocation();
						URI scriptURI = URIUtil.toURI(location);
						processNewOrChangedScript(scriptURI, location);
					}
				}
				return true;
			}
		});
	}

	private ScriptMetadata getMetadataFrom(IPath path) throws CoreException, IOException {
		String contents = Utilities.getFileContents(path);
		IMonkeyLanguageFactory langFactory = (IMonkeyLanguageFactory)EclipseMonkeyPlugin.getDefault().getLanguageStore().get(path.getFileExtension());
		ScriptMetadata metadata = langFactory.getScriptMetadata(contents);
		metadata.setPath(path);
		return metadata;
	}

	/**
	 * 
	 */
	public static void createTheMonkeyMenu() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for(int i = 0; i < windows.length; i++) {
			final IWorkbenchWindow window = windows[i];
			window.getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					RecreateMonkeyMenuAction action = new RecreateMonkeyMenuAction();
					action.init(window);
					action.run(null);
				}
			});
		}
	}

}
