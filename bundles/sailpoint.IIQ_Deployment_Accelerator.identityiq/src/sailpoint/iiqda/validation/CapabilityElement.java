package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class CapabilityElement extends AbstractArtifactRootElement {

  public CapabilityElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.CAPABILITY;
  }

}
