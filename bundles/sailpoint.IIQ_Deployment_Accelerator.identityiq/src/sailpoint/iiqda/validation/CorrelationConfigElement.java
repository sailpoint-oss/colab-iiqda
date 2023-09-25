package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class CorrelationConfigElement extends AbstractArtifactRootElement {

  public CorrelationConfigElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.CORRELATIONCONFIG;
  }

}
