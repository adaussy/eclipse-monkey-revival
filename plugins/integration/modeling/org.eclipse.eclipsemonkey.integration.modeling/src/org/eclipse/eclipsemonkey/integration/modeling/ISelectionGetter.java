/*****************************************************************************
 * Copyright (c) 2011 Atos Origin.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Vincent Hemery (Atos Origin) {vincent.hemery@atosorigin.com} - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.eclipsemonkey.integration.modeling;

import org.eclipse.emf.ecore.EObject;

/**
 * This interface allows editors to define which model element must be returned, depending on the specific object in the
 * selection
 * 
 * @author vhemery
 * 
 */
public interface ISelectionGetter
{
    /**
     * Returns the currently selected model element, from the editor selected object. Implementing methods should not
     * hesitate to return null when element does not correspond specifically to the object they expected, to let other
     * less specialized implementations handle this case.
     * 
     * @param element the first object in editor selection
     * @return the currently selected model element or null.
     */
    public EObject getSelection(Object element);
}
