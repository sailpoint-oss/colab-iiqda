package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class ValidatorScriptElement extends AbstractArtifactElement implements
    IArtifactElement, IScriptContainerElement {

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
  public void addSource(SourceElement se) {
    this.se=se;
  }
  

  @Override
  public SourceElement getSource() {
    return se;
  }

}
