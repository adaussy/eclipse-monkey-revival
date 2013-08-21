/*
 * Menu: Examples > Modeling > UML > Add class with N attributes
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * Description : This script creates a new UML class (in the currently selected package) which
 * initially contains a user-specified number of attributes.
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
	var name, nbAttributes, prefix, newClass, attr;
	
	var parent = uml.getSelection("Package");
	if (parent == null) {
		output.error("The parent package of the new class must be selected.");
		return;
	}
	name = input.ask("Name of the new class:");
	if (name != null) {
		nbAttributes = input.ask("Number of attributes to create:");
		prefix = input.ask("Prefix for the attributes names:");
		newClass = uml.createClass(parent, name);
		for (i = 0; i < nbAttributes; i++) {
			attr = uml.getFactory().createProperty();
			attr.setName(prefix + i);
			newClass.getOwnedAttributes().add(attr);
		}
	}
}
