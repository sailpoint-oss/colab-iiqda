package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class ValueScriptElement extends AbstractArtifactElement implements
    IArtifactElement, IScriptContainerElement, IRuleBasedScript {

  private SourceElement se;
  
  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return "java.lang.String";
  }

  @Override
  public String getRuleType() {
    return "ValueScript";
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
