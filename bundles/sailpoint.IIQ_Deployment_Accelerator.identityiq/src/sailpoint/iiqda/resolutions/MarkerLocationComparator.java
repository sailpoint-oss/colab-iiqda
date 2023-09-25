package sailpoint.iiqda.resolutions;

import java.util.Comparator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class MarkerLocationComparator implements Comparator<IMarker> {

	@Override
  public int compare(IMarker o1, IMarker o2) {
	  try {
	    int o1start=(Integer)o1.getAttribute(IMarker.CHAR_START);
	    int o2start=(Integer)o2.getAttribute(IMarker.CHAR_START);
	    int diff=o1start-o2start;
	    if(diff<0) return 1;
	    if(diff==0) return 0;
	    return -1;
    } catch (CoreException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return 0;
    }
  }

}
