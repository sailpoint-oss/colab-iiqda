package sailpoint.iiqda.validation;

public class ApprovalElement extends AbstractArtifactElement {
  
  String[] send;
  
  public ApprovalElement(String send) {
    if(send==null) {
      this.send=new String[0];
    } else {
      this.send=send.split(",");
    }
  }

  public String[] getSendVariables() {
    return send;
  }
  
  public WorkflowElement getWorkflow() {
    return getWorkflow(parent);
  }

  public WorkflowElement getWorkflow(IArtifactElement el) {
    if(el instanceof WorkflowElement) return (WorkflowElement)el;
    if(el.getParent()==null) return null;
    return getWorkflow(el.getParent());
  }
}
