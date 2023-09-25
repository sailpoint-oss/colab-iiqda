package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.builder.SourceElement;
import sailpoint.iiqda.internal.Rule;

public abstract class AbstractArtifactElement implements IArtifactElement {

  protected List<IArtifactElement> children;
  protected IArtifactElement parent;
  
  public AbstractArtifactElement() {
   children=new ArrayList<IArtifactElement>();
  }
  
  @Override
  public void setParent(IArtifactElement peek) {
    this.parent=peek;
    ((AbstractArtifactElement)peek).addChild(this);
  }
  
  @Override
  public void addChild(IArtifactElement newChild) {
    children.add(newChild);
  }
  @Override
  public List<IArtifactElement> getChildren() {
    return children;
  }

  @Override
  public IArtifactElement getParent() {
    return this.parent;
  }
  
  @Override
  public List<SourceElement> getSourceElements() {
    List<SourceElement> sources=new ArrayList<SourceElement>();
    addSources(sources, this);
    return sources;
  }
  
  private void addSources(List<SourceElement> sources, IArtifactElement aEl) {
    if(aEl instanceof IScriptContainerElement) {
      IScriptContainerElement scEl = (IScriptContainerElement)aEl;
      if (scEl.getSource()!=null) {
        sources.add(scEl.getSource());
      }
    }
    for(IArtifactElement child: aEl.getChildren()) {
      addSources(sources, child);
    }
  }
  
  protected List<Variable> getRuleVariables(String ruleType) {
    // Figure out which variables are passed in for the given type of rule
//TODO: put this in the right place
//    if(ruleType==null) {
//      if(resource!=null) addMarker(getSeverityPreference(resource, IIQPreferenceConstants.P_NO_RULE_TYPE), "No rule type specified", getRootElementStart(), getRootElementEnd(), getRootElementLine());
//      addDefaultVariables(vars);
//      return;
//    }
    
//TODO: put this in the right place
    List<Variable> vars=new ArrayList<Variable>();
    Rule rule=IIQPlugin.getRuleRegistry().getModel().getRuleByType(ruleType);
    if(rule==null) {
//      if(resource!=null) addMarker(getSeverityPreference(resource, IIQPreferenceConstants.P_UNKNOWN_RULE_TYPE), "Unknown Rule type '"+ruleType+"'", getRootElementStart(), getRootElementEnd(), getRootElementLine());
      return vars;
    }
    Map<String,String> args=rule.getArgumentSignatures();
    for(String varname: args.keySet()) {
      vars.add(new Variable(args.get(varname), varname, Variable.Source.CONTEXT));
    }
    return vars;
  }
  
  // The default for this is to defer up the list
  // There are any number of elements that sit between <Source> and the element that would define variables
  // Any element that actually provides variables can override it
  @Override
  public List<Variable> getVariables() {
    if(parent!=null) {
      return parent.getVariables();
    }
    return null;
  }
  
}
