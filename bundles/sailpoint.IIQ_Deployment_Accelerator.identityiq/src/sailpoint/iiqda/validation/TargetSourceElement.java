package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class TargetSourceElement extends AbstractArtifactRootElement {

  public TargetSourceElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.TARGETSOURCE;
  }

}
