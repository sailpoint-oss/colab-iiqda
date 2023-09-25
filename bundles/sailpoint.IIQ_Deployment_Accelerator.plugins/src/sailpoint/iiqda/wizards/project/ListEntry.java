package sailpoint.iiqda.wizards.project;

import sailpoint.iiqda.wizards.AbstractModelObject;

public class ListEntry extends AbstractModelObject {
  
  private String value;
  
  public ListEntry(String value) {
    this.value=value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj==null) return false;
    if (!(obj instanceof ListEntry)) return false;
    ListEntry other=(ListEntry) obj;
    if(other.getValue()==null && value==null) return true;
    if (value==null && other.getValue()!=null) return false;
    return value.equals(other.getValue());
  }
  
  
  
}
