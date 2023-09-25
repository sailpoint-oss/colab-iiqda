package sailpoint.iiqda.validation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.eclipse.core.runtime.Platform;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.builder.SourceElement;

public class FieldElement extends AbstractArtifactElement implements IScriptContainerElement, IRuleBasedScript {
  
  private static final boolean DEBUG_ELEMENTS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Elements"));

  private String fieldName;
  private String fieldType;
  private List<String> dependencies;
  private SourceElement se;

  public FieldElement(XMLStreamReader stream) {
    
    fieldName=stream.getAttributeValue("", "name");
    fieldType=stream.getAttributeValue("", "type");
    String sDependencies=stream.getAttributeValue("", "dependencies");
    dependencies=new ArrayList<String>();
    if(sDependencies!=null) {
      for(String dependency: sDependencies.split(",")) {
        dependencies.add(dependency);
      }
    }
    if(fieldType==null) fieldType="";
    switch(fieldType) {
      case "string":
      case "secret":
        fieldType="java.lang.String";
        break;
      case "identity":
        fieldType="sailpoint.object.Identity";
      default:
        if (DEBUG_ELEMENTS) IIQPlugin.logDebug("unhandled field return type : "+fieldType);
        fieldType="java.lang.Object";
    }
    //    workflowVariables.add(new Variable(varType, varName, Variable.Source.WORKFLOW_VARIABLE));

  }

  public String getFieldName() {
    return fieldName;
  }

  @Override
  public boolean needsReturn() {
    return true;
  }

  @Override
  public String getReturnType() {
    return fieldType;
  }

  @Override
  public String getRuleType() {
    return "FieldValue";
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
    // We need to get variables for all the fields that were previously defined
    // plus any 'depends' fields
    
    List<Variable> vars=new ArrayList<Variable>();
    
    List<FieldElement> fields=getFormFields(parent);
    boolean myField=false;
    for(FieldElement field: fields) {
      if(field.equals(this)) {
        myField=true;
      }
      String fieldName=field.getFieldName();
      if((!myField || dependencies.contains(fieldName))) {
        if (fieldName!=null) {// can have fields with no name, e.g. for display text. Ignore them
          addFormVariable(vars, field.getReturnType(), field.getFieldName());
        }
      }
    }
    // and 'FieldValue' rule variables
    vars.addAll(getRuleVariables("FieldValue"));
    // Then we pass the list up the chain in case this form was part of an approval
    // At that point, we need to pass the 'sends' variables from the workflow
    if (parent instanceof IFieldContainingElement && ((IFieldContainingElement)parent).isApprovalForm()) {
      vars.addAll( ((IFieldContainingElement)parent).getSentVariables() );
    }
    return vars;
  }

  private List<FieldElement> getFormFields(IArtifactElement formParent2) {
    if(formParent2 instanceof IFieldContainingElement) {
      return ((IFieldContainingElement)formParent2).getFields();
    } else {
      // Probably <Section>
      return getFormFields(formParent2.getParent());
    }
  }

  private void addFormVariable(List<Variable> vars, String type, String name) {

    // TODO: Only add the fields *before* this field, or ones in the 'dependencies' attribute

    if(type.contains(".")) { // Fully qualified type
      if (DEBUG_ELEMENTS) IIQPlugin.logDebug("var.getType()="+type);
      vars.add(new Variable(type, name, Variable.Source.FORM_FIELD));
    } else {
      switch(type) {
        case "date":
          vars.add(new Variable("java.util.Date", name, Variable.Source.FORM_FIELD));
          break;
        case "boolean":
          vars.add(new Variable("boolean", name, Variable.Source.FORM_FIELD));
          break;
        case "string":
          vars.add(new Variable("java.lang.String", name, Variable.Source.FORM_FIELD));
          break;
        case "Identity":
          vars.add(new Variable("sailpoint.object.Identity", name, Variable.Source.FORM_FIELD));
          break;
        default:
          if (DEBUG_ELEMENTS) IIQPlugin.logDebug("Unhandled Form field type: "+type);
      }
    }
  }



}
