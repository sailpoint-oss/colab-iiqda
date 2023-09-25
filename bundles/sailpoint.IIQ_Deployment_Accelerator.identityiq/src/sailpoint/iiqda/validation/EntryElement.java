package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class EntryElement extends AbstractArtifactElement implements
IScriptContainerElement {

  private String key;
  private boolean needsReturn;
  private String returnType=null;
  private SourceElement se;

  public EntryElement(String key, boolean needsReturn, String returnType) {
    this.key=key;
    this.needsReturn=needsReturn;
    this.returnType=returnType;
    this.se=null;
  }

  @Override
  public boolean needsReturn() {
    return needsReturn;
  }

  @Override
  public String getReturnType() {
    return returnType;
  }

  @Override
  public SourceElement getSource() {
    return se;
  }

  @Override
  public void addSource(SourceElement se) {
    this.se=se;
  }

  public boolean hasSource() {
    return se!=null;
  }

}
