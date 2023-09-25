package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.builder.SourceElement;

public class DataSourceScriptElement extends AbstractArtifactElement implements
    IArtifactElement, IScriptContainerElement, IRuleBasedScript {

  private SourceElement se;
  
  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return "java.util.Map";
  }

  @Override
  public String getRuleType() {
    return "DataSourceScript";
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
    
    vars.add(new Variable("sailpoint.object.Attributes", "reportArgs", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.LiveReport", "report", Variable.Source.CONTEXT));
    vars.add(new Variable("java.lang.String", "baseHql", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.QueryOptions", "baseQueryOptions", Variable.Source.CONTEXT));
    
    return vars;
  }

}
