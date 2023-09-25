package sailpoint.iiqda.validation;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import sailpoint.iiqda.IIQPlugin;

public class XMLIFileArtifactParser extends BaseXMLArtifactParser {

  private static final boolean DEBUG_VALIDATION = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Validation"));

  private IFile resource;

	public XMLIFileArtifactParser(IFile file) throws CoreException {
	  this(file, null, null);
	}
	
	public XMLIFileArtifactParser(IFile file, Map<String,List<String>> allTheArtifacts, Map<String, String> allTheRules) throws CoreException {
		super();
		this.resource=file;
		this.allTheArtifacts=allTheArtifacts;
		this.allTheRules=allTheRules;
		
		InputStream inStream=file.getContents();
		
		IDocumentProvider provider = new TextFileDocumentProvider();
		provider.connect(file);
		IDocument document = provider.getDocument(file);
		boolean lineEnder=( "\r\n".equals(TextUtilities.getDefaultLineDelimiter(document)) );
		
		init(inStream, lineEnder);
	}

	public IFile getResource() {
		return resource;
	}

	private IMarker internalCreateMarker(int severity,
			String message, int charStart, int charEnd, int line, String markerType) throws CoreException{
		//if (DEBUG_VALIDATION) CoreActivator.logDebug("IIQArtifactBuilder.addmarker: "+markerType+" : "+message);
		// Create a marker
		IMarker marker = resource.createMarker(markerType);

		//Once we have a marker object, we can set its attributes
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.CHAR_START, charStart );
		marker.setAttribute(IMarker.CHAR_END, charEnd );
		marker.setAttribute(IMarker.LINE_NUMBER, line);
		return marker;
	}
	
	@Override
	protected void createMarker(int severity,
			String message, int charStart, int charEnd, int line, String markerType) {
		try {
			internalCreateMarker(severity, message, charStart, charEnd, line, markerType);
		} catch (CoreException e) {
		  if (DEBUG_VALIDATION) IIQPlugin.logDebug("XMLIFileArtifactParser Can't create marker due to "+e);
		}
	}

	@Override
	protected void createMarker(int severity,
			String message, int charStart, int charEnd, int line, String markerType, Map<String,Object> attributes) {
		try {
			IMarker marker=internalCreateMarker(severity, message, charStart, charEnd, line, markerType);
			for(String key: attributes.keySet()) {
				marker.setAttribute(key, attributes.get(key));
			}
		} catch (CoreException e) {
		  if (DEBUG_VALIDATION) IIQPlugin.logDebug("XMLIFileArtifactParser Can't create marker due to "+e);
		}
	}

  @Override
  protected IProject getProject() {
    return resource.getProject();
  }
  
}
