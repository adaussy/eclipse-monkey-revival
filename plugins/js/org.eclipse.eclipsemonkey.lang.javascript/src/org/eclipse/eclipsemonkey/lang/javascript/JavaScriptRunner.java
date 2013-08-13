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

package org.eclipse.eclipsemonkey.lang.javascript;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.eclipsemonkey.DOMDescriptor;
import org.eclipse.eclipsemonkey.ScriptService;
import org.eclipse.eclipsemonkey.IMonkeyScriptRunner;
import org.eclipse.eclipsemonkey.RunMonkeyException;
import org.eclipse.eclipsemonkey.ScriptMetadata;
import org.eclipse.eclipsemonkey.StoredScript;
import org.eclipse.eclipsemonkey.dom.IMonkeyDOMFactory;
import org.eclipse.eclipsemonkey.dom.Utilities;
import org.eclipse.eclipsemonkey.lang.javascript.doms.IJavaScriptDOMFactory;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

/**
 * 
 * @author Ingo Muschenetz
 * 
 */
public class JavaScriptRunner implements IMonkeyScriptRunner {

	IPath path;

	IWorkbenchWindow window;

	StoredScript storedScript;

	ClassLoader old_classloader;

	JavaScriptClassLoader classloader;

	/**
	 * 
	 * @param path
	 * @param window
	 */
	public JavaScriptRunner(IPath path, IWorkbenchWindow window) {
		this.path = path;

		if(window == null) {
			this.window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		} else {
			this.window = window;
		}
	}

	/**
	 * @see org.eclipse.eclipsemonkey.IMonkeyScriptRunner#getStoredScript()
	 */
	public StoredScript getStoredScript() {
		return storedScript;
	}

	/**
	 * @see org.eclipse.eclipsemonkey.IMonkeyScriptRunner#run(java.lang.String, java.lang.Object[])
	 */
	public Object run(String entryName, Object[] functionArgs) throws RunMonkeyException {
		Object result = null;

		try {
			Context cx = Context.enter();

			Scriptable sharedScope = null;
			URI uri = URIUtil.toURI(this.path.toPortableString());
			Map<URI, StoredScript> scriptStore = ScriptService.getInstance().getScriptStore();

			storedScript = (StoredScript)(scriptStore.get(uri));

			if(!storedScript.metadata.ensure_doms_are_loaded(window)) {
				return null;
			}

			String sharedScopeName = storedScript.metadata.getScopeName();

			if(sharedScopeName != null) {
				Map<String, Object> scopeStore = ScriptService.getInstance().getScopeStore();
				sharedScope = (Scriptable)scopeStore.get(sharedScopeName);

				if(sharedScope == null) {
					sharedScope = new JavaScriptGlobal(cx);
					scopeStore.put(sharedScopeName, sharedScope);
				}
			}

			defineDynamicVariables(path);
			defineClassLoader(cx);

			try {
				Scriptable compiledScope = (Scriptable)storedScript.extra.get("compiledScope");

				boolean needs_compiling = compiledScope == null;

				if(needs_compiling) {
					compiledScope = new JavaScriptGlobal(cx);
					storedScript.extra.put("compiledScope", compiledScope);

					if(sharedScope != null) {
						compiledScope.setParentScope(sharedScope);
					}
				}

				defineStandardGlobalVariables(compiledScope);
				defineExtensionGlobalVariables(compiledScope, storedScript.metadata);

				if(needs_compiling) {
					String contents = Utilities.getFileContents(path);
					Script compiledScript = cx.compileString(contents, URIUtil.toPath(uri).toPortableString(), 1, null);

					// place path in script's global
					compiledScope.put(JavaScriptGlobal.LOCATION_PROPERTY, compiledScope, this.path);

					compiledScript.exec(cx, compiledScope);
					storedScript.extra.put("compiledScript", compiledScript);
					//Ckeck if necessary?
					//					EclipseMonkeyPlugin.getDefault().notifyScriptsChanged();
				}

				Object fObj = compiledScope.get(entryName, compiledScope);

				if(!(fObj instanceof Function)) {
					throw new EvaluatorException("function " + entryName + "() is not defined in ", uri.getPath(), 0, "", 0);
				} else {
					Function f = (Function)fObj;
					result = f.call(cx, compiledScope, compiledScope, functionArgs);
				}

			} finally {
				undefineClassLoader(cx);
				undefineDynamicVariables(path);
			}
		} catch (WrappedException x) {
			error(x, x.getWrappedException().toString());
		} catch (EvaluatorException x) {
			error(x, x.lineSource() + "\n" + x.details());
		} catch (RhinoException x) {
			error(x, x.details());
		} catch (IOException x) {
			error(x, this.path.toString(), x.toString());
		} catch (CoreException x) {
			error(x, this.path.toString(), x.toString());
		} finally {
			Context.exit();
		}

		if(result instanceof NativeJavaObject) {
			result = ((NativeJavaObject)result).unwrap();
		}

		return result;
	}

