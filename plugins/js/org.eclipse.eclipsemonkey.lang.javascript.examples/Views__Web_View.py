#
# Menu: Examples > Views > Py > Google Web View
# Kudos: Paul Colton
# License: EPL 1.0
#

from org.eclipse.swt.browser import LocationListener
from org.eclipse.eclipsemonkey.ui.scriptableView import GenericScriptableView
from org.eclipse.ui import IWorkbenchPage

class MyLocationListener(LocationListener):
	
	def changing(self, event):
		location = event.location
	
		# Print out the location to the console
		print "You clicked on: " + location
	
	def changed(self, event):
		pass
		
page = window.getActivePage()

view = page.showView(
	"org.eclipse.eclipsemonkey.ui.scriptableView.GenericScriptableView", 
	"GoogleWebView", 
	IWorkbenchPage.VIEW_VISIBLE)
view.setViewTitle("Google")

browser = view.getBrowser()
browser.setUrl("http://www.google.com")
browser.addLocationListener(MyLocationListener());
