package sailpoint.iiqda.wizards.project.ipf;

import java.util.ArrayList;
import java.util.List;

import sailpoint.iiqda.wizards.AbstractModelObject;
import sailpoint.iiqda.wizards.project.ListEntry;

public class PageDataLibraries extends AbstractModelObject {

  private String iiqLocation;
  private List<ListEntry> references;
  private List<ListEntry> includes;

  public PageDataLibraries(){
    this.includes=new ArrayList<ListEntry>();
    this.references=new ArrayList<ListEntry>();
  }
  
	public String getIIQLocation() {
		return iiqLocation;
	}

	public void setIIQLocation(String iiqLocation) {
		this.iiqLocation = iiqLocation;
	}

  public List<ListEntry> getReferences() {
    return references;
  }

  public void setReferences(List<ListEntry> references) {
    this.references = references;
  }

  public List<ListEntry> getIncludes() {
    return includes;
  }

  public void setIncludes(List<ListEntry> includes) {
    this.includes = includes;
  }
  
  public void addInclude(String include) {
    ListEntry le = new ListEntry(include);
    includes.add(le);
    firePropertyChange("includes", null, le);
  }
  
  public void addReference(String reference) {
    ListEntry le = new ListEntry(reference);
    references.add(le);
    firePropertyChange("references", null, le);
  }
  
  public void removeReference(String reference) {
    ListEntry le = new ListEntry(reference);
    references.remove(le);
    firePropertyChange("references", null, le);
  }
  
  public void removeInclude(String include) {
    ListEntry le = new ListEntry(include);
    includes.remove(le);
    firePropertyChange("includes", null, le);
  }
	
}
