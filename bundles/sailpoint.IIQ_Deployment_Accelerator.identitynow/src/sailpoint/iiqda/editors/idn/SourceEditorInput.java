package sailpoint.iiqda.editors.idn;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class SourceEditorInput implements IEditorInput {

  private final String id; 
  
  public SourceEditorInput(String id) {
    this.id = id;
  }
  
  public String getId() {
      return id;
  }

  @Override
  public <T> T getAdapter(Class<T> arg0) {
    return null;
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return null;
  }

  @Override
  public String getName() {
    return id;
  }

  @Override
  public IPersistableElement getPersistable() {
    System.out.println("IEditorInput.getPersistable:");
    return null;
  }

  @Override
  public String getToolTipText() {
    System.out.println("IEditorInput.getToolTipText:");
    return "Displays an IdentityNow Source object";
  }

}
