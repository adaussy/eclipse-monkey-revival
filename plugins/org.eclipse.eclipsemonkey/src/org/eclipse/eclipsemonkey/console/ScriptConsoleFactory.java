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
package org.eclipse.eclipsemonkey.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

/**
 * The factory to create the Topcased Scripting Console.
 * 
 * @author <a href="mailto:pierre-charles.david@obeo.fr">Pierre-Charles David</a>
 */
public class ScriptConsoleFactory implements IConsoleFactory
{
    private IConsoleManager fConsoleManager = null;
    private ScriptConsole fConsole = null;

    public ScriptConsoleFactory() {
        fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
        fConsoleManager.addConsoleListener(new IConsoleListener() {
            public void consolesAdded(IConsole[] consoles) {
            }

            public void consolesRemoved(IConsole[] consoles) {
                for (int i = 0; i < consoles.length; i++) {
                    if(consoles[i] == fConsole) {
                        fConsole = null;
                    }
                }
            }
        
        });
    }
    
    public ScriptConsole getConsole()
    {
        return fConsole;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsoleFactory#openConsole()
     */
    public void openConsole() {
        if (fConsole == null) {
            fConsole = new ScriptConsole(); 
            fConsoleManager.addConsoles(new IConsole[]{fConsole});
        }
        fConsoleManager.showConsoleView(fConsole);
    }
}
