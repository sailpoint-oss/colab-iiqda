package sailpoint.iiqda.validation;

import sailpoint.iiqda.builder.SourceElement;

public class StepElement extends AbstractArtifactElement implements
    IScriptContainerElement, IRuleBasedScript {

  private boolean needsReturn=false;
  private String resultVariable;
  private SourceElement se;  
  
  public StepElement(String resultVariable) {
    this.resultVariable=resultVariable;    // Here's another step start. Record whether there's a resultVariable or not (this means
    if(resultVariable!=null) {
      needsReturn=true;
    } else {
      needsReturn=false;
    }

  }

  @Override
  public boolean needsReturn() {
    return needsReturn;
  }

  @Override
  public String getReturnType() {
    // Figure out what the "resultVariable" element is 
    // then get its type
    if(needsReturn) {
      WorkflowElement we=(WorkflowElement)parent;
      for(Variable v: we.workflowVariables()) {
        if(v.getName().equals(resultVariable)) {
          return v.getType();
        }
      }
      // if the step is not returning into a variable defined with a <Variable> element
      // at the start of the workflow, then make it just Object
      // (for example, 'payload' in our Importer workflow)
      return "java.lang.Object";
    }
    return null;
  }
  
  @Override
  public String getRuleType() {
    return "Workflow";
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
