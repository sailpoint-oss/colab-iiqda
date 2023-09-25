package sailpoint.iiqda.wizards.project;

import org.eclipse.core.databinding.conversion.IConverter;

public class RightConverter implements IConverter {

  public RightConverter() {}
  
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
    return from+"Right";
  }

}
