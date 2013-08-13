/*******************************************************************************
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 * 
 * Contributions:
 *    Kevin Lindsey based on code by Patrick Mueller
 *    José Fonseca - adapted for python
 *******************************************************************************/

package org.eclipse.eclipsemonkey.lang.python;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.osgi.framework.Bundle;
import org.python.core.PySystemState;

/**
 * @author Kevin Lindsey based on code by Patrick Mueller
 */
public class PythonClassLoader extends ClassLoader {

	private ArrayList<Bundle> _bundles;

	/**
	 * ScriptClassLoader
	 */
	public PythonClassLoader() {
		super(PythonClassLoader.class.getClassLoader());

		this._bundles = new ArrayList<Bundle>();
	}

	/**
	 * addBundle
	 * 
	 * @param bundle
	 */
	public void addBundle(Bundle bundle) {
		if(bundle == null) {
			throw new IllegalArgumentException("ScriptClassLoader_Bundle_Not_Defined");
		}

		if(this._bundles.contains(bundle) == false) {
			this._bundles.add(bundle);

			String packages = (String)bundle.getHeaders().get("Provide-Package");
			if(packages != null) {
				String[] names = packages.split(",");
				for(int j = 0; j < names.length; ++j) {
					PySystemState.add_package(names[j].trim());
				}
			}
			packages = (String)bundle.getHeaders().get("Export-Package");
			if(packages != null) {
				String[] names = packages.split(",");
				for(int j = 0; j < names.length; ++j) {
					PySystemState.add_package(names[j].trim());
				}
			}
		}
	}

	/**
	 * findClass
	 * 
	 * @param name
	 * @return Class
	 * @throws ClassNotFoundException
	 */
	protected Class findClass(String name) throws ClassNotFoundException {
		Class result = this.loadClassFromBundles(name);

		if(result == null) {
			String message = "ScriptClassLoader_Unable_To_Find_Class: " + name;

			throw new ClassNotFoundException(message);
		}

		return result;
	}

	/**
	 * findResource
	 * 
	 * @param name
	 * @return URL
	 */
	protected URL findResource(String name) {
		URL result = super.findResource(name);

		if(result == null) {
			Iterator<Bundle> iterator = this._bundles.iterator();

			while(iterator.hasNext()) {
				Bundle bundle = iterator.next();

				result = bundle.getResource(name);

				if(result != null) {
					break;
				}
			}
		}

		return result;
	}

	/**
	 * findResources
	 * 
	 * @param name
	 * @return Enumeration
	 * @throws IOException
	 */
	protected Enumeration findResources(String name) throws IOException {
		Enumeration result = super.findResources(name);

		if(result == null) {
			Iterator<Bundle> iterator = this._bundles.iterator();

			while(iterator.hasNext()) {
				Bundle bundle = iterator.next();

				result = bundle.getResources(name);

				if(result != null) {
					break;
				}
			}
		}

		if(result == null) {
			String message = "ScriptClassLoader_Unable_To_Find_Resource: " + name;

			throw new IOException(message);
		}

		return result;
	}

	/**
	 * loadClass
	 * 
	 * @param name
	 * @return Class
	 * @throws ClassNotFoundException
	 */
	public Class loadClass(String name) throws ClassNotFoundException {
		Class result = super.loadClass(name);

		if(result == null) {
			result = this.loadClassFromBundles(name);
		}

		if(result == null) {
			String message = "ScriptClassLoader_Unable_To_Load_Class: " + name;

			throw new ClassNotFoundException(message);
		}

		return result;
	}

	/**
	 * loadClass
	 * 
	 * @param name
	 * @param resolve
	 * @return Class
	 * @throws ClassNotFoundException
	 */
	protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class result = super.loadClass(name, resolve);

		if(result == null) {
			result = this.loadClassFromBundles(name);
		}

		if(result == null) {
			String message = "ScriptClassLoader_Unable_To_Load_Class: " + name;

			throw new ClassNotFoundException(message);
		}

		return result;
	}

	/**
	 * loadClassFromBundles
	 * 
	 * @param name
	 * @return Class
	 * @throws ClassNotFoundException
	 */
	private Class loadClassFromBundles(String name) {
		Class result = null;
		Iterator<Bundle> iterator = this._bundles.iterator();

		while(iterator.hasNext()) {
			Bundle bundle = iterator.next();

			try {
				result = bundle.loadClass(name);
			} catch (ClassNotFoundException e) {
			}

			if(result != null) {
				break;
			}
		}

		return result;
	}
}
