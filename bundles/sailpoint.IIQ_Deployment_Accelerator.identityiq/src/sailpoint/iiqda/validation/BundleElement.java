package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class BundleElement extends AbstractArtifactRootElement {

  public BundleElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.BUNDLE;
  }

}
