package sailpoint.iiqda.wizards.project;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TextEntryDialog extends Dialog {
  private Text text;

  private String txtValue;

  private String title;

  private String sTextInfo="";
	private Label lblTextInfo;
  
  public TextEntryDialog(Shell parent, String title) {
    super(parent);
    this.title=title;
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(title);
  }
  
  public void setInfoLabel(String label) {
  	if (lblTextInfo!=null) lblTextInfo.setText(label);
  	this.sTextInfo=label;
  }
  
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    container.setLayout(new GridLayout(1, false));
    
    lblTextInfo = new Label(container, SWT.NONE);
    lblTextInfo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    lblTextInfo.setText(sTextInfo);
    
    text = new Text(container, SWT.BORDER);
    text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    return container;
  }
  
  @Override
  protected void okPressed() {
    txtValue = text.getText();
    super.okPressed();
  }
  
  public String getTextValue() {
    return txtValue;
  }
  
}
