package sailpoint.iiqda.views;

import java.util.Date;

import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LogEntryDialog extends Dialog {

	private LoggingEvent event;

	/**
	 * Create the dialog.
	 * @param parentShell
	 * @param evt 
	 */
	public LogEntryDialog(Shell parentShell, LoggingEvent evt) {
		super(parentShell);
		this.event=evt;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 2;
		
		Label lblDate = new Label(container, SWT.NONE);
		lblDate.setText("Date");
		
		Label valueDate = new Label(container, SWT.NONE);
		GridData gd_valueDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_valueDate.widthHint = 371;
		valueDate.setLayoutData(gd_valueDate);
		valueDate.setText(new Date(event.getTimeStamp()).toString());
		
		Label lblSeverity = new Label(container, SWT.NONE);
		lblSeverity.setText("Severity");
		
		Label valueSeverity = new Label(container, SWT.NONE);
		valueSeverity.setText(event.getLevel().toString());
		
		Label lblCategory = new Label(container, SWT.NONE);
		lblCategory.setText("Category");
		
		Label valueCategory = new Label(container, SWT.NONE);
		valueCategory.setText(event.getLoggerName());
		
		Label lblMessage = new Label(container, SWT.NONE);
		lblMessage.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblMessage.setText("Message");
		
		Text valueMessage = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		GridData gd_valueMessage = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_valueMessage.widthHint = 357;
		gd_valueMessage.heightHint = 165;
		valueMessage.setLayoutData(gd_valueMessage);
		valueMessage.setText(event.getMessage().toString());
		
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
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

}
