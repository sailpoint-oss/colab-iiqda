package sailpoint.iiqda.validation;

import java.util.Collection;
import java.util.List;

public interface IFieldContainingElement {

  public List<FieldElement> getFields();

  public boolean isApprovalForm();

  public Collection<Variable> getSentVariables();

}
