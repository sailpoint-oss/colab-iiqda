package sailpoint.iiqda.comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IRegion;

public class EntityComparator extends AbstractArtifactComparator {

  private static final Map<Character, String> entities = new HashMap<Character, String>(){
    {
      put('\"', "&quot;");
      put('&',  "&amp;");
      put('\'', "&apos;");
      put('<',  "&lt;");
      put('>',  "&gt;");
    }
  };

  @Override
  public void setInput(Object input, Object ancestor, Object left, Object right) {
    // TODO Auto-generated method stub
    System.out.println("HibernateComparator.setInput:");

  }

  @Override
  public IRegion[] getFilteredRegions(HashMap lineComparison) {
    // Look at THIS_LINE
    // look for id="xxxx"
    // Add that to the IRegion list
    List<IRegion> regions=new ArrayList<IRegion>();
    String thisLine=(String) lineComparison.get("THIS_LINE");
    String otherLine=(String) lineComparison.get("OTHER_LINE");    
    getEntityRegions(regions, thisLine, otherLine);
    return regions.toArray(new IRegion[regions.size()]);
  }

  private void getEntityRegions(List<IRegion> regions, String thisLine, String thatLine) {
    List<EntityEntry> entityParsed=null;
    if(thisLine!=null) {
      // Check for char in this, entity in that
      for (char c: entities.keySet()) {
        int charPos=thisLine.indexOf(c);
        while (charPos!=-1) {
          // we found at least one of this char in thisLine
          // so we need to parse out 'otherLine' for entity references
          if(entityParsed==null) {
            entityParsed=parseForEntities(thatLine);
          }
          if (charPos<entityParsed.size()) {
            EntityEntry possibleEntity=entityParsed.get(charPos);
            if(possibleEntity.length()>1) { // i.e. not a char
              IRegion region=new TextRegion(charPos, 1);
              StringBuilder sb=new StringBuilder("Adding region: ");
              sb.append(thisLine.substring(0, charPos));
              sb.append("{{");
              sb.append(c);
              sb.append("}}");
              sb.append(thisLine.substring(charPos+1));
              System.out.println(sb.toString());
              regions.add(region);
            }
          }
          charPos=thisLine.indexOf(c, charPos+1);
        }
      }
      // Check for entity in this, char in that
      boolean found=false;
      for (char c: entities.keySet()) {
        String entity=entities.get(c);        
        if (thisLine.contains(entity)) {
          found=true;
          continue;
        }
      }
      if (found) {
        // we found at least one entity in thisLine
        // so we need to parse out 'thisLine' to find character locations
        entityParsed=parseForEntities(thisLine);
        for (int charLoc=0;charLoc<entityParsed.size();charLoc++) {
          EntityEntry entityEntry=entityParsed.get(charLoc);
          if (entities.containsValue(entityEntry.value())) {
            // Could use apache collections here
            // But I'm not going to import another library just for this
            char key=0x0;
            for (char c: entities.keySet()) {
              if (entities.get(c).equals(entityEntry.value())) {
                key=c;
              }
            }
            int loc = entityEntry.location();
            if(charLoc<thatLine.length() && thatLine.charAt(charLoc)==key) {
              IRegion region=new TextRegion(loc, entityEntry.length());
              regions.add(region);
            }
          }
        }
        //            StringBuilder sb=new StringBuilder("Adding region: ");
        //            sb.append(thisLine.substring(0, charPos));
        //            sb.append("{{");
        //            sb.append(c);
        //            sb.append("}}");
        //            sb.append(thisLine.substring(charPos+1));
        //            System.out.println(sb.toString());
      }
    }

  }

  private List<EntityEntry> parseForEntities(String thatLine) {
    List<EntityEntry> parsed=new ArrayList<EntityEntry>();
    for (int i=0;i<thatLine.length();i++) {
      char c=thatLine.charAt(i);
      if(c=='&') {
        // find an entity reference - this to the next ';'
        int semicolon=thatLine.indexOf(';', i);
        if(semicolon!=-1) {
          // is it an entity?
          String possibleEntity=thatLine.substring(i, semicolon+1); // +1 to include semicolon
          if(entities.containsValue(possibleEntity)) {
            // put the entity to the outChar; advance the counter past the semicolon
            parsed.add(new EntityEntry(possibleEntity, i));
            i=semicolon;
          }
        }
      } else {
        String outChar=new Character(c).toString();
        parsed.add(new EntityEntry(outChar, i));
      }
    }
    return parsed;
  }
  private class EntityEntry {
    private String entity;
    private int originalLocation;

    public EntityEntry(String entity, int loc) {
      this.entity=entity;
      this.originalLocation=loc;
    }
    public int location() {
      return originalLocation;
    }
    public String value() {
      return entity;
    }
    public int length() {
      return entity.length();
    }
  }
}