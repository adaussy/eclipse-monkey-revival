/*******************************************************************************
 * Copyright (c) 2005, 2006 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bjorn Freeman-Benson - initial implementation
 *     Ward Cunningham - initial implementation
 *******************************************************************************/

package org.eclipse.eclipsemonkey.ui.views;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.eclipsemonkey.IScriptStoreListener;
import org.eclipse.eclipsemonkey.ScriptService;
import org.eclipse.eclipsemonkey.StoredScript;
import org.eclipse.eclipsemonkey.Subscription;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/**
 * ScriptsInfoView
 * 
 */
public class ScriptsInfoView extends ViewPart implements IScriptStoreListener {

	private TableViewer viewer;

	class ViewContentProvider implements IStructuredContentProvider {

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object parent) {
			Map<URI, StoredScript> scriptStore = ScriptService.getInstance().getScriptStore();
			return scriptStore.keySet().toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object obj, int index) {
			if(index == 0)
				return getText(obj);
			if(index == 1) {
				StoredScript stored = (StoredScript)ScriptService.getInstance().getScriptStore().get(obj);
				if(stored == null)
					return "";
				return stored.metadata.getMenuName();
			}
			if(index == 2) {
				StoredScript stored = (StoredScript)ScriptService.getInstance().getScriptStore().get(obj);
				if(stored == null)
					return "";
				List<Subscription> subscriptions = stored.metadata.getSubscriptions();
				Iterator<Subscription> iter = subscriptions.iterator();
				String s = "";
				while(iter.hasNext()) {
					Subscription subscription = (Subscription)iter.next();
					s += ", " + subscription;
				}
				if(s.length() > 2)
					s = s.substring(2);
				return s;
			}

			return "?";
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		/**
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		@Override
		public Image getImage(Object obj) {
			return null;
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * ScriptsInfoView
	 */
	public ScriptsInfoView() {
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] HEADINGS = { "Script", "Menu", "Listeners" };

		layout.addColumnData(new ColumnWeightData(20, 100, true));
		TableColumn nameCol = new TableColumn(table, SWT.NONE, 0);
		nameCol.setText(HEADINGS[0]);
		nameCol.setAlignment(SWT.LEFT);
		nameCol.setResizable(true);

		layout.addColumnData(new ColumnWeightData(20, 100, true));
		TableColumn menuCol = new TableColumn(table, SWT.NONE, 1);
		menuCol.setText(HEADINGS[1]);
		menuCol.setAlignment(SWT.LEFT);
		menuCol.setResizable(true);

		layout.addColumnData(new ColumnWeightData(20, 100, true));
		TableColumn typeCol = new TableColumn(table, SWT.NONE, 2);
		typeCol.setText(HEADINGS[2]);
		typeCol.setAlignment(SWT.LEFT);
		typeCol.setResizable(true);

		viewer = new TableViewer(table);

		// viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
		// | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		ScriptService.getInstance().addScriptStoreListener(this);
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		ScriptService.getInstance().removeScriptStoreListener(this);
	}

	/**
	 * @see org.eclipse.eclipsemonkey.IScriptStoreListener#storeChanged()
	 */
	@Override
	public void storeChanged() {
		Display display = viewer.getControl().getDisplay();
		if(!display.isDisposed()) {
			display.asyncExec(new Runnable() {

				@Override
				public void run() {
					if(viewer.getControl().isDisposed())
						return;
					viewer.refresh();
				}
			});
		}
	}

	private void hookContextMenu() {
		// MenuManager menuMgr = new MenuManager("#PopupMenu");
		// menuMgr.setRemoveAllWhenShown(true);
		// menuMgr.addMenuListener(new IMenuListener() {
		// public void menuAboutToShow(IMenuManager manager) {
		// ScriptsView.this.fillContextMenu(manager);
		// }
		// });
		// Menu menu = menuMgr.createContextMenu(viewer.getControl());
		// viewer.getControl().setMenu(menu);
		// getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		// IActionBars bars = getViewSite().getActionBars();
		// fillLocalPullDown(bars.getMenuManager());
		// fillLocalToolBar(bars.getToolBarManager());
	}

	// private void fillLocalPullDown(IMenuManager manager) {
	// manager.add(action1);
	// manager.add(new Separator());
	// manager.add(action2);
	// }
	//
	// private void fillContextMenu(IMenuManager manager) {
	// manager.add(action1);
	// manager.add(action2);
	// // Other plug-ins can contribute there actions here
	// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	// }
	//
	// private void fillLocalToolBar(IToolBarManager manager) {
	// manager.add(action1);
	// manager.add(action2);
	// }

	private void makeActions() {
		// action1 = new Action() {
		// public void run() {
		// showMessage("Action 1 executed");
		// }
		// };
		// action1.setText("Action 1");
		// action1.setToolTipText("Action 1 tooltip");
		// action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
		// .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		//
		// action2 = new Action() {
		// public void run() {
		// showMessage("Action 2 executed");
		// }
		// };
		// action2.setText("Action 2");
		// action2.setToolTipText("Action 2 tooltip");
		// action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
		// .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		// doubleClickAction = new Action() {
		// public void run() {
		// ISelection selection = viewer.getSelection();
		// Object obj = ((IStructuredSelection) selection)
		// .getFirstElement();
		// showMessage("Double-click detected on " + obj.toString());
		// }
		// };
	}

	private void hookDoubleClickAction() {
		// viewer.addDoubleClickListener(new IDoubleClickListener() {
		// public void doubleClick(DoubleClickEvent event) {
		// doubleClickAction.run();
		// }
		// });
	}

	// private void showMessage(String message) {
	// MessageDialog.openInformation(viewer.getControl().getShell(),
	// "Sample View", message);
	// }

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
