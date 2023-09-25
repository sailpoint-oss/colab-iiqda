package sailpoint.iiqda;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

public class MyResourceChangeReporter implements IResourceChangeListener {

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    // TODO Auto-generated method stub
    System.out.println("MyResourceChangeReporter.resourceChanged:");
    IResourceDelta delta = event.getDelta();
    if (delta!=null) {
      handleDelta(delta, "");
    }

  }

  private void handleDelta(IResourceDelta delta, String indent) {
    if (delta.getAffectedChildren().length!=0) {
      for (IResourceDelta cDelta: delta.getAffectedChildren()) {
        handleDelta(cDelta, indent+"..");
      }
    } else {
      System.out.println(indent+"Delta for: "+delta.getResource().getName()+" : "+delta.getKind());
    }
  }

}