	private void defineStandardGlobalVariables(Scriptable scope) {
		Object wrappedWindow = Context.javaToJS(window, scope);
		ScriptableObject.putProperty(scope, "window", wrappedWindow);
	}

	private void defineExtensionGlobalVariables(Scriptable scope, ScriptMetadata metadata) throws IOException {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint("org.eclipse.eclipsemonkey.dom");
		if(point != null) {
			IExtension[] extensions = point.getExtensions();
			for(int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurations = extension.getConfigurationElements();
				for(int j = 0; j < configurations.length; j++) {
					IConfigurationElement element = configurations[j];
					try {
						IExtension declaring = element.getDeclaringExtension();

						String declaring_plugin_id = declaring.getDeclaringPluginDescriptor().getUniqueIdentifier();
						//						String declaring_plugin_id = declaring.getNamespaceIdentifier();

						if(metadata.containsDOM_by_plugin(declaring_plugin_id)) {
							String variableName = element.getAttribute("variableName");
							Object object = element.createExecutableExtension("class");
							IMonkeyDOMFactory factory = (IMonkeyDOMFactory)object;

							Object rootObject = factory.getDOMroot();

							//							ClassLoader rootLoader = rootObject.getClass()
							//									.getClassLoader();
							//							classloader.add(rootLoader);
							Object wrappedRoot = Context.javaToJS(rootObject, scope);
							ScriptableObject.putProperty(scope, variableName, wrappedRoot);
						}
					} catch (InvalidRegistryObjectException x) {
						// ignore bad extensions
					} catch (CoreException x) {
						// ignore bad extensions
					}
				}
			}
		}

		point = registry.getExtensionPoint("org.eclipse.eclipsemonkey.lang.javascript.javascript_dom");

		if(point != null) {
			IExtension[] extensions = point.getExtensions();
			for(int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurations = extension.getConfigurationElements();
				for(int j = 0; j < configurations.length; j++) {
					IConfigurationElement element = configurations[j];
					try {
						IExtension declaring = element.getDeclaringExtension();

						//				String declaring_plugin_id = declaring
						//						.getDeclaringPluginDescriptor()
						//						.getUniqueIdentifier();
						String declaring_plugin_id = declaring.getNamespaceIdentifier();

						if(metadata.containsDOM_by_plugin(declaring_plugin_id)) {

							String variableName = element.getAttribute("variableName");

							String basedOnDOM = element.getAttribute("basedOn");

							if(basedOnDOM != null && basedOnDOM.trim().length() > 0) {
								Pattern p = Pattern.compile("\\s*(\\p{Graph}+)\\/((\\p{Alnum}|\\.)+)", Pattern.DOTALL);
								Matcher m = p.matcher(basedOnDOM);
								while(m.find()) {
									metadata.getDOMs().add(new DOMDescriptor(m.group(1), m.group(2)));
								}

								if(metadata.ensure_doms_are_loaded(window) == false) {
									throw new IOException("Cannot load the required DOM extension:\n\n" + basedOnDOM + "\n");
								}
							}

							Object object = element.createExecutableExtension("class");

							IJavaScriptDOMFactory factory = (IJavaScriptDOMFactory)object;

							Object rootObject = factory.getDOMroot(scope);

							//							ClassLoader rootLoader = rootObject.getClass().getClassLoader();
							//							classloader.add(rootLoader);
							Object wrappedRoot = Context.javaToJS(rootObject, scope);
							ScriptableObject.putProperty(scope, variableName, wrappedRoot);
						}
					} catch (InvalidRegistryObjectException x) {
						// ignore bad extensions
					} catch (CoreException x) {
						// ignore bad extensions
					}
				}
			}
		}

	}

	private void error(RhinoException x, String string) throws RunMonkeyException {

		RunMonkeyException e = new RunMonkeyException(x.getClass().getName(), x.sourceName(), new Integer(x.lineNumber()), string);

		MessageConsoleStream cs = JavaScriptGlobal.getConsoleStream();
		cs.println(e.toString());

		throw e;
	}

	private void defineDynamicVariables(IPath path) {
		Utilities.state().begin(path);
		Utilities.state().set(Utilities.SCRIPT_NAME, path.toPortableString());
	}

	private void undefineDynamicVariables(IPath path) {
		Utilities.state().end(path);
	}

	private void error(Exception x, String fileName, String string) throws RunMonkeyException {

		RunMonkeyException e = new RunMonkeyException(x.getClass().getName(), fileName, null, string);

		MessageConsoleStream cs = JavaScriptGlobal.getConsoleStream();
		cs.println(e.toString());

		throw e;
	}

	private void defineClassLoader(Context cx) {
		old_classloader = cx.getApplicationClassLoader();
		classloader = new JavaScriptClassLoader();
		cx.setApplicationClassLoader(classloader);
	}

	private void undefineClassLoader(Context cx) {
		cx.setApplicationClassLoader(old_classloader);
	}
}
