/*******************************************************************************
 * Copyright (c) 2008 AIRBUS FRANCE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Charles David (Obeo) - initial API and implementation
 *    Vincent Hemery (Atos Origin) - removing modeler dependencies
 *******************************************************************************/
package org.eclipse.eclipsemonkey.integration.modeling;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.eclipsemonkey.integration.modeling.extensions.GetSelectionManager;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * A base class which provides access to meta-model independent objects and services for Modeling scripts. This class is
 * not exposed directly as a DOM in this plug-in. It is made to be extended and customized by meta-model specific DOMs
 * in other plug-ins.
 * 
 * @author <a href="mailto:pierre-charles.david@obeo.fr">Pierre-Charles David</a>
 * @author <a href="mailto:laurent.redor@obeo.fr">Laurent Redor</a>
 */
public abstract class BasicModelingDOM {

	/**
	 * Returns the currently selected model element, either in the editor or the outline view. If several elements are
	 * selected, only the first is returned.
	 * 
	 * @return the currently selected model element.
	 */
	public EObject getSelection() {
		EObject result = null;
		ISelection selection = getEditorSelection();
		if(selection instanceof ITreeSelection) {
			Object firstElement = ((ITreeSelection)selection).getFirstElement();
			if(firstElement instanceof EObject) {
				return (EObject)firstElement;
			}
		} else if(selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			result = GetSelectionManager.getInstance().getSelection(element);
		}
		return result;
	}

	/**
	 * The current editor part is return or null if there is any active editor. In the case of there is any active
	 * editor a message is display to inform the user.
	 * 
	 * @return IEditorPart The current editor part or null
	 */
	protected IEditorPart getCurrentEditorPart() {
		/**
		 * ActiveEditorRef
		 */
		class ActiveEditorRef {

			public IEditorPart activeEditorPart = null;
		}

		final IWorkbench workbench = PlatformUI.getWorkbench();
		final ActiveEditorRef activeEditorRef = new ActiveEditorRef();
		Display display = workbench.getDisplay();

		display.syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

				// this can be null if you close all perspectives
				if(window != null && window.getActivePage() != null && window.getActivePage().getActiveEditor() != null) {
					activeEditorRef.activeEditorPart = window.getActivePage().getActiveEditor();
				} else {
					Shell shell;
					if(window != null && window.getShell() != null) {
						shell = window.getShell();
					} else {
						shell = new Shell();
					}

					MessageDialog.openError(shell, "Modeling Scripting", "There is no active editor.");
				}
			}
		});
		return activeEditorRef.activeEditorPart;
	}

	/**
	 * Return the current editor selection. Default implementation handles graphical GEF/GMF editor. Subclasses may
	 * override this implementation to provide support for more editors.
	 * 
	 * @return ISelection The current editor selection
	 */
	protected ISelection getEditorSelection() {
		IEditorPart editorPart = getCurrentEditorPart();
		ISelection selection = null;

		Object viewer = editorPart.getAdapter(org.eclipse.gef.GraphicalViewer.class);
		if(viewer instanceof GraphicalViewer) {
			// GEF/GMF diagram editor
			selection = ((GraphicalViewer)viewer).getSelection();
		}
		return selection;
	}

	/**
	 * Save the current editor
	 */
	public void save() {
		getCurrentEditorPart().doSave(new NullProgressMonitor());
	}

	protected Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/**
	 * Run an operation in the current editor's command stack
	 * 
	 * @param operation
	 *        the operation to run
	 * @param operationName
	 *        the name to give to the operation execution
	 */
	public void runOperation(final Runnable operation, String operationName) {
		Object domain = getCurrentEditorPart().getAdapter(EditingDomain.class);
		if(domain instanceof EditingDomain) {
			// execute the operation in a command
			((EditingDomain)domain).getCommandStack().execute(new AbstractCommand(operationName) {

				/**
				 * Execute the operation
				 */
				@Override
				public void execute() {
					operation.run();
				}

				/**
				 * Execute the operation
				 */
				@Override
				public void redo() {
					execute();
				}

				/**
				 * Return true
				 */
				@Override
				protected boolean prepare() {
					return true;
				}

			});
		} else {
			// try simply running the operation
			operation.run();
		}
	}

}
