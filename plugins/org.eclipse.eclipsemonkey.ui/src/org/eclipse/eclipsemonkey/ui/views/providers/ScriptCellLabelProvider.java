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
package org.eclipse.eclipsemonkey.ui.views.providers;

import org.eclipse.eclipsemonkey.ScriptMetadata;
import org.eclipse.eclipsemonkey.StoredScript;
import org.eclipse.eclipsemonkey.ui.IScriptAction;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;

/**
 * Cell label provider for scripts
 * 
 * @author adaussy
 * 
 */
public class ScriptCellLabelProvider extends CellLabelProvider {

	private ScriptsViewLabelProvider labelProvider = new ScriptsViewLabelProvider();

	@Override
	public String getToolTipText(Object element) {
		if(element instanceof IScriptAction) {
			IScriptAction script = (IScriptAction)element;
			StoredScript storeStript = script.getStoredScript();
			if(storeStript != null) {
				ScriptMetadata metadata = storeStript.metadata;
				if(metadata != null) {
					String desc = metadata.getDescription();
					if(desc != null && !"".equals(desc)) {
						return desc;
					}
				}
			}
		}
		return null;
	}

	@Override
	public Point getToolTipShift(Object object) {
		return new Point(5, 5);
	}

	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}

	@Override
	public int getToolTipTimeDisplayed(Object object) {
		return 10000;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		cell.setText(labelProvider.getText(element));
		cell.setImage(labelProvider.getImage(element));

	}

}
