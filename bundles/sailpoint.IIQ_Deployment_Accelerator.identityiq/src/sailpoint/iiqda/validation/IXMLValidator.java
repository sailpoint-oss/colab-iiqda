package sailpoint.iiqda.validation;

import org.eclipse.core.runtime.CoreException;

public interface IXMLValidator {
  
  public boolean hasErrors();
  public void validate() throws CoreException;

}
