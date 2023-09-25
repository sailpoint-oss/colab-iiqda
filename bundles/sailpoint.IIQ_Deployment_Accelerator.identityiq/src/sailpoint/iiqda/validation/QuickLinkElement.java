package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class QuickLinkElement extends AbstractArtifactRootElement {

  public QuickLinkElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.QUICKLINK;
  }

  @Override
  public List<Variable> getVariables() {
    
    List<Variable> vars=new ArrayList<Variable>();
    
    vars.add(new Variable("sailpoint.object.Identity", "currentUser", Variable.Source.CONTEXT));
    
    return vars;
  }

}
