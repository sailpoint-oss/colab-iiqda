package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.validation.BaseXMLArtifactParser.ArtifactType;

public class FormElement extends AbstractArtifactRootElement implements IArtifactElement, IArtifactRootElement, IFieldContainingElement {

  private List<FieldElement> fields;

  public FormElement(String formName) {
    super(formName);
    this.fields=new ArrayList<FieldElement>();
  }

  @Override
  public ArtifactType getType() {
    return ArtifactType.FORM;
  }

  @Override
  public List<FieldElement> getFields() {
    List<FieldElement> fields=new ArrayList<FieldElement>();
    for(IArtifactElement aEl: children) {
      if(aEl instanceof FieldElement) {
        fields.add((FieldElement)aEl);
      } else if (aEl instanceof SectionElement) {
        fields.addAll( ((SectionElement)aEl).getFields() );
      }
    }
    return fields;
  }

  public boolean isApprovalForm() {
    return (parent instanceof ApprovalElement);
  }

  public List<Variable> getSentVariables() {

    if(!(parent instanceof ApprovalElement)) {
      return null;
    }

    ApprovalElement ae=(ApprovalElement)parent;
    WorkflowElement wf=((ApprovalElement)parent).getWorkflow();
    List<Variable> vars=new ArrayList<Variable>();

    for(String sendVar: ae.getSendVariables()) {
      vars.add(wf.getVariable(sendVar));
    }

    return vars;
  }

}
