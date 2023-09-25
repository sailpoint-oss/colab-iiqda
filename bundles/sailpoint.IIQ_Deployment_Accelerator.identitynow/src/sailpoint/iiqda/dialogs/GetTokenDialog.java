package sailpoint.iiqda.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;

public class GetTokenDialog extends Dialog {
  
  private String sToken;
  private Text tToken;
  
  public GetTokenDialog(Shell parent) {
    
    super(parent);
    
  }

  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText("Enter Verification Token");
  }
  
  /**
   * Create contents of the dialog.
   * @param parent
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    container.setLayout(new GridLayout(2, false));
    
    Label lbl = new Label(container, SWT.NONE);
    lbl.setText("Enter Verification Token");

    tToken = new Text(container, SWT.BORDER);
    tToken.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    tToken.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent arg0) {
          sToken=tToken.getText();
        }
    });    
    return container; 
  }
  
  public String getToken() {
    return sToken;
  }
  
}
