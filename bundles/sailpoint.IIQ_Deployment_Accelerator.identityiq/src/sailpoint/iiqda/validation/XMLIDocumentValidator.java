package sailpoint.iiqda.validation;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import sailpoint.iiqda.core.CoreUtils;

@SuppressWarnings("restriction")
public class XMLIDocumentValidator extends BaseXMLValidator implements IXMLValidator {

  private IValidator validator;
  private IDocument doc;
  private IReporter reporter;
  private boolean hasErrors=false;


  public XMLIDocumentValidator(IValidator iValidator, IDocument doc, IReporter reporter) {
    this.validator=iValidator;
    this.doc=doc;
    this.reporter=reporter;
  }

  public boolean hasErrors() {
    return hasErrors;
  }

  public void validate() throws CoreException {

    byte[] contents;
    try {
      contents = doc.get(0, doc.getLength()).getBytes();
    } catch (BadLocationException e) {
      throw new CoreException(CoreUtils.toErrorStatus("BadLocationException getting doc contents: "+e));
    }

    List<BSIssue> issues=validateStream(new ByteArrayInputStream(contents));

    if(issues.size()>0) {
      for (BSIssue issue: issues) {

        int sev=-1;
        switch(issue.getSeverity()) {
          case ERR:
            sev=IMessage.HIGH_SEVERITY;
            break;
          case WARN:
            sev=IMessage.NORMAL_SEVERITY;
            break;
          default:
            sev=IMessage.LOW_SEVERITY;            
        }
        IMessage m = new LocalizedMessage(sev, issue.getMessage());
        m.setOffset(issue.getStart());
        m.setLength(issue.getLength());
        m.setLineNo(issue.getLine());
        reporter.addMessage(validator, m);
      }
      //        for (String key: issue.getAttributes().keySet()) {
      //          Object value=issue.getAttributes().get(key);
      //          marker.setAttribute(key, value);
      //        }
    }

  }
}