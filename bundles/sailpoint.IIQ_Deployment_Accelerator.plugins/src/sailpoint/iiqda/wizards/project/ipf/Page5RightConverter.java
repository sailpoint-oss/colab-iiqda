package sailpoint.iiqda.wizards.project.ipf;

import org.eclipse.core.databinding.conversion.IConverter;

public class Page5RightConverter implements IConverter {

  public Page5RightConverter() {}
  
  @Override
  public Object getFromType() {
    // TODO Auto-generated method stub
    System.out.println("page 5RightConverter.getFromType:");
    return String.class;
  }

  @Override
  public Object getToType() {
    // TODO Auto-generated method stub
    System.out.println("page 5RightConverter.getToType:");
    return String.class;
  }

  @Override
  public Object convert(Object fromObject) {
    
    String from=(String) fromObject;
    
    if(from==null || from.length()==0) return "";
    return from+"RESTAllow";
  }

}