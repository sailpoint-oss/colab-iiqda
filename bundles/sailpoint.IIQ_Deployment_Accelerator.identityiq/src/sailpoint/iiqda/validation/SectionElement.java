package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

public class SectionElement extends AbstractArtifactElement {

  public List<FieldElement> getFields() {
    List<FieldElement> fields=new ArrayList<FieldElement>();
    for(IArtifactElement aEl: children) {
      if(aEl instanceof FieldElement) {
        fields.add((FieldElement)aEl);
      } else if (aEl instanceof SectionElement) {
        // I don't know if we can have sections in sections, but if not this does nothing
        fields.addAll( ((SectionElement)aEl).getFields() );
      }
    }
    return fields;
  }

  @Override
  public List<Variable> getVariables() {
    // A <Section> in a <Form> can have an <Attributes> section which describes whether the
    // <Section> is read-only, hidden etc.
    // The scripts for these attributes have to know certain things.. a <Field> in a <Section>
    // implements the getVariables() method itself, so the <Field> will never drop this far to get
    // these variables.
    List<Variable> vars=new ArrayList<Variable>();
    
    vars.add(new Variable("sailpoint.object.Form", "form", Variable.Source.CONTEXT));
    return vars;
  }

  
}
