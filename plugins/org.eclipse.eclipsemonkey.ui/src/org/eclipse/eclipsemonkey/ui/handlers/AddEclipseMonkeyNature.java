package org.eclipse.eclipsemonkey.ui.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.eclipsemonkey.EclipseMonkeyProjectNature;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

public class AddEclipseMonkeyNature extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = getSelection();
		if(selection instanceof IStructuredSelection) {
			for(Iterator<?> it = ((IStructuredSelection)selection).iterator(); it.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if(element instanceof IProject) {
					project = (IProject)element;
				} else if(element instanceof IAdaptable) {
					project = (IProject)((IAdaptable)element).getAdapter(IProject.class);
				}
				if(project != null) {
					toggleNature(project);
				}
			}
		}
		return null;
	}

	private ISelection getSelection() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
	}

	@Override
	public boolean isEnabled() {
		ISelection selection = getSelection();
		if(selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection)selection).getFirstElement();
			return firstElement instanceof IProject;
		}
		return super.isEnabled();
	}

	public void toggleNature(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			// Add the nature
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = EclipseMonkeyProjectNature.ECLIPSE_MONKEY_NATURE;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (CoreException e) {
			//TODO log error
		}
	}

}
