package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class ObjectConfigElement extends AbstractArtifactRootElement {

  
  
  public ObjectConfigElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.OBJECTCONFIG;
  }

}
