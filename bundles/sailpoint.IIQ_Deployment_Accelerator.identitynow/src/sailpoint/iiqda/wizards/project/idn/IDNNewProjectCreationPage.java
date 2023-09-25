package sailpoint.iiqda.wizards.project.idn;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import sailpoint.iiqda.IDNPlugin;

public class IDNNewProjectCreationPage extends WizardNewProjectCreationPage implements Listener {
  
  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IDNPlugin.PLUGIN_ID+"/debug/Wizards"));
  
  
  private boolean _isComplete=false;
  private IDNDetailsControl idnDetails;
  
  public IDNNewProjectCreationPage(String pageName) {
    super(pageName);
    
  }

  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    if (DEBUG_WIZARDS) IDNPlugin.logDebug("parent="+Integer.toHexString(parent.hashCode()));
    Composite thecontrol = (Composite)getControl();
    idnDetails = new IDNDetailsControl(thecontrol, SWT.NONE);
    idnDetails.addListener(SWT.Modify, this);

    GridData gridData = new GridData();
    gridData.horizontalAlignment=GridData.FILL;
    gridData.verticalAlignment=GridData.FILL;
    gridData.grabExcessVerticalSpace=true;    
    idnDetails.setLayoutData(gridData);

  }
  
  @Override
  public boolean isPageComplete() {
    System.out.println("IDNNewProjectCreationPage.isPageComplete: ");
    return super.isPageComplete()&&idnDetails.isComplete();
  }

  @Override
  public void handleEvent(Event arg0) {
    // TODO Auto-generated method stub
    System.out.println("Listener.handleEvent:");
    getWizard().getContainer().updateButtons();
  }
  
  public IDNPageData getPageData() {
    return idnDetails.getIDNData();
  }
  
}
