package sailpoint.iiqda.validation;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

public class XMLNonValidatingIDocumentArtifactParser extends
    XMLIDocumentArtifactParser {

  public XMLNonValidatingIDocumentArtifactParser(IDocument doc) throws CoreException {
    super(null, doc, null);
  }

  @Override
  protected void createMarker(int severity, String message, int charStart,
      int charEnd, int line, String markerType) {
    // Do nothing
  }

  @Override
  protected void createMarker(int severity, String message, int charStart,
      int charEnd, int line, String markerType, Map<String, Object> attributes) {
    // Do nothing
  }

  
  
}
