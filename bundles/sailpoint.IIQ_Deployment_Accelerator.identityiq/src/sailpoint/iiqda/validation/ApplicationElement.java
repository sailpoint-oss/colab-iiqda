package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class ApplicationElement extends AbstractArtifactRootElement implements IArtifactRootElement {
  
  public ApplicationElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.APPLICATION;
  }

}
