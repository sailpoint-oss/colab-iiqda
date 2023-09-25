package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public interface IScriptContainerElement {

  boolean needsReturn();

  public String getReturnType();

  public void addSource(SourceElement se);
  public SourceElement getSource();

}
