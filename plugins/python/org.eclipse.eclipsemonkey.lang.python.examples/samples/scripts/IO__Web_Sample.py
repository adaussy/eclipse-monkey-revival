#
# Menu: Examples >IO > Py > Web Sample
# Kudos: Paul Colton, Aptana, Inc.
# License: EPL 1.0
#

import urllib

import org.eclipse.jface.dialogs

req = urllib.urlopen("http://xml.weather.yahoo.com/forecastrss?p=94103")
text = req.read()

org.eclipse.jface.dialogs.MessageDialog.openInformation(
	window.getShell(), 	
	"San Francisco Weather Feed", 
	text
)
