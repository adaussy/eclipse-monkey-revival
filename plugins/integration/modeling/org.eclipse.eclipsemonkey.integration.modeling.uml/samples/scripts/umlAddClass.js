/*
 * Menu: Examples > Modeling > UML > Add class
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * Description : This script create a new UML class in the current package. A package must be
 * selected in the current UML editor. It illustrates how to create new
 * elements. In this case we use uml.createClass(), which is a pre-defined
 * helper on the uml object. It creates a new class, sets its name, and adds it
 * into the specified package. The uml object provides some other helpers, but
 * it is also possible to to use uml.getFactory() to create any UML element.
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
	var name;
	var parentPackage = uml.getSelection("Package");
	if (parentPackage == null) {
		output.error("A Package must be selected in the current UML editor to run this script.");
		return;
	}
	name = input.askSimpleString("New class", "Name of the new class:", "Class");
	if (name != null) {
		uml.createClass(parentPackage, name);
		// An equivalent way using the generic UML factory would be:
		// var klass = uml.getFactory().createClass();
		// klass.setName(name);
		// parentPackage.getPackagedElements().add(klass);
	}
}
