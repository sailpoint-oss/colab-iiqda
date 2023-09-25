package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.builder.SourceElement;

public class RenderScriptElement extends AbstractArtifactElement implements
    IArtifactElement, IScriptContainerElement, IRuleBasedScript {

  private SourceElement se;

  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return "java.lang.Object";
  }

  @Override
  public String getRuleType() {
    return "RenderScript";
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
  public List<Variable> getVariables() {

    List<Variable> vars=new ArrayList<Variable>();
    
    vars.add(new Variable("java.lang.Object", "value", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.ReportColumnConfig", "column", Variable.Source.CONTEXT));
    vars.add(new Variable("java.util.Map", "scriptArgs", Variable.Source.CONTEXT));
    vars.add(new Variable("java.util.Locale", "locale", Variable.Source.CONTEXT));
    vars.add(new Variable("java.util.TimeZone", "timezone", Variable.Source.CONTEXT));
    vars.add(new Variable("java.util.Map", "renderCache", Variable.Source.CONTEXT));
    
    return vars;
  }

}
