package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TemplateElement extends AbstractArtifactElement implements IFieldContainingElement {

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

  @Override
  public boolean isApprovalForm() {
    return false;
  }

  @Override
  public Collection<Variable> getSentVariables() {
    return null;
  }

}
