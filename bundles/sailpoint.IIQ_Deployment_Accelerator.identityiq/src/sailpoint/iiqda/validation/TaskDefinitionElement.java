package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class TaskDefinitionElement extends AbstractArtifactRootElement {

  // There are certain elements in a Task Definition that have different sets of variables passed in, for example,
  // QueryScript, DataSourceScript or RenderScript
  // These won't call addVariables on the parent, so if we got to the TaskDefinition element, it's a generic
  // script that has the following variables
  
  public TaskDefinitionElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.TASKDEFINITION;
  }
  
  @Override
  public List<Variable> getVariables() {

    List<Variable> vars=new ArrayList<Variable>();
    
    vars.add(new Variable("java.lang.Object", "value", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.Attributes", "arguments", Variable.Source.CONTEXT));        

    return vars;
  }

}
