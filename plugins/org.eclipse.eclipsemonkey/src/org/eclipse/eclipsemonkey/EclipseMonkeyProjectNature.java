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
package org.eclipse.eclipsemonkey;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Project nature to add to project in order to manage script inside
 * 
 * @author adaussy
 * 
 */
public class EclipseMonkeyProjectNature implements IProjectNature {

	public static String ECLIPSE_MONKEY_NATURE = "org.eclipse.eclipsemonkey.Eclipse_Monkey_Nature";

	private IProject project;

	@Override
	public void configure() throws CoreException {
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	public static boolean isEclipseMonkeyProject(IProject project) {
		if(project != null && project.exists()) {
			try {
				return project.hasNature(ECLIPSE_MONKEY_NATURE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean isEclipseMonkeyResource(IResource resource) {
		if(resource != null) {
			return isEclipseMonkeyProject(resource.getProject());
		}
		return false;
	}

	public static void addEclipseMoneyNature(IProject project) throws CoreException {
		if(project != null) {
			if(!isEclipseMonkeyProject(project)) {
				IProjectDescription description = project.getDescription();
				String[] natures = description.getNatureIds();
				String[] newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = ECLIPSE_MONKEY_NATURE;
				description.setNatureIds(newNatures);
				project.setDescription(description, new NullProgressMonitor());
			}
		}
	}

}
