/*
 * Menu: Examples > Modeling > UML > Find element by name
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * Description : This script searches inside a model (starting from the current selection) for
 * all the elements whose name matches a string (or regular expression) provided
 * by the user. It reports the element it found on the Topcased Scripting
 * console.
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
	var name; // The name to search for.
	var matchFunction; // The function used to match elements.
	var elements; // The elements found.
	
	var start = uml.getSelection();
	if (start == null) {
		output.error("No starting point selected.");
		return;
	}
	name = input.askSimpleString("Search string:");
	if (input.askQuestion("Type of search", "Use as a regular expression?")) {
		matchFunction = function (elt) {
			var re = new RegExp(name);
			var matches = re.exec(elt.getName());
			return matches != null && matches.length > 0;
		};
	} else {
		matchFunction = function (elt) {
			return elt.getName().equals(name);
		};
	}
	elements = find(start, matchFunction);
	reportResults(elements);
}

function find(elt, matcher) {
	var result = [];
	var children;
	var i;
	
	if (uml.isA(elt, "NamedElement") && matcher(elt)) {
		result.push(elt);
	}
	children = elt.getOwnedElements();
	for (i = 0; i < children.size(); i++) {
		result = result.concat(find(children.get(i), matcher));
	}
	return result;
}

function reportResults(elements)
{
	output.console("Search result:");
	for (var i = 0; i < elements.length; i++) {
		output.console("- " + elements[i].getName());
	}
}
