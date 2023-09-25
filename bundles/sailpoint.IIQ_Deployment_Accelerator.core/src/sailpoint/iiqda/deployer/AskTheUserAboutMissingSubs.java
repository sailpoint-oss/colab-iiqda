package sailpoint.iiqda.deployer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class AskTheUserAboutMissingSubs implements Runnable {

  private boolean shouldQuit=false;
  private IStatus missing;

  public AskTheUserAboutMissingSubs(IStatus missing) {
    this.missing=missing;
  }
  
  private class CancellableErrorDialog extends ErrorDialog {
    
    public CancellableErrorDialog(Shell parentShell, String dialogTitle,
        String message, IStatus status, int displayMask) {
      super(parentShell, dialogTitle, message, status, displayMask);
      // TODO Auto-generated constructor stub
    }    
    
    protected void createButtonsForButtonBar(Composite parent) {
      // create OK and Details buttons
      createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
          true);
      createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL,
          true);
      createDetailsButton(parent);
    }
  }

  @Override
  public void run() {
    
    CancellableErrorDialog ced=new CancellableErrorDialog(
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
        "Substitution macros missing",
        null,
        missing,
        IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
    if(ced.open()!=Window.OK) shouldQuit=true;
  }
  
  public boolean shouldQuit() {
    return shouldQuit;
  }
}