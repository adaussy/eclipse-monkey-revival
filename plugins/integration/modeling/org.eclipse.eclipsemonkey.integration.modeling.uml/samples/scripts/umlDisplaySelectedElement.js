/*
 * Menu: Examples > Modeling > UML > Display name of the selected element
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * This script displays some basic information (name and type of element) of the currently selected element.
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
	var name, type;
	var element = uml.getSelection("NamedElement");
	if (element != null) {
		name = element.getName();
		type = element.eClass().getName();
		output.info("Name of the selected element", "The current element is a " + type + " named '" + element.getName() + "'.");
	} else {
		output.error("No appropriate element selected");
	}
}
