# Menu: Examples > Modeling > UML > Change Class font to Tahoma (Papyrus)
# License: EPL 1.0
# Key: M3+I
#

from java.lang import Runnable
from org.eclipse.gmf.runtime.notation import Node
from org.eclipse.gmf.runtime.diagram.core.util import ViewUtil
from org.eclipse.gmf.runtime.notation import NotationPackage
from org.eclipse.eclipsemonkey.integration.modeling.uml.notation import NotationDOM
from org.eclipse.swt.graphics import FontData
from org.eclipse.jface.resource import StringConverter
from org.eclipse.swt.SWT import NORMAL

from org.eclipse.uml2.uml import Class


def main():
    notation = NotationDOM()
    selection = notation.getSelection("Node")
    if selection == None:
        print "[ERROR] Please select a graphical node."
        return
    GMFresource = selection.eResource()
            
    class MyRunnable(Runnable) :
        def run(self):
            print "[INFO] Update Fonts"
            font = FontData("Tahoma", 8, NORMAL) 
            
            for elt in GMFresource.getAllContents():
                if isinstance(elt, Node):
                    object = elt.getElement()
                    
                    if isinstance(object, Class):
                        print "[INFO] " + object.eContainer().getName() + "::" + object.getName() + " has been updated."
                        
                        ViewUtil.setStructuralFeatureValue(elt, NotationPackage.eINSTANCE.getFontStyle_FontName(), StringConverter.asString(font))
    
    op = MyRunnable()
    notation.runOperation(op, "Change Class font to Tahoma")
    notation.save()
    print "[INFO] file " + GMFresource.getURI().toString() + " has been saved."

main()
