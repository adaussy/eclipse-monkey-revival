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

import org.eclipse.eclipsemonkey.dom.IMonkeyDOMFactory;
/**
 * The factory which provides the {@link UmlDOM} to Eclipse Monkey.
 * 
 * @author <a href="mailto:pierre-charles.david@obeo.fr">Pierre-Charles David</a>
 */
public class DOMFactory implements IMonkeyDOMFactory
{
    private UmlDOM domInstance = new UmlDOM();

    public Object getDOMroot()
    {
        return domInstance;
    }
}
