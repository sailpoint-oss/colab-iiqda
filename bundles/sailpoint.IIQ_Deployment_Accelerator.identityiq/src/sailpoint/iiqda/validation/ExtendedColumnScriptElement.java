package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class ExtendedColumnScriptElement extends AbstractArtifactElement implements
    IArtifactElement, IScriptContainerElement, IRuleBasedScript {

  private SourceElement se;

  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return "java.util.List";
  }

  @Override
  public String getRuleType() {
    return "ExtendedColumnScript";
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
