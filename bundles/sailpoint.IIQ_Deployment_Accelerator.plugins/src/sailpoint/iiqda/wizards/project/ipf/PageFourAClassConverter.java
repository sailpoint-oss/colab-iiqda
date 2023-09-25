package sailpoint.iiqda.wizards.project.ipf;

import org.eclipse.core.databinding.conversion.IConverter;

import sailpoint.iiqda.core.CoreUtils;

public class PageFourAClassConverter implements IConverter {

  public PageFourAClassConverter() {}
  
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
    return "sailpoint.server."+CoreUtils.capitalize(from)+"Service";
  }

}