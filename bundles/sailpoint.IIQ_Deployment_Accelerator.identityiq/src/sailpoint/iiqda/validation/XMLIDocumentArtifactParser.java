package sailpoint.iiqda.validation;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;

@SuppressWarnings("restriction")
public class XMLIDocumentArtifactParser extends BaseXMLArtifactParser {

  private static final boolean DEBUG_VALIDATION = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Validation"));
  
	private IDocument resource;
	private IValidator validator;
	private IReporter reporter;
	
	private IProject project;
	
	public XMLIDocumentArtifactParser(IValidator iValidator, IDocument doc, IReporter reporter) throws CoreException {
    this.validator=iValidator;
    this.reporter=reporter;
	  this.resource=doc;

	  boolean isCRLF="\r\n".equals(TextUtilities.getDefaultLineDelimiter(doc));
	  
	  // Try and get the Project
	  if (DEBUG_VALIDATION) IIQPlugin.logDebug("XMLIDocArtifactParser.getReferencedRule:");
    ITextFileBufferManager bufferMgr = FileBuffers.getTextFileBufferManager();
    ITextFileBuffer buffer=bufferMgr.getTextFileBuffer(resource);
    if(buffer==null) {
      if (DEBUG_VALIDATION) IIQPlugin.logDebug("Couldn't find Buffer");
    } else {
      // Find the path's container
      // Not sure why we wanted this..
//      IPath path=buffer.getLocation();
//      IPath directory=path.removeLastSegments(path.segmentCount()-1); // get first segment i.e. Project
//      IWorkspaceRoot workRoot= ResourcesPlugin.getWorkspace().getRoot();
//      IProject container=(IProject)workRoot.getContainerForLocation(directory);
    }
	  
	  String str;
    try {
	    str = doc.get(0, doc.getLength());
    } catch (BadLocationException e) {
	    throw new CoreException(CoreUtils.toErrorStatus("BadLocationException getting Document contents "+e));
    }
	  ByteArrayInputStream bais=new ByteArrayInputStream(str.getBytes());
	  init(bais, isCRLF);
  }
	
	public IDocument getResource() {
	  if (DEBUG_VALIDATION) IIQPlugin.logDebug("XMLIDocumentArtifactParser.getResource: ");
    return resource;
  }
	
	private IMessage internalCreateMarker(int severity, String message, int charStart,
      int charEnd, int line, String markerType) {
		int sev=-1;
    switch(severity) {
      case IMarker.SEVERITY_ERROR:
        sev=IMessage.HIGH_SEVERITY;
        break;
      case IMarker.SEVERITY_WARNING:
        sev=IMessage.NORMAL_SEVERITY;
        break;
      default:
        sev=IMessage.LOW_SEVERITY;            
    }
    IMessage m = new LocalizedMessage(sev, message);
    m.setOffset(charStart);
    m.setLength(charEnd-charStart);
    m.setLineNo(line);
    m.setMarkerId(markerType);
    return m;
  }
  //        for (String key: issue.getAttributes().keySet()) {
  //          Object value=issue.getAttributes().get(key);
  //          marker.setAttribute(key, value);
  //        }

	@Override
  protected void createMarker(int severity, String message, int charStart,
      int charEnd, int line, String markerType) {
		IMessage m=internalCreateMarker(severity, message, charStart, charEnd, line, markerType);
		reporter.addMessage(validator, m);	  
  }

	@Override
  protected void createMarker(int severity, String message, int charStart,
      int charEnd, int line, String markerType, Map<String, Object> attributes) {
		IMessage m=internalCreateMarker(severity, message, charStart, charEnd, line, markerType);
		reporter.addMessage(validator, m);
  }

	protected IProject getProject() {
	  return project;
	}
	
}
