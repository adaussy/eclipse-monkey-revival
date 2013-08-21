/*
 * Menu: Examples > Modeling > UML > Set element name from file
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * This script shows how to read the contents of a text file. It lets the user
 * select a file from the workspace, reads the contents, and use it to rename
 * the currently selected model element.
 */
function main() {
	var op = new java.lang.Runnable({
		run: function () {
			run();
		}
	});
	uml.runOperation(op, "Rename element");
}

/**
 * Run the script itself
 */
function run() {
	var elt = uml.getSelection("NamedElement");
	if (elt == null) {
		output.error("The selection must be a NamedElement.");
		return;
	}
	var file = input.selectResource();
	if (file != null) {
		var contents = input.readFile(file);
		elt.setName(contents);
	}
}
