package sailpoint.iiqda.views;

import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class LogComparator extends ViewerComparator {

	@Override
  public int compare(Viewer viewer, Object e1, Object e2) {
	  LoggingEvent l1=(LoggingEvent)e1;
	  LoggingEvent l2=(LoggingEvent)e2;
	  return Long.compare(l1.getTimeStamp(), l2.getTimeStamp());
  }

}
