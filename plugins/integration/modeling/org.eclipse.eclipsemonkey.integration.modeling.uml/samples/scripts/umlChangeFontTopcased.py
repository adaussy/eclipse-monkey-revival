# Menu: Examples > Modeling > UML > Change Class font to Tahoma (Topcased)
# License: EPL 1.0
# Key: M3+U
#

from org.topcased.modeler.di.model import GraphNode
from org.topcased.modeler.di.model import EMFSemanticModelBridge
from org.topcased.modeler.di.model.util import DIUtils
from org.eclipse.eclipsemonkey.integration.modeling.uml.di import DiDOM
from org.eclipse.swt.graphics import FontData
from org.topcased.modeler import ModelerPropertyConstants
from org.eclipse.jface.resource import StringConverter
from org.eclipse.swt.SWT import NORMAL

from org.eclipse.uml2.uml import Class


def main():
    di = DiDOM()
    print "[INFO] Update Fonts"
    selection = di.getSelection("GraphNode")
    if selection == None:
        print "[ERROR] Please select a graphical node."
        return
    DIresource = selection.eResource()
    root = DIresource.getContents().get(0)
    
    font = FontData("Tahoma", 8, NORMAL) 
    
    for elt in root.eAllContents():
        if isinstance(elt, GraphNode):
            modelElt = elt.getSemanticModel()

            if isinstance(modelElt, EMFSemanticModelBridge):
                object = modelElt.getElement()
            
                if isinstance(object, Class):
                    print "[INFO] " + object.eContainer().getName() + "::" + object.getName() + " has been updated."
                    DIUtils.setProperty(elt, ModelerPropertyConstants.FONT, StringConverter.asString(font))
    
    DIresource.save(None)
    print "[INFO] file " + DIresource.getURI().toString() + " has been saved."

main()    
