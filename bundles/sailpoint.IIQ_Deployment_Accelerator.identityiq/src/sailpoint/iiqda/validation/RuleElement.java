package sailpoint.iiqda.validation;

import java.util.List;

import sailpoint.iiqda.builder.SourceElement;
import sailpoint.iiqda.internal.Rule;
import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class RuleElement extends AbstractArtifactRootElement implements IArtifactElement, IArtifactRootElement, IScriptContainerElement, IRuleBasedScript {

  private SourceElement se;

  private String returnType;

  private String ruleType;

  private String ruleLanguage;
  
  public RuleElement(String ruleName, Rule rule, String ruleLanguage) {
    super(ruleName);
    this.returnType=rule.getReturnType();
    this.ruleType=rule.getType();
    this.ruleLanguage=ruleLanguage;
  }

  public RuleElement(String ruleName, String ruleType, String returnType, String ruleLanguage) {
    super(ruleName);
    this.ruleType=ruleType;
    this.returnType=returnType;
    this.ruleLanguage=ruleLanguage;
  }

  @Override
  public boolean needsReturn() {
    return returnType!=null && !"void".equals(returnType);
  }
  
  @Override
  public String getReturnType() {
    return returnType; 
  }
  @Override
  public String getRuleType() {
    return ruleType;
  }
  @Override
  public void addSource(SourceElement se) {
    this.se=se;
  }
  @Override
  public SourceElement getSource() {
    return se;
  }
  @Override
  public ArtifactType getType() {
    return ArtifactType.RULE;
  }
  @Override
  public List<Variable> getVariables() {
    return getRuleVariables(getRuleType());
  }
  public String getRuleLanguage() {
    return this.ruleLanguage;
  }
}
