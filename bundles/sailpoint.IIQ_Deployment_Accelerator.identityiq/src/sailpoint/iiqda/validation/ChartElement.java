package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class ChartElement extends AbstractArtifactElement implements
    IArtifactElement, IScriptContainerElement {

  private SourceElement se;
  private String title;
  
  public ChartElement(String title) {
    super();
    this.title=title;
  }
  
  public String getTitle() {
    return title;
  }
  
  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return "java.util.List";
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
