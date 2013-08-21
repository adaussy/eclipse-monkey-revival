package org.eclipse.eclipsemonkey.lang.javascript;

/**
 * Store all pattern to find metadata
 * 
 * @author adaussy
 * 
 */
public interface IMetadaPattern {

	public static final String DOM_METADATA_PATTERN = "DOM:\\s*(\\p{Graph}+)\\/((\\p{Alnum}|\\.)+)";

	public static final String SCOPE_SCOPE_METADA_PATTERN = "Scope:\\s*((\\p{Graph}| )+)";

	public static final String KEY_METADA_PATTERN = "Key:\\s*((\\p{Graph}| )+)";

	public static final String ON_LOAD_METADATA_PATTERN = "OnLoad:\\s*((\\p{Graph}| )+)";

	public static final String MENU_METADA_PATTERN = "Menu:\\s*((\\p{Graph}| )+)";

	public static final String LISTENER_METADA_PATTERN = "Listener:\\s*((\\p{Graph}| )+)";

	public static final String DESCRIPTION_METADA_PATTERN = "Description:\\s*\\{(.+)\\}";
}
