package sailpoint.iiqda.validation;


public class UninterestingElement extends AbstractArtifactElement {

  private String elType;

  public UninterestingElement(String elName) {
    this.elType=elName;
  }

  public String getUninterestingType() {
    return elType;
  }
}
