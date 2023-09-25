package sailpoint.iiqda.resolutions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

import sailpoint.iiqda.IIQPlugin;

public abstract class ResourceModifyingMarkerResolution extends WorkbenchMarkerResolution {

  private static final boolean DEBUG_RESOLUTIONS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Resolutions"));
 
  protected enum ModificationType {
    INSERT,
    INSERTBEFORE,
    DELETE;
  }

  protected void modifyResource(IFile res, ModificationType insert, int pos,
      String token) {
    modifyResource(res, insert, pos, token, null);
  }

  protected void modifyResource(IFile res, ModificationType insert, int pos,
      String token, String otherToken) {

    try {
      ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().
          getActiveWorkbenchWindow().getActivePage(), res, true);
      IDocument doc = editor.getDocumentProvider().
          getDocument(new FileEditorInput(res));

      if(insert==ModificationType.INSERT){ 
        doc.replace(pos, 0, token);
      }
      if(insert==ModificationType.DELETE) {
        // we look for the thing for cases where there may be whitespace between pos and the thing
        FindReplaceDocumentAdapter fnr=new FindReplaceDocumentAdapter(doc);
        fnr.find(pos, token, true, true, false, false);
        fnr.replace("", false);
      }


      if(insert==ModificationType.INSERTBEFORE) {
        FindReplaceDocumentAdapter fnr=new FindReplaceDocumentAdapter(doc);
        if (DEBUG_RESOLUTIONS) {
          IIQPlugin.logDebug("Looking for "+otherToken+" at "+pos);
        }
        IRegion reg=fnr.find(pos, otherToken, true, true, false, false);
        fnr.replace(token+otherToken, false);
      }
    } catch (PartInitException | BadLocationException e) {
      if (DEBUG_RESOLUTIONS) {
        IIQPlugin.logDebug("Unable to modify Resource "+res.getName());
      }
      e.printStackTrace();
    }

  }

}
