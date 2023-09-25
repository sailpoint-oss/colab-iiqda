package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class ValidationScriptElement extends AbstractArtifactElement implements
    IArtifactElement, IScriptContainerElement, IRuleBasedScript {

  private SourceElement se;

  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return "java.lang.Object";
  }

  @Override
  public String getRuleType() {
    return "Validation";
  }
  
  @Override
  public void addSource(SourceElement se) {
    this.se=se;
  }
  

  @Override
  public SourceElement getSource() {
    return se;
  }

}
