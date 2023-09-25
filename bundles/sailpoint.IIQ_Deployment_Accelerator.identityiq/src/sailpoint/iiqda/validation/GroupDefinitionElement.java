package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class GroupDefinitionElement extends AbstractArtifactRootElement {

  public GroupDefinitionElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.GROUPDEFINITION;
  }

}
