/*
 * Menu: Examples > Modeling > UML > Show all classes
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * Description :  This script finds and prints all the UML classes contained inside the selected element, directly or indirectly.
 */
function main()
{
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
  var start = uml.getSelection("Namespace");
  output.console("Searching for all classes inside " + start.getName());
  showClasses(start);
}

function showClasses(elt)
{
	var members;
	
	if (uml.isA(elt, "Class")) {
		output.console(elt.getName());
	}
	
	if (uml.isA(elt, "Namespace")) {
		members = elt.getMembers();
		for (var i = 0; i < members.size(); i++) {
			showClasses(members.get(i));
		}
	}
}
