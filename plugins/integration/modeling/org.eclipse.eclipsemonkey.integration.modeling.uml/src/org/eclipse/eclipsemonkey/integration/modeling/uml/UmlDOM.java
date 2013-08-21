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
package org.eclipse.eclipsemonkey.integration.modeling.uml;

import org.eclipse.eclipsemonkey.integration.modeling.BasicModelingDOM;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.editor.presentation.UMLEditor;

/**
 * This DOM gives access to UML-specific objects and services in Topcased scripts.
 * 
 * @author <a href="mailto:pierre-charles.david@obeo.fr">Pierre-Charles David</a>
 */
public class UmlDOM extends BasicModelingDOM
{
    /**
     * Return the UML factory object which can be use to create instances of any of the UML meta-classes.
     * 
     * @return the UML factory.
     */
    public UMLFactory getFactory()
    {
        return UMLFactory.eINSTANCE;
    }

    /**
     * Create a new UML class with the specified name and return it.
     * 
     * @param name the name to give to the new class
     * @return the new class
     */
    public Class createClass(String name)
    {
        Class klass = getFactory().createClass();
        klass.setName(name);
        return klass;
    }

    /**
     * Create a new UML class with the specified name, put it into the given package and return it.
     * 
     * @param parent the package in which to put the new class
     * @param name the name to give to the new class
     * @return the new class
     */
    public Class createClass(Package parent, String name)
    {
        Class klass = createClass(name);
        klass.setPackage(parent);
        return klass;
    }

    /**
     * Create a new UML package, set its name as specified and return it.
     * 
     * @param name the name to give to the new package.
     * @return the new package.
     */
    public Package createPackage(String name)
    {
        Package pkg = getFactory().createPackage();
        pkg.setName(name);
        return pkg;
    }

    /**
     * Returns the currently selected model element in the current UML editor if it is an instance of the named
     * meta-class (or a sub-class).
     * 
     * @param umlTypeName the name of a UML meta-class (e.g. "Property" or "Package")
     * @return the first element selected in the current UML editor if there is one and it is an instance of the named
     *         meta-class or a sub-class of it.
     */
    public EObject getSelection(String umlTypeName)
    {
        EObject selection = getSelection();
        if (isA(selection, umlTypeName))
        {
            return selection;
        }
        else
        {
            return null;
        }
    }

    /**
     * Test whether the specified object is an instance of the named UML meta-class (or a sub-class).
     * 
     * @param element the object to test
     * @param umlTypeName the name of a UML meta-class (e.g. "Class" or "Property").
     * @return <code>true</code> iff the element is an instance of the named class or a sub-class.
     */
    public boolean isA(Object element, String umlTypeName)
    {
        EClassifier type = UMLPackage.eINSTANCE.getEClassifier(umlTypeName);
        if (type == null)
        {
            return false;
        }
        else
        {
            return element != null && type.isInstance(element);
        }
    }

    /**
     * Return the current selection in the UmlEditor The UmlEditor is one of the TreeView Editor or Diagram Editor (the
     * Outline selection is not considered)
     * 
     * @return ISelection The current editor selection
     */
    @Override
    protected ISelection getEditorSelection()
    {
        IEditorPart editorPart = getCurrentEditorPart();
        ISelection selection = super.getEditorSelection();

        if (selection == null && editorPart instanceof UMLEditor)
        {
            // Treeview editor
            UMLEditor umlEditor = (UMLEditor) editorPart;
            selection = umlEditor.getSelection();
        }
        return selection;
    }
}
