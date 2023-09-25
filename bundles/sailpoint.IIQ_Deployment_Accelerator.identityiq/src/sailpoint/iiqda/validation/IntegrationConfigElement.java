package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class IntegrationConfigElement extends AbstractArtifactRootElement {

  public IntegrationConfigElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.INTEGRATIONCONFIG;
  }

}
