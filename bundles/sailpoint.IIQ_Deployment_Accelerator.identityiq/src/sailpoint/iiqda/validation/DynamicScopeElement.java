package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class DynamicScopeElement extends AbstractArtifactRootElement {

  public DynamicScopeElement(String name) {
    super(name);
  }

  
  
  @Override
  public ArtifactType getType() {
    return ArtifactType.DYNAMICSCOPE;
  }



  @Override
  public List<Variable> getVariables() {
    List<Variable> vars=new ArrayList<Variable>();
    vars.add(new Variable("sailpoint.object.Identity", "identity", Variable.Source.CONTEXT));
    return vars;
  }
  
}
