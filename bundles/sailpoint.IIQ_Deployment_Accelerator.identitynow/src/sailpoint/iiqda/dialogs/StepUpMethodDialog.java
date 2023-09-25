package sailpoint.iiqda.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sailpoint.iiqda.idn.StepUpMethod;

public class StepUpMethodDialog extends Dialog {
  
  private List<StepUpMethod> methods;

  private List<Button> bMethods;
  private String btnSelection;
  
  public StepUpMethodDialog(Shell parent, List<StepUpMethod> methods) {
    
    super(parent);
    this.methods=methods;
    
  }

  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText("Select StepUp Method");
  }
  
  /**
   * Create contents of the dialog.
   * @param parent
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    container.setLayout(new GridLayout(1, false));
    
    Label lbl = new Label(container, SWT.NONE);
    lbl.setText("Please Select method of StepUp Authentication");
    
    for (StepUpMethod method: methods) {
      Button btnA = new Button(container, SWT.RADIO);
      btnA.setText(method.getDescription());
      btnA.setData(method.getStrongAuthType());
      btnA.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          Button source=(Button) e.getSource();             
          if(source.getSelection())  {
            btnSelection=(String)source.getData();
          }
        }
      });
    }
    return container; 
  }
  
  public StepUpMethod getSelectedMethod() {
    for (StepUpMethod method: methods) {
      if (method.getStrongAuthType().equals(btnSelection)) {
        return method;
      }
    }
    return null;
  }
  
}
