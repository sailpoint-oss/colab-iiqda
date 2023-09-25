package sailpoint.iiqda.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sailpoint.log4j.LogLevelSetting.LogLevel;

public class AddLogDialog extends Dialog {

	Text logClass;
	private Combo level;
	
	String sLogClass=null;
	LogLevel lvl=null;
	
	protected AddLogDialog(Shell parentShell) {
	  super(parentShell);
  }
	
	@Override
  protected Control createDialogArea(Composite parent) {
		
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout layout=new GridLayout(2, false);
    container.setLayout(layout);
    
    logClass=new Text(container, SWT.NONE);
    GridData data=new GridData();
    data.widthHint=200;
    data.grabExcessHorizontalSpace=true;
    logClass.setLayoutData(data);
    
    level = new Combo(container, SWT.NONE);
    for(LogLevel lvl: LogLevel.values()) {
    	level.add(lvl.getLabel());
    }
    level.select(0);
    
    return container;
	}
	
	@Override
  protected void buttonPressed(int buttonId) {
	  sLogClass=logClass.getText();
	  lvl=LogLevel.fromString(level.getText());
	  super.buttonPressed(buttonId);
  }

	public String getLogClass() {
		return sLogClass;
	}
	
	public LogLevel getLogLevel() {
		return lvl;
	}
	
}
