package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class TransitionElement extends AbstractArtifactElement implements
    IScriptContainerElement {

  private SourceElement se;

  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return "boolean";
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
