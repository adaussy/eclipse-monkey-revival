/*
 * Menu: Examples > Modeling > UML > New script
 * License: EPL 1.0
 * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling
 * DOM: http://adressToUpdateSite/org.eclipse.eclipsemonkey.integration.modeling.uml
 */

/**
 * Description :  This script create a new Topcased script properly configured for UML.
 */
function main()
{
	var location = input.selectContainer("Location of the new script", "Select the directoty where to place the new script:");
	if (location == null) {
		return;
	}
	var name = input.askSimpleString("Name of the script", "Name of the new script file (without extension):");
	if (name == null) {
		return;
	}
	if (!output.createNewFile(location, name + ".js", initialContents(name))) {
		output.error("An error occured during the creation of the new script.");
	}
}

function initialContents(name)
{
	var contents = "";
	contents = appendLine(contents, "/*");
	contents = appendLine(contents, " * Menu: Topcased UML > " + name);
	contents = appendLine(contents, " * License: EPL 1.0");
	contents = appendLine(contents, " * DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.javascript");
	contents = appendLine(contents, " * DOM: http://topcased-mm.gforge.enseeiht.fr/release/update-site3.4/org.eclipse.eclipsemonkey.integration.modeling.uml");
	contents = appendLine(contents, " * DOM: http://topcased-mm.gforge.enseeiht.fr/release/update-site3.4/org.eclipse.eclipsemonkey.integration.modeling.uml.uml");
	contents = appendLine(contents, " */");
	contents = appendLine(contents, "");
	contents = appendLine(contents, "/**");
	contents = appendLine(contents, " * New UML Script " + name);
	contents = appendLine(contents, " */");
	contents = appendLine(contents, "function main() {");
	contents = appendLine(contents, "	var op = new java.lang.Runnable({");
	contents = appendLine(contents, "		run: function () {");
	contents = appendLine(contents, "			run();");
	contents = appendLine(contents, "		}");
	contents = appendLine(contents, "	});");
	contents = appendLine(contents, "	uml.runOperation(op, \"" + name + "\");");
	contents = appendLine(contents, "}");
	contents = appendLine(contents, "");
	contents = appendLine(contents, "/**");
	contents = appendLine(contents, " * Run the script itself");
	contents = appendLine(contents, " */");
	contents = appendLine(contents, "function run() {");
	contents = appendLine(contents, "	// Put the contents of your script here.");
	contents = appendLine(contents, "}");
	return contents;
}

function appendLine(contents, newLine)
{
	return contents + newLine + "\n";
}