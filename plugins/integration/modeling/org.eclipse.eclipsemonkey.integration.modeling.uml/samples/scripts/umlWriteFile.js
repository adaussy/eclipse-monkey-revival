/*
 * Menu: Examples > Modeling > UML > Write element name to file
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * This script illustrates how to write the result of a script into a text file.
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
  var elt = uml.getSelection("NamedElement");
  if (elt == null) {
	  output.error("The selection must be a NamedElement");
	  return;
  }
  var contents = "The selection is a " + elt.eClass().getName() + " named " + elt.getName() + ".\n";
  contents += "It comes from the model in resource " + elt.eResource().getURI().toString() + ".\n";
  contents += "The current time is " + new Date() + ".\n";
  var location = input.selectContainer("Location of the result", "Select the directoty where to place the result:");
  if (output.createNewFile(location, "result.txt", contents)) {
    output.info("See file " + location[0].toString() + "/result.txt for the result of this script.");
  } else {
	  output.error("The result could new be written to the disk.");
  }
}
