package sailpoint.iiqda.comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.IRegion;

public class QuoteComparator extends AbstractArtifactComparator {

  @Override
  public IRegion[] getFilteredRegions(HashMap lineComparison) {
    // Look at THIS_LINE
    // look for differing quotes
    // Add them to the IRegion list
    List<IRegion> regions=new ArrayList<IRegion>();
    String thisLine=(String) lineComparison.get("THIS_LINE");
    String otherLine=(String)lineComparison.get("OTHER_LINE");
    char dblQuot = '\"';
    char sglQuot = '\'';
    getQuoteRegions(regions, thisLine, otherLine, dblQuot, sglQuot);
    getQuoteRegions(regions, thisLine, otherLine, sglQuot, dblQuot);
    return regions.toArray(new IRegion[regions.size()]);
  }

  private void getQuoteRegions(List<IRegion> regions, String thisLine, String otherLine, char a, char b) {
    int quot=thisLine.indexOf(a);
    while (quot!=-1) {
      if (otherLine.length()>quot && otherLine.charAt(quot)==b) {
        regions.add(new TextRegion(quot, 1));
      }
      quot=thisLine.indexOf(a, quot+1);
    }

  }
}
