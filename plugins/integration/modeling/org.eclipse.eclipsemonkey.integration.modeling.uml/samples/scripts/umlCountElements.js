/*
 * Menu: Examples > Modeling > UML > Count elements
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * This script shows how to navigate inside a model. It asks the user for a type
 * of element to look for (defaulting to "Class"), and starting from the current
 * selected element, counts all the elements of this type contained inside the
 * current selected element (directly or not). The result is shown to the user
 * in a dialog box.
 * 
 * This script also shows how it is possible to create auxiliary JavaScript
 * functions (here countElement()) which can be called from the script body (the
 * main() function).
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
	var start;
	var kind;
	var count;

	start = uml.getSelection();
	if (start == null) {
		output.error("An element must be selected.");
		return;
	}
	
	kind = input.askSimpleString("Element kind", "What kind of elements do you want to count?", "Class");
	if (kind != null) {
		count = countElements(start, kind);
		output.info("There are " + count + " elements of kind " + kind.toString());
	}
}

function countElements(elt, kind) {
	var count = 0;
	var children;
	var i;

	if (uml.isA(elt, kind)) {
		count = 1;
	}
	children = elt.getOwnedElements();
	for (i = 0; i < children.size(); i++) {
		count += countElements(children.get(i), kind);
	}
	return count;
}