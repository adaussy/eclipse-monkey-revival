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

import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.TextConsole;

/**
 * The Topcased Scripting Console, which scripts can use through <code>output.console()</code> to show textual results to the user.
 * 
 * @author <a href="mailto:pierre-charles.david@obeo.fr">Pierre-Charles David</a>
 */
public class ScriptConsole extends TextConsole
{
    private ScriptConsolePartitioner partitioner;
    
    public ScriptConsole()
    {
        super("Topcased Scripts", "topcasedScriptConsole", null, true);
        partitioner = new ScriptConsolePartitioner();
        partitioner.connect(getDocument());
    }

    @Override
    protected IConsoleDocumentPartitioner getPartitioner()
    {
        return partitioner;
    }
    
    /**
     * Provides a partitioner for this console type
     */
    class ScriptConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner {

        public ScriptConsolePartitioner() {
            super(new RuleBasedPartitionScanner(), null);
            getDocument().setDocumentPartitioner(this);
        }

        public boolean isReadOnly(int offset) {
            return false;
        }

        public StyleRange[] getStyleRanges(int offset, int length) {
            return null;
        }
    }
}
