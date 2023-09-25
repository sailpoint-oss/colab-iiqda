package sailpoint.iiqda.propertytest;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.editors.ArtifactEditor;

@SuppressWarnings("restriction")
public class ElementPropertyTester extends PropertyTester {

  private static final boolean DEBUG_PROPERTY_TESTER = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/PropertyTesters"));

  @Override
  public boolean test(Object receiver, String property, Object[] args,
      Object expectedValue) {
    if (DEBUG_PROPERTY_TESTER) {
      IIQPlugin.logDebug("ElementPropertyTester.test:");
    }
    if (receiver instanceof ArtifactEditor) {
      ArtifactEditor iSel=(ArtifactEditor) receiver;
      // so, it's a document. This is good.
      // Now we need to parse it to find the XML element the pointer is in
      ITextEditor editor = (ITextEditor) iSel
          .getAdapter(ITextEditor.class);
      IStructuredSelection strucSelection = (IStructuredSelection) iSel.getSite().getSelectionProvider().getSelection();
      ITextSelection textSelection = (ITextSelection) iSel.getSite().getSelectionProvider().getSelection();
      
      int x=0;
      Object obj=strucSelection.getFirstElement();
      // This is in case we clicked on one of the attributes in the element declaration
      if(obj!=null && obj instanceof AttrImpl) {
        obj=((AttrImpl)obj).getOwnerElement();
      }
      if(obj!=null && obj instanceof IDOMElement) {
        IDOMElement iEl=(IDOMElement)obj;
        int selOffset = textSelection.getOffset();
        if (DEBUG_PROPERTY_TESTER) {
          IIQPlugin.logDebug("TextSelection is "+selOffset+" (len="+textSelection.getLength()+")");
          IIQPlugin.logDebug("Selection is <"+iEl.getNodeName()+">, looking for "+expectedValue);
          IIQPlugin.logDebug("startoffset="+iEl.getStartOffset()+" , startendoffset="+iEl.getStartEndOffset());
        }
        if(iEl.getNodeName().equals(expectedValue) && (iEl.getStartOffset()<=selOffset && iEl.getStartEndOffset()>=selOffset )) return true;
      }
      System.out.println("x");
    } else {
      if (DEBUG_PROPERTY_TESTER) {
        IIQPlugin.logDebug("Not a structured Selection");
      }
    }
    return false;
  }

}
