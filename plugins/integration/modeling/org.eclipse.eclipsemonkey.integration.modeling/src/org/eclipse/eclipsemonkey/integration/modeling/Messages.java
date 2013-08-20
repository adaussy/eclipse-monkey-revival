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
 *  Vincent Hemery (Atos Origin) - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.eclipsemonkey.integration.modeling;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * This class handles internationalization of messages
 * 
 * @author vhemery
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = Activator.PLUGIN_ID.concat(".messages"); //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Private constructor
     */
    private Messages()
    {
    }

    /**
     * Get the translated string
     * 
     * @param key message key
     * @return message
     */
    public static String getString(String key)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }
    }
}
