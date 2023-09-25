package sailpoint.iiqda.validation;

import javax.xml.stream.XMLStreamReader;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.internal.Rule;
import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class ArtifactElementFactory {

  public IArtifactElement getElement(XMLStreamReader stream) {
    
    String elementName = stream.getLocalName();

    ArtifactType type=ArtifactType.fromString(elementName);
    
    switch(type) {
      case AFTERSCRIPT:
        return new AfterScriptElement();
      case ALLOWEDVALUESDEFINITION:
        return new AllowedValuesDefinitionElement();
      case APPLICATION:
        return getApplicationElement(stream);
      case APPROVAL:
        return getApprovalElement(stream);
      case ARG:
        return new ArgElement();
      case BUNDLE:
        return getBundleElement(stream);
      case CAPABILITY:
        return getCapabilityElement(stream);
      case CHART:
        return getChartElement(stream);
      case CONDITIONSCRIPT:
        return new ConditionScriptElement();
      case CONFIGURATION:
        return getConfigurationElement(stream);
      case CORRELATIONCONFIG:
        return getCorrelationConfigElement(stream);
      case DATASOURCESCRIPT:
        return new DataSourceScriptElement();
      case DYNAMICSCOPE:
        return getDynamicScopeElement(stream);
      case ENTRY:
        return getEntryElement(stream);
      case EXTENDEDCOLUMNSCRIPT:
        return getExtendedColumnScriptElement(stream);
      case FIELD:
        return new FieldElement(stream);
      case FILTERSCRIPT:
        return new FilterScriptElement();
      case FORM:
        return getFormElement(stream);
      case GROUPDEFINITION:
        return getGroupDefinitionElement(stream);
      case IDENTITYDASHBOARD:
        return getIdentityDashboardElement(stream);
      case IDENTITYSELECTOR:
        return new IdentitySelectorElement();
      case IDENTITYTRIGGER:
        return getIdentityTriggerElement(stream);
      case INTEGRATIONCONFIG:
        return getIntegrationConfigElement(stream);
      case OBJECTCONFIG:
        // ObjectConfig can contains references to app sources
        // In the Attribute mappings 
        return getObjectConfigElement(stream);
      case OPTIONSSCRIPT:
        return new OptionsScriptElement();
      case POLICY:
        // Policies can contain references
        return getPolicyElement(stream);
      case QUERYSCRIPT:
        return new QueryScriptElement();
      case QUICKLINK:
        return getQuickLinkElement(stream);
      case REFERENCE:
        return new ReferenceElement(stream);        
      case RENDERSCRIPT:
        return new RenderScriptElement();
      case RULE:
        return getRuleElement(stream);
//      case SCRIPT:
//        return getScriptElement(stream);
      case SECTION:
        return getSectionElement(stream);
      case STEP:
        return getStepElement(stream);
      case TARGETSOURCE:
        return getTargetSourceElement(stream);
      case TASKDEFINITION:
        return getTaskDefinitionElement(stream);
      case TEMPLATE:
        return getTemplateElement(stream);
      case TRANSITION:
        return new TransitionElement();
      case VALIDATIONSCRIPT:
        return getValidationScriptElement(stream);
      case VALIDATORSCRIPT:
        return getValidatorScriptElement(stream);
      case VALUE:
        return new ValueElement();
      case VALUESCRIPT:
        return new ValueScriptElement();
      case VARIABLE:
        return new VariableElement(stream);
      case WORKFLOW:
        return getWorkflowElement(stream);
      default:
        return getUninterestingElement(stream);
    }
  }
       
  private IArtifactElement getApplicationElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue("", "name");
    
    ApplicationElement el=new ApplicationElement(name);
    return el;
    
  }
  
  private IArtifactElement getApprovalElement(XMLStreamReader stream) {
    
    String send=stream.getAttributeValue(null, "send");
    
    ApprovalElement el=new ApprovalElement(send);
    
    return el;
  }
  
  private IArtifactElement getBundleElement(XMLStreamReader stream) {
    
    String name = stream.getAttributeValue(null, "name");
    BundleElement el=new BundleElement(name);
    return el;
  }
  
  private IArtifactElement getCapabilityElement(XMLStreamReader stream) {
    
    String name = stream.getAttributeValue(null, "name");
    CapabilityElement el=new CapabilityElement(name);
    return el;
  }
  
  private IArtifactElement getChartElement(XMLStreamReader stream) {
    
    String title = stream.getAttributeValue(null, "title");
    ChartElement el=new ChartElement(title);
    return el;
  }
  
  private IArtifactElement getConfigurationElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue(null, "name");
    
    ConfigurationElement aEl=new ConfigurationElement(name);
    
    return aEl;
  }
  
  private IArtifactElement getCorrelationConfigElement(XMLStreamReader stream) {
    
    String name = stream.getAttributeValue(null, "name");
    CorrelationConfigElement el=new CorrelationConfigElement(name);
    return el;
  }
  
  private IArtifactElement getDynamicScopeElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue("", "name");
    
    DynamicScopeElement el=new DynamicScopeElement(name);
    
    return el;
    
  }
  
  private IArtifactElement getEntryElement(XMLStreamReader stream) {
    
    String key = stream.getAttributeValue(null, "key");
    boolean needsReturn=false;
    String returnType=null;
    
    if ( "requester".equals(key) || "launcher".equals(key) ) {
      needsReturn=true;
      returnType="java.lang.String";
    } else if ("textScript".equalsIgnoreCase(key) ) {
      needsReturn=true;
      returnType="int";
    } else if ("ownerId".equalsIgnoreCase(key) ) {
      needsReturn=true;
      returnType="java.lang.String";
    }
    
    EntryElement ee=new EntryElement(key, needsReturn, returnType);
    return ee;
  }
  
  private IArtifactElement getExtendedColumnScriptElement(XMLStreamReader stream) {
    
    ExtendedColumnScriptElement el=new ExtendedColumnScriptElement();
    return el;
    
  }
  
  private IArtifactElement getFormElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue("", "name");
    
    FormElement el=new FormElement(name);
    
    return el;
    
  }
  
  private IArtifactElement getGroupDefinitionElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue(null, "name");
    
    GroupDefinitionElement el=new GroupDefinitionElement(name);
    
    return el;
  }
  
  private IArtifactElement getIdentityDashboardElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue("", "name");
    
    IdentityDashboardElement el=new IdentityDashboardElement(name);
    return el;
  }

  private IArtifactElement getIdentityTriggerElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue("", "name");
    
    IdentityTriggerElement el=new IdentityTriggerElement(name);
    return el;
    
  }
  
  private IArtifactElement getIntegrationConfigElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue("", "name");
    
    IntegrationConfigElement el=new IntegrationConfigElement(name);
    return el;
    
  }
  
  private IArtifactElement getObjectConfigElement(XMLStreamReader stream) {

    String name=stream.getAttributeValue("", "name");
    
    ObjectConfigElement el=new ObjectConfigElement(name);
    return el;
  }
  
  private IArtifactElement getPolicyElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue("", "name");
    
    PolicyElement el=new PolicyElement(name);
    
    return el;
    
  }
  
  private IArtifactElement getQuickLinkElement(XMLStreamReader stream) {
    
    String name = stream.getAttributeValue(null, "name");
    QuickLinkElement el=new QuickLinkElement(name);
    return el;
  }
  
  private IArtifactElement getRuleElement(XMLStreamReader stream) {
    
    String ruleName=stream.getAttributeValue(null, "name");
    String ruleType=stream.getAttributeValue(null, "type");
    String ruleLanguage=stream.getAttributeValue(null, "language");
    if (ruleLanguage==null) ruleLanguage="beanshell";
    
    Rule ruleByType=null;
    boolean needsReturn=false;
    
    RuleElement re=null;
    
    if((ruleType==null) || ruleType.equals("")) {
        // Special case. If the ruleType is empty, chances are that this is a rule designed
        // to be run with the Rule Executor Task. If this is the case it needs to return a value
        // otherwise the task "fails". Let's assume it's a java.lang.Object
      re=new RuleElement(ruleName, null, "java.lang.Object", ruleLanguage);
    } else {
      ruleByType = IIQPlugin.getRuleRegistry().getModel().getRuleByType(ruleType);
      if(ruleByType!=null) {
        re=new RuleElement(ruleName, ruleByType, ruleLanguage);
      } else {
        re=new RuleElement(ruleName, ruleType, null, ruleLanguage);
      }
    }
    
    return re; 
    
  }
  
  private IArtifactElement getScriptElement(XMLStreamReader stream) {
    ScriptElement aEl=new ScriptElement();
    return aEl;
  }

  private IArtifactElement getSectionElement(XMLStreamReader stream) {
    
    SectionElement aEl=new SectionElement();
    
    return aEl;
  }

  private IArtifactElement getStepElement(XMLStreamReader stream) {
    
    String result=stream.getAttributeValue(null, "resultVariable");
    
    StepElement aEl=new StepElement(result);
    
    return aEl;
  }
  
  private IArtifactElement getTargetSourceElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue(null, "name");
    
    TargetSourceElement aEl=new TargetSourceElement(name);
    
    return aEl;
  }
  
  private IArtifactElement getTaskDefinitionElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue(null, "name");
    
    TaskDefinitionElement aEl=new TaskDefinitionElement(name);
    
    return aEl;
  }
  
  private IArtifactElement getTemplateElement(XMLStreamReader stream) {
    TemplateElement te=new TemplateElement();
    return te;
  }
  
  private IArtifactElement getUninterestingElement(XMLStreamReader stream) {
    
    UninterestingElement aEl=new UninterestingElement(stream.getLocalName());
    
    return aEl;
  }
  
  private IArtifactElement getValidationScriptElement(XMLStreamReader stream) {
    
    ValidationScriptElement el=new ValidationScriptElement();
    return el;
    
  }
  
  private IArtifactElement getValidatorScriptElement(XMLStreamReader stream) {
    
    ValidatorScriptElement el=new ValidatorScriptElement();
    return el;
    
  }
  private IArtifactElement getWorkflowElement(XMLStreamReader stream) {
    
    String name=stream.getAttributeValue("", "name");
    
    WorkflowElement el=new WorkflowElement(name);
    return el;
    
  }
  
  
}
