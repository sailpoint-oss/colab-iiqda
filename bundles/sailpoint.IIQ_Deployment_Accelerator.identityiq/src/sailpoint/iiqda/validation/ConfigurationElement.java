package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class ConfigurationElement extends AbstractArtifactRootElement {

  // There are certain types of Configuration object (for example, IdentitySelectionConfiguration)
  // That can contain scripts (in <IdentityFilter> elements)
  
  public ConfigurationElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.CONFIGURATION;
  }
  
  @Override
  public List<Variable> getVariables() {

    List<Variable> vars=new ArrayList<Variable>();
    return vars;
  }

}
