package sailpoint.iiqda.comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.IRegion;

public class CDATAComparator extends AbstractArtifactComparator {

  @Override
  public void setInput(Object input, Object ancestor, Object left, Object right) {
  }

  @Override
  public IRegion[] getFilteredRegions(HashMap lineComparison) {
    // Look at THIS_LINE
    // look for id="xxxx"
    // Add that to the IRegion list
    List<IRegion> regions=new ArrayList<IRegion>();
    String thisLine=(String) lineComparison.get("THIS_LINE");
    getStringRegions(regions, "<![CDATA[", thisLine);
    getStringRegions(regions, "]]>", thisLine);
    return regions.toArray(new IRegion[regions.size()]);
  }
}