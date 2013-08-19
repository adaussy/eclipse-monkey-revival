/**
 * Copyright (c) 2005-2006 Aptana, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code, this entire header must remain intact.
 */
package org.eclipse.eclipsemonkey.ui.views.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.eclipsemonkey.IScriptStoreListener;
import org.eclipse.eclipsemonkey.MenuRunMonkeyScript;
import org.eclipse.eclipsemonkey.RunMonkeyException;
import org.eclipse.eclipsemonkey.ScriptService;
import org.eclipse.eclipsemonkey.ui.EclipseMonkeyUIActivator;
import org.eclipse.eclipsemonkey.ui.IScriptAction;
import org.eclipse.eclipsemonkey.ui.IScriptActionSet;
import org.eclipse.eclipsemonkey.ui.IScriptActionsViewEventListener;
import org.eclipse.eclipsemonkey.ui.IScriptUI;
import org.eclipse.eclipsemonkey.ui.IconPath;
import org.eclipse.eclipsemonkey.ui.ScriptActionsManager;
import org.eclipse.eclipsemonkey.ui.data.ScriptAction;
import org.eclipse.eclipsemonkey.ui.data.ScriptActionSet;
import org.eclipse.eclipsemonkey.ui.event.ScriptActionsViewEvent;
import org.eclipse.eclipsemonkey.ui.event.ScriptActionsViewEventTypes;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

/**
 * ScriptsView
 * 
 * @author Paul Colton (Aptana, Inc.)
 */
public class ScriptsView extends ViewPart implements IScriptStoreListener {

	/*
	 * Fields
	 */
	static final String INFO_MESSAGE = "\nEclipse Monkey Scripts View.";

	static final String[] FILTER_EXTENSIONS = new String[]{ "*.js", "*.*" };

	static final String[] FILTER_NAMES = new String[]{ "JavaScript Files (" + FILTER_EXTENSIONS[0] + ")", "All Files (" + FILTER_EXTENSIONS[1] + ")" };

	private TreeViewer viewer;

	private StackLayout layout;

	private Composite viewParent;

	private Label infoLabel;

	private Font infoLabelFont;

	private org.eclipse.jface.action.Action actionNewActionSet;

	private org.eclipse.jface.action.Action actionAdd;

	private org.eclipse.jface.action.Action actionEdit;

	private org.eclipse.jface.action.Action actionReload;

	private Action actionRefresh;

	private org.eclipse.jface.action.Action actionDelete;

	private org.eclipse.jface.action.Action actionDoubleClick;

	private org.eclipse.jface.action.Action actionAddCurrentFile;

	private org.eclipse.jface.action.Action actionExecute;

	private org.eclipse.jface.action.Action actionMakeExecutable;

	private ArrayList<IScriptActionsViewEventListener> listeners = new ArrayList<IScriptActionsViewEventListener>();

	private ScriptActionsManager _scriptActionsManager;

	/*
	 * Constructor.
	 */

	/**
	 * ScriptsView
	 */
	public ScriptsView() {
		_scriptActionsManager = ScriptActionsManager.getInstance();
	}

	/*
	 * Methods
	 */

	/**
	 * fireActionsViewEvent
	 * 
	 * @param e
	 */
	public void fireActionsViewEvent(ScriptActionsViewEvent e) {
		for(int i = 0; i < listeners.size(); i++) {
			IScriptActionsViewEventListener listener = listeners.get(i);
			listener.onScriptActionsViewEvent(e);
		}
	}

	/**
	 * addActionsViewEventListener
	 * 
	 * @param l
	 */
	public void addScriptsViewEventListener(IScriptActionsViewEventListener l) {
		listeners.add(l);
	}

	/**
	 * removeActionsViewEventListener
	 * 
	 * @param l
	 */
	public void removeScriptsViewEventListener(IScriptActionsViewEventListener l) {
		listeners.remove(l);
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
					_scriptActionsManager.clearAll();
					viewer.refresh();
					viewer.expandAll();
				}
			});
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if(infoLabelFont != null) {
			infoLabelFont.dispose();
		}

		ScriptService.getInstance().removeScriptStoreListener(this);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		layout = new StackLayout();
		parent.setLayout(layout);

		viewer = new TreeViewer(new Tree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL));
		viewer.setContentProvider(new ScriptsViewContentProvider());
		viewer.setLabelProvider(new ScriptsViewLabelProvider());
		viewer.setSorter(new ScriptsViewSorterProvider());
		viewer.setInput(getViewSite());
		viewer.expandAll();

		infoLabel = new Label(parent, SWT.CENTER);
		infoLabel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		infoLabelFont = new Font(parent.getDisplay(), "Arial", 14, SWT.NONE);
		infoLabel.setFont(infoLabelFont);
		infoLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		infoLabel.setText(INFO_MESSAGE);
		layout.topControl = infoLabel;
		layout.topControl = viewer.getControl();
		viewParent = parent;
		viewParent.layout();

		final DropTarget labeldt = new DropTarget(infoLabel, DND.DROP_MOVE);

		labeldt.setTransfer(new Transfer[]{ FileTransfer.getInstance() });
		labeldt.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(DropTargetEvent event) {
				handleDrop(event);
			}
		});

		DropTarget dt = new DropTarget(viewer.getControl(), DND.DROP_MOVE);
		dt.setTransfer(new Transfer[]{ FileTransfer.getInstance() });
		dt.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(DropTargetEvent event) {
				handleDrop(event);
			}
		});

		makeActions();
		hookKeyActions(viewer.getControl());
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		ScriptService.getInstance().addScriptStoreListener(this);
	}

	/**
	 * handleDrop
	 * 
	 * @param event
	 */
	protected void handleDrop(DropTargetEvent event) {
		String[] files = (String[])event.data;
		ArrayList<Path> paths = new ArrayList<Path>();

		for(int i = 0; i < files.length; i++) {
			paths.add(new Path(files[i]));
		}

		if(paths.size() > 0) {
			IPath[] ipaths = paths.toArray(new IPath[0]);
			ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.DROP);

			e.setPaths(ipaths);

			Widget w = event.item;

			if(w != null) {
				TreeItem item = (TreeItem)w;
				Object element = item.getData();
				IScriptUI action = null;

				if(element instanceof ScriptActionSet) {
					action = (ScriptActionSet)element;
				} else if(element instanceof ScriptAction) {
					action = ((ScriptAction)element).getParent();
				}

				if(action != null) {
					e.setName(action.getName());
				}
			}

			fireActionsViewEvent(e);
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = viewer.getSelection();
				Object firstElement = ((IStructuredSelection)selection).getFirstElement();
				ScriptsView.this.fillContextMenu(manager, firstElement);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
		// manager.add(actionAdd);
		// manager.add(actionNewActionSet);
		manager.add(actionEdit);
		manager.add(actionRefresh);
		// manager.add(actionDelete);
	}

	private void fillContextMenu(IMenuManager manager, Object element) {

		if(element instanceof ScriptActionSet) {
			ScriptActionSet set = (ScriptActionSet)element;

			if(set.isExecutable()) {
				manager.add(actionExecute);
			}

			manager.add(actionMakeExecutable);
		} else {
			manager.add(actionExecute);
		}

		manager.add(new Separator());
		// manager.add(actionAddCurrentFile);

		if(element instanceof ScriptActionSet == false) {
			manager.add(actionEdit);
		}

		//manager.add(actionReload);
		// manager.add(actionDelete);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		// manager.add(actionNewActionSet);
		// manager.add(actionDelete);
		manager.add(actionRefresh);
	}

	class PushButtonAction extends Action {

		/**
		 * PushButtonAction
		 * 
		 * @param text
		 */
		public PushButtonAction(String text) {
			super(text, Action.AS_PUSH_BUTTON);
		}
	}

	private void makeActions() {

		actionRefresh = new PushButtonAction("Refresh") {

			@Override
			public void run() {
				_scriptActionsManager.clearAll();
				viewer.refresh();
			}
		};
		actionRefresh.setToolTipText("Refresh");
		actionRefresh.setImageDescriptor(EclipseMonkeyUIActivator.getLocalImageDescriptor(IconPath.REFRESH_ICON_PATH));

		actionMakeExecutable = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				ISelection selection = viewer.getSelection();

				if(selection != null && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection)selection;
					Object element = structuredSelection.getFirstElement();
					if(element instanceof ScriptActionSet) {
						if(((ScriptActionSet)element).isExecutable()) {
							((ScriptActionSet)element).setExecutable(false);
						} else {
							((ScriptActionSet)element).setExecutable(true);
						}

						viewer.refresh();
					}
				}

			}
		};
		actionMakeExecutable.setText("Toggle Executable");
		actionMakeExecutable.setToolTipText("Toggle whether or not this set is executable.");

		actionExecute = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.EXECUTE);
				ISelection selection = viewer.getSelection();

				if(selection != null && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection)selection;
					Object element = structuredSelection.getFirstElement();
					Collection<? extends IScriptUI> actions;

					if(element instanceof ScriptAction) {
						actions = Collections.singleton((IScriptAction)element);
						executeScript((ScriptAction)element);
					} else if(element instanceof ScriptActionSet) {
						List<IScriptAction> actions2 = ((IScriptActionSet)element).getScriptActions();
						for(Iterator<IScriptAction> iterator = actions2.iterator(); iterator.hasNext();) {
							IScriptAction iScriptAction = (IScriptAction)iterator.next();
							executeScript(iScriptAction);
						}
						actions = actions2;
					} else {
						actions = Collections.emptyList();
					}

					e.getUIScript().addAll(actions);
				}

				fireActionsViewEvent(e);
			}
		};
		actionExecute.setText("Execute");
		actionExecute.setToolTipText("Execute this action or action set.");

		actionAddCurrentFile = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.ADD_CURRENT_FILE);
				ISelection selection = viewer.getSelection();
				if(selection != null) {
					Object firstElement = ((IStructuredSelection)selection).getFirstElement();
					if(firstElement instanceof ScriptActionSet) {
						e.setName(((ScriptActionSet)firstElement).getName());
					}
				}

				fireActionsViewEvent(e);
			}
		};
		actionAddCurrentFile.setText("Add Current File");
		actionAddCurrentFile.setToolTipText("Add the current file to this profile.");

		actionNewActionSet = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				InputDialog input = new InputDialog(getSite().getShell(), "New Action Set Name", "Please enter new Action Set name", org.eclipse.eclipsemonkey.utils.StringUtils.EMPTY, null);

				if(input.open() == Window.OK && input.getValue().length() > 0) {
					ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.CREATE_ACTION_SET);
					e.setName(input.getValue());
					fireActionsViewEvent(e);
				}
			}
		};
		actionNewActionSet.setText("New Script Set");
		actionNewActionSet.setToolTipText("New Script Set");
		actionNewActionSet.setImageDescriptor(EclipseMonkeyUIActivator.getLocalImageDescriptor(IconPath.ADD_FILE_ICON_PATH));

		actionEdit = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				ISelection selection = viewer.getSelection();

				if(selection != null) {
					Object firstElement = ((IStructuredSelection)selection).getFirstElement();
					if(firstElement instanceof ScriptAction) {
						ScriptAction a = (ScriptAction)firstElement;
						editAction(a);
					}
				}
			}
		};
		actionEdit.setText("Edit Script");
		actionEdit.setToolTipText("Edit Script");

		actionReload = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				ISelection selection = viewer.getSelection();

				if(selection != null) {
					Object firstElement = ((IStructuredSelection)selection).getFirstElement();
					if(firstElement instanceof ScriptAction) {
						ScriptAction a = (ScriptAction)firstElement;
						reloadAction(a);
					}
				}
			}
		};
		actionReload.setText("Reload Script");
		actionReload.setToolTipText("Reload Script");

		actionAdd = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				FileDialog fileDialog = new FileDialog(viewer.getControl().getShell(), SWT.MULTI);
				fileDialog.setFilterExtensions(FILTER_EXTENSIONS);
				fileDialog.setFilterNames(FILTER_NAMES);
				String text = fileDialog.open();
				if(text != null) {
					IPath basePath = new Path(fileDialog.getFilterPath());
					String[] fileNames = fileDialog.getFileNames();
					IPath[] paths = new IPath[fileNames.length];

					for(int i = 0; i < paths.length; i++) {
						paths[i] = basePath.append(fileNames[i]);
					}

					ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.ADD);
					e.setPaths(paths);
					fireActionsViewEvent(e);
				}
			}
		};

		actionAdd.setText("Add File");
		actionAdd.setToolTipText("Add File");
		actionAdd.setImageDescriptor(EclipseMonkeyUIActivator.getLocalImageDescriptor(IconPath.ADD_FILE_ICON_PATH));

		actionDelete = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				removeFiles(viewer.getSelection());
			}
		};
		actionDelete.setText("Remove File");
		actionDelete.setToolTipText("Remove File");
		actionDelete.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

		actionDoubleClick = new org.eclipse.jface.action.Action() {

			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				Object firstElement = ((IStructuredSelection)selection).getFirstElement();

				if(firstElement instanceof ScriptActionSet) {
					toggleElementState(firstElement);
				} else if(firstElement instanceof IScriptAction) {
					ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.EXECUTE);
					e.getUIScript().add((IScriptAction)firstElement);

					fireActionsViewEvent(e);

					executeScript((ScriptAction)firstElement);
				}
			}
		};
	}

	/**
	 * executeScript
	 * 
	 * @param script
	 */
	private void executeScript(IScriptAction script) {
		MenuRunMonkeyScript run = new MenuRunMonkeyScript(script.getStoredScript().scriptPath);
		try {
			run.run("main", new Object[]{});
		} catch (RunMonkeyException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * toggleElementState
	 * 
	 * @param element
	 */
	private void toggleElementState(Object element) {
		boolean state = viewer.getExpandedState(element);

		if(state) {
			viewer.setExpandedState(element, false);
		} else {
			viewer.setExpandedState(element, true);
		}
	}

	/**
	 * Removes one or more files
	 * 
	 * @param selection
	 *        The currently selected files
	 */
	private void removeFiles(ISelection selection) {
		if(!(selection instanceof StructuredSelection)) {
			return;
		}

		Object o = ((StructuredSelection)selection).getFirstElement();

		if(o == null) {
			return;
		}

		if(o instanceof ScriptActionSet) {
			ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.DELETE_ACTION_SET);
			e.setName(((ScriptActionSet)o).getName());
			fireActionsViewEvent(e);
		} else {

			ArrayList<IScriptUI> actionsList = new ArrayList<IScriptUI>();

			for(Iterator iter = ((StructuredSelection)selection).iterator(); iter.hasNext();) {
				Object s = iter.next();
				if(s instanceof IScriptUI) {
					IScriptUI scriptUI = (IScriptUI)s;
					actionsList.add(scriptUI);
				}
			}

			if(actionsList.size() > 0) {
				ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.DELETE);
				e.getUIScript().addAll(actionsList);
				fireActionsViewEvent(e);
			}
		}
	}

	/**
	 * hookDoubleClickAction
	 */
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				actionDoubleClick.run();
			}
		});
	}

	/**
	 * hookKeyActions
	 * 
	 * @param control
	 */
	private void hookKeyActions(Control control) {
		control.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.character == SWT.DEL) {
					removeFiles(viewer.getSelection());
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * expandAll
	 */
	public void expandAll() {
		this.viewer.expandAll();
	}

	/**
	 * expandActionSet
	 * 
	 * @param setName
	 */
	public void expandScriptActionSet(String setName) {
		TreeItem[] treeItems = viewer.getTree().getItems();

		for(int i = 0; i < treeItems.length; i++) {
			Object o = treeItems[i].getData();

			if(o instanceof ScriptActionSet) {
				ScriptActionSet p = (ScriptActionSet)o;
				String name = p.getName();
				if(name.equals(setName)) {
					viewer.setExpandedState(o, true);
					viewer.getTree().showItem(treeItems[i]);
					return;
				}
			}
		}

	}

	/**
	 * selectAndReveal
	 * 
	 * @param actionPath
	 */
	public void selectAndReveal(String actionPath) {
		IScriptAction a = findAction(actionPath);

		if(a != null && a instanceof ScriptAction) {
			selectAndReveal((ScriptAction)a);
		}
	}

	/**
	 * selectAndReveal
	 * 
	 * @param action
	 */
	public void selectAndReveal(ScriptAction action) {
		TreeItem[] treeItems = viewer.getTree().getItems();

		forcePopulateTree();

		for(int i = 0; i < treeItems.length; i++) {
			Object o = treeItems[i].getData();

			if(o instanceof ScriptActionSet) {
				TreeItem[] children = treeItems[i].getItems();

				for(int j = 0; j < children.length; j++) {
					IScriptAction a = (IScriptAction)children[j].getData();

					if(a == action) {
						viewer.getTree().showItem(children[j]);
						viewer.getTree().setSelection(new TreeItem[]{ children[j] });
						return;
					}

				}
			} else if(o instanceof ScriptAction) {
				ScriptAction a = (ScriptAction)o;
				if(a == action) {
					viewer.getTree().showItem(treeItems[i]);
					viewer.getTree().setSelection(new TreeItem[]{ treeItems[i] });
					return;
				}
			}
		}
	}

	/**
	 * forcePopulateTree
	 */
	public void forcePopulateTree() {
		viewer.getTree().setVisible(false);
		Object[] expandedElement = viewer.getExpandedElements();
		viewer.expandAll();
		viewer.setExpandedElements(expandedElement);
		viewer.getTree().setVisible(true);
	}

	/**
	 * Find the action set for the given name
	 * 
	 * @param name
	 *        The action set name to find
	 * @return Returns the matching action set or null;
	 */
	public IScriptActionSet findActionSet(String name) {
		IScriptActionSet result = null;
		TreeItem[] treeItems = viewer.getTree().getItems();

		for(int i = 0; i < treeItems.length; i++) {
			Object itemData = treeItems[i].getData();

			if(itemData instanceof ScriptActionSet) {
				ScriptActionSet actionSet = (ScriptActionSet)itemData;

				if(actionSet.getName().equals(name)) {
					result = actionSet;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * findAction
	 * 
	 * @param path
	 * @return IAction
	 */
	public IScriptAction findAction(String path) {
		IScriptAction result = null;

		if(path != null && path.length() > 0 && path.charAt(0) == '/') {
			int slashIndex = path.indexOf('/', 1);

			if(slashIndex != -1) {
				String actionSetName = path.substring(1, slashIndex);
				String actionName = path.substring(slashIndex + 1);

				IScriptActionSet actionSet = findActionSet(actionSetName);

				if(actionSet != null) {
					List<IScriptAction> actions = actionSet.getScriptActions();
					for(Iterator iterator = actions.iterator(); iterator.hasNext();) {
						IScriptAction iScriptAction = (IScriptAction)iterator.next();
						if(iScriptAction.getName().equals(actionName)) {
							result = iScriptAction;
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * fireAction
	 * 
	 * @param actionName
	 */
	public void fireAction(String actionName) {
		IScriptAction action = findAction(actionName);

		if(action != null) {
			ScriptActionsViewEvent actionEvent = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.EXECUTE);
			actionEvent.getUIScript().add(action);
			fireActionsViewEvent(actionEvent);
		}
	}

	/**
	 * refresh
	 */
	public void refresh() {
		this.viewer.refresh();
	}

	/**
	 * editAction
	 * 
	 * @param path
	 */
	public void editAction(String path) {
		editAction(findAction(path));
	}

	/**
	 * editAction
	 * 
	 * @param a
	 */
	private void editAction(final IScriptAction a) {
		if(a == null) {
			return;
		}

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor editorDesc = null;

		IPath path = a.getStoredScript().metadata.getPath();
		File f = path.toFile();

		try {
			editorDesc = IDE.getEditorDescriptor(f.getName());

			if(editorDesc.isOpenExternal() == true) {
				editorDesc = IDE.getEditorDescriptor("foo.txt");
			}

		} catch (PartInitException e) {
			System.err.println("Error opening file in editor: " + e);
		}

		try {

			IFile t = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			FileEditorInput tre = new FileEditorInput(t);

			IDE.openEditor(page, tre, editorDesc.getId());
		} catch (PartInitException e) {
			System.err.println("Error opening editor: " + e);
		}
	}

	/**
	 * reloadAction
	 * 
	 * @param a
	 */
	private void reloadAction(final IScriptAction a) {
		ScriptActionsViewEvent e = new ScriptActionsViewEvent(ScriptActionsViewEventTypes.RELOAD);
		e.getUIScript().add(a);
		fireActionsViewEvent(e);
	}
}
