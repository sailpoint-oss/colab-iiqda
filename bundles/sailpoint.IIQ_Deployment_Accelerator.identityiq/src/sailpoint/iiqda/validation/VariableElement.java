package sailpoint.iiqda.validation;

import javax.xml.stream.XMLStreamReader;

import sailpoint.iiqda.builder.SourceElement;

public class VariableElement extends AbstractArtifactElement implements
    IArtifactElement, IScriptContainerElement {

  private String varName;
  private String varType;
  private SourceElement se;
  
  public VariableElement(XMLStreamReader stream) {
    varName=stream.getAttributeValue("", "name");
    varType=stream.getAttributeValue("", "type");
    if(varType==null || varType.equals("string")) varType="java.lang.String";
//    workflowVariables.add(new Variable(varType, varName, Variable.Source.WORKFLOW_VARIABLE));
  }

  public String getVariableName() {
    return varName;
  }
  
  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return varType;
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
