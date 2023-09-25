package sailpoint.iiqda.validation;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class IdentityDashboardElement extends AbstractArtifactRootElement {

  public IdentityDashboardElement(String name) {
    super(name);
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.IDENTITYDASHBOARD;
  }

}
