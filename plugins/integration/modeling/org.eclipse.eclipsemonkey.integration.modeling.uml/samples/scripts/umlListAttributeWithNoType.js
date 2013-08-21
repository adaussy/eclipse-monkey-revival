/*
 * Menu: Examples > Modeling > UML > List attribute with no type
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * This script lists the attribute anywhere in the current model which do not
 * have a specified type. It illustrates how to obtain the top-level Model from
 * the currently selected element, how to navigate inside a whole model
 * (recursively) and detect elements which match some criterion, and how to
 * report results in the console.
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
	var attributes;
	var current = uml.getSelection();
	if (current == null) {
		output.error("No element selected. Can not determine the current model.");
		return;
	}

	// Go to the top of the model
	while (!uml.isA(current, "Model")) {
		current = current.getOwner();
	}

	attributes = findAttributes(current);
	reportResult(attributes);
}

function findAttributes(elt) {
	var result = [];
	var children;

	if (uml.isA(elt, "Property") && elt.getType() == null) {
		result.push(elt);
	}
	children = elt.getOwnedElements();
	for ( var i = 0; i < children.size(); i++) {
		result = result.concat(findAttributes(children.get(i)));
	}
	return result;
}

function reportResult(attributes) {
	var attr;
	output.console("The following attributes do not have a specified type:");
	for ( var i = 0; i < attributes.length; i++) {
		attr = attributes[i];
		output.console(" - " + attr.getClass_().getName() + "#"
				+ attr.getName());
	}
}
