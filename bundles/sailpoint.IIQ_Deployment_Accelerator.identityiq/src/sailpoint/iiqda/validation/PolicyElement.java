package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class PolicyElement extends AbstractArtifactRootElement implements
    IArtifactRootElement {
  
  public PolicyElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.POLICY;
  }

}
