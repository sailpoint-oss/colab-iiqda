package sailpoint.iiqda.widgets.idn;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import sailpoint.iiqda.objects.idn.Transform;
import sailpoint.iiqda.objects.idn.Transform.Type;

public class TransformTypeDialog extends Dialog {

  private TransformTypeComboViewer combo;
  private Type type;

  protected TransformTypeDialog(Shell parentShell) {
    super(parentShell);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    System.out.println("TransformTypeDialog.createDialogArea:");
    combo = new TransformTypeComboViewer(container);

    if (type!=null) {
      combo.setSelection(new StructuredSelection(type));
    }
    
    return container;
  }

  @Override
  protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("New Transform Type");
  }

  public Transform.Type getType() {
    return combo.getType();
  }
  
  public int open(Transform.Type type) {
    this.type=type;
    return open();
  }
  
}
