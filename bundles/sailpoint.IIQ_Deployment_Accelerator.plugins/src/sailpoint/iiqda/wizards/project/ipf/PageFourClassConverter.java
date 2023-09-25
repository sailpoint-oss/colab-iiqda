package sailpoint.iiqda.wizards.project.ipf;

import org.eclipse.core.databinding.conversion.IConverter;

public class PageFourClassConverter implements IConverter {

  public PageFourClassConverter() {}
  
  @Override
  public Object getFromType() {
    // TODO Auto-generated method stub
    System.out.println("page 5ClassConverter.getFromType:");
    return String.class;
  }

  @Override
  public Object getToType() {
    // TODO Auto-generated method stub
    System.out.println("page 5ClassConverter.getToType:");
    return String.class;
  }

  @Override
  public Object convert(Object fromObject) {
    
    String from=(String) fromObject;
    
    if(from==null || from.length()==0) return "";
    return from.substring(0, 1).toUpperCase()+from.substring(1)+"Resource";
  }

}