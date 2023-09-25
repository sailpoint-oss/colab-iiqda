package sailpoint.iiqda.wizards;

import org.eclipse.core.databinding.conversion.IConverter;

import sailpoint.iiqda.core.CoreUtils;

public class CamelConverter implements IConverter {

  public CamelConverter() {}
  
  @Override
  public Object getFromType() {
    return String.class;
  }

  @Override
  public Object getToType() {
    return String.class;
  }

  @Override
  public Object convert(Object fromObject) {
    
    String from=(String) fromObject;
    
    if(from==null || from.length()==0) return "";
    return CoreUtils.toCamelCase(from);
  }

}
