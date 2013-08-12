#
# Menu: Py > Editors > Replace Selecton with Date
# Kudos: José Fonseca & Paul Colton
# License: EPL 1.0
#

import time

editor = window.getActivePage().getActiveEditor()
document = editor.getDocumentProvider().getDocument(editor.getEditorInput())

# get range of lines in the selection (or at the cursor position)
selection = editor.getSelectionProvider().getSelection()
offset = selection.getOffset()
deleteLength = selection.getLength()

text = time.strftime("%Y-%m-%d %H:%M:%S")

# apply edit and reveal in editor
source = document.get()
document.replace(offset, deleteLength, text)
editor.selectAndReveal(offset, len(text))
