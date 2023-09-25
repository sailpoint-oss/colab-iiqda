package sailpoint.iiqda.comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.IRegion;

public class HibernateComparator extends AbstractArtifactComparator {

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
    getAttributeRegions(regions, "id", thisLine);
    getAttributeRegions(regions, "created", thisLine);
    getAttributeRegions(regions, "modified", thisLine);
    return regions.toArray(new IRegion[regions.size()]);
  }

  private void getAttributeRegions(List<IRegion> regions, String attribute, String thisLine) {
    if(thisLine!=null) {
      String lookfor = attribute+"=";
      int idLoc=thisLine.indexOf(lookfor);
      while(idLoc!=-1) {
        char quoteType=thisLine.charAt(idLoc+lookfor.length());
        int closeQuote=thisLine.indexOf(quoteType, idLoc+lookfor.length()+1);
        IRegion region=new TextRegion(idLoc, (closeQuote-idLoc)+1);
        regions.add(region);
        idLoc=thisLine.indexOf(lookfor, closeQuote+1);
      }
    }
  }
}