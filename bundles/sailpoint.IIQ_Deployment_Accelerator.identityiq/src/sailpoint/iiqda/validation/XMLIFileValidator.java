package sailpoint.iiqda.validation;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class XMLIFileValidator extends BaseXMLValidator implements IXMLValidator {

  private IFile file;
  private boolean hasErrors=false;
  

  public XMLIFileValidator(IFile file) {
    this.file=file;
  }

  public boolean hasErrors() {
    return hasErrors;
  }
  
  public void validate() throws CoreException {

    List<BSIssue> issues=validateStream(file.getContents());
    
    if(issues.size()>0) {
      for (BSIssue issue: issues) {
        IMarker marker = file.createMarker(issue.getType());
        int sev=0;
        switch(issue.getSeverity()) {
          case WARN:
            sev=IMarker.SEVERITY_WARNING;
            break;
          case ERR:
            sev=IMarker.SEVERITY_ERROR;
            break;
          default:
        }
        marker.setAttribute(IMarker.SEVERITY, sev);
        marker.setAttribute(IMarker.MESSAGE, issue.getMessage());
        marker.setAttribute(IMarker.CHAR_START, issue.getStart());
        marker.setAttribute(IMarker.CHAR_END, issue.getStart()+issue.getLength());
        marker.setAttribute(IMarker.LINE_NUMBER, issue.getLine());
        
        // add the extra attributes (if any) {
        for (String key: issue.getAttributes().keySet()) {
          Object value=issue.getAttributes().get(key);
          marker.setAttribute(key, value);
        }
      }
    }
  }
}
