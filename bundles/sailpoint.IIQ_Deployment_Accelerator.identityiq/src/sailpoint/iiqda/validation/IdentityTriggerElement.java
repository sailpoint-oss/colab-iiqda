package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class IdentityTriggerElement extends AbstractArtifactRootElement {

  public IdentityTriggerElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.IDENTITYTRIGGER;
  }

}
