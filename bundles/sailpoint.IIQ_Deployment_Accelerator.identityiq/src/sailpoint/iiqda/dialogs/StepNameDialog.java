package sailpoint.iiqda.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class StepNameDialog extends Dialog {
  private Text tStepName;
  private String sStepName;
  private List<String> stepNames;
  private Label lblErrorMessage;

  /**
   * Create the dialog.
   * @param parentShell
   */
  public StepNameDialog(Shell parentShell, List<String> stepNames) {
    super(parentShell);
    this.stepNames=stepNames;
  }

  /**
   * Create contents of the dialog.
   * @param parent
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout gl_container = new GridLayout(2, false);
    gl_container.marginWidth = 1;
    gl_container.marginRight = 10;
    gl_container.marginHeight = 0;
    gl_container.verticalSpacing = 10;
    gl_container.marginTop = 10;
    gl_container.marginLeft = 10;
    container.setLayout(gl_container);
    
    Label lblStepName = new Label(container, SWT.NONE);
    lblStepName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblStepName.setText("Step Name");
    
    tStepName = new Text(container, SWT.BORDER);
    tStepName.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        String sStep = tStepName.getText();
        if (stepNames.contains(sStep)) {
          lblErrorMessage.setText("Step '"+sStep+"' already exists");
          getButton(OK).setEnabled(false);
        } else {
          lblErrorMessage.setText("");
          getButton(OK).setEnabled(true);
        }
      }
    });
    tStepName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    lblErrorMessage = new Label(container, SWT.NONE);
    lblErrorMessage.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED) );
    lblErrorMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
    lblErrorMessage.setText("");

    parent.layout(true, true);
    parent.setSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
    return container;
    
    
  }

  /**
   * Create contents of the button bar.
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
        true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        IDialogConstants.CANCEL_LABEL, false);
  }
  
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Insert Step");
  }

  @Override
  protected void okPressed() {
    this.sStepName=tStepName.getText();
    super.okPressed();
  }

  public String getNewStepName() {
    return sStepName;
  }

  
  
}
