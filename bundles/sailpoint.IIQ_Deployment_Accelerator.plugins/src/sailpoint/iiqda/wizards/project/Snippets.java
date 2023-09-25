package sailpoint.iiqda.wizards.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sailpoint.iiqda.wizards.AbstractModelObject;

public class Snippets extends AbstractModelObject implements Iterable<SnippetDefinition>{

    private final List<SnippetDefinition> snippets= new ArrayList<SnippetDefinition>();

    public void addSnippet(SnippetDefinition def) {
      snippets.add(def);
      firePropertyChange("snippets", null, snippets);
    }

    public void removeSnippetDefinition(SnippetDefinition def) {
      snippets.remove(def);
      firePropertyChange("snippets", null, snippets);
    }

    public void removeSnippetDefinitionByPattern(String pattern) {
      boolean found=false;
      SnippetDefinition theSnippet=null;
      for (SnippetDefinition def: snippets) {
        if (def.getRegex().equals(pattern)) {
          theSnippet=def;
          found=true;
          break;
        }
      }
      if (found) {
        removeSnippetDefinition(theSnippet);
      }
    }
    
    public List<SnippetDefinition> getSnippets() {
      return snippets;
    }
    
    public int size() {
    	return snippets.size();
    }
    
    public List<String> getSPRights() {
    	List<String> rights=new ArrayList<String>();
    	for (SnippetDefinition sd: snippets) {
    		rights.add(sd.getRightRequired());
    	}
    	return rights;
    }

    @Override
    public Iterator<SnippetDefinition> iterator() {
      return snippets.iterator();
    }
    
}
