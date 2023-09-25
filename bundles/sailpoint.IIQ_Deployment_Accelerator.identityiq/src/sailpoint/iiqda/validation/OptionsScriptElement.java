package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.builder.SourceElement;

public class OptionsScriptElement extends AbstractArtifactElement implements
IArtifactElement, IScriptContainerElement, IRuleBasedScript {

  private SourceElement se;

  @Override
  public boolean needsReturn() {
    return true;
  } 

  @Override
  public String getReturnType() {
    return "sailpoint.object.QueryOptions";
  }

  @Override
  public String getRuleType() {
    return "OptionsScript";
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

    vars.add(new Variable("java.util.Map", "args", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.QueryOptions", "options", Variable.Source.CONTEXT));

    return vars;
  }

}
