package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class WorkflowElement extends AbstractArtifactRootElement implements IArtifactElement, IArtifactRootElement {
  
  public WorkflowElement(String name) {
    super(name);
    // TODO Auto-generated constructor stub
  }

  private List<ReferenceElement> references;
  

  @Override
  public ArtifactType getType() {
    return ArtifactType.WORKFLOW;
  }

  @Override
  public List<Variable> getVariables() {
    /*
     * Add default workflow variables, and also any variables defined at the
     * beginning of the workflow (that are available to every step)
     */
    List<Variable> vars=new ArrayList<Variable>();

    // Add some variables you get for free with workflow
    vars.add(new Variable("sailpoint.object.Workflow.Approval", "approval", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.workflow.WorkflowHandler", "handler", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.WorkItem", "item", Variable.Source.CONTEXT));
    vars.add(new Variable("java.lang.String", "launcher", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.Workflow.Step", "step", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.workflow.WorkflowContext", "wfcontext", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.WorkflowCase", "wfcase", Variable.Source.CONTEXT));
    vars.add(new Variable("sailpoint.object.Workflow", "workflow", Variable.Source.CONTEXT));
    vars.add(new Variable("boolean", "approved", Variable.Source.CONTEXT));
    // Add the variables declared at the start of the workflow
    for(Variable var: workflowVariables()) {
      if(!var.getName().equals("transient")) {
        vars.add(var);
      }
    }
    
    return vars;
  }
  
  public List<Variable> workflowVariables() {
    
    List<Variable> wfVars=new ArrayList<Variable>();
    
    for(IArtifactElement aEl: children) {
      if(aEl instanceof VariableElement) {
        VariableElement ve=(VariableElement)aEl;
        wfVars.add(new Variable(ve.getReturnType(), ve.getVariableName(), Variable.Source.WORKFLOW_VARIABLE));
      }
    }
    
    return wfVars;
  }

  public Variable getVariable(String sendVar) {
    for (Variable v: workflowVariables()) {
      if (v.getName().equals(sendVar)) {
        return v;
      }
    }
    return null;
  }
}
