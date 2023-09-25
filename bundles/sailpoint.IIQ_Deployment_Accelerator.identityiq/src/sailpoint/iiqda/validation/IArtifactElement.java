package sailpoint.iiqda.validation;

import java.util.List;

import sailpoint.iiqda.builder.SourceElement;

public interface IArtifactElement {

  public void setParent(IArtifactElement peek);
  
  public IArtifactElement getParent();

  public void addChild(IArtifactElement childEl);

  public List<IArtifactElement> getChildren();
  
  public List<SourceElement> getSourceElements();

  public List<Variable> getVariables();

}
