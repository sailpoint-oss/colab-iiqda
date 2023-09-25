package sailpoint.iiqda.validation;

import org.eclipse.core.resources.IProject;

public class RuleNotFoundException extends Exception {
  
  private static final long serialVersionUID = -7692063727381632700L;
  
  private String ruleName;
  private IProject project;

  public RuleNotFoundException(String ruleName, IProject project) {
    this.ruleName=ruleName;
    this.project=project;
  }

  public String getRuleName() {
    return ruleName;
  }

  public IProject getProject() {
    return project;
  }
  
  

}
