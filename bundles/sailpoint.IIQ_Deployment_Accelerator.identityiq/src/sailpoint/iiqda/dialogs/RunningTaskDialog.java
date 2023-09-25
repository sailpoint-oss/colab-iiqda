package sailpoint.iiqda.dialogs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;

public class RunningTaskDialog extends Dialog {

	protected Object result;
	private Table tblAttributes;
	private String taskId;

	private IIQRESTClient client;
	private Label tTaskName;
	private Label tDescription;
	private Label tStatus;
	private Label tProgress;
	private Label tStarted;
	private Label tCompleted;
	private Label tLastUpdate;
	
	private Thread t;
	private boolean stopped=false;
	private Composite composite;
	private List listErrors;

	/**
	 * Create the dialog.
	 * @param taskId 
	 * @param client 
	 * @param parent
	 * @param style
	 */


	private class UpdateThread extends Thread {
		
		private String taskId;
		
		public UpdateThread(String taskId) {
			this.taskId=taskId;
		}
		
		public void run() {
			final Map<String,Object> tr=new HashMap<String,Object>();
			while( !stopped && !(composite!=null && composite.isDisposed()) ) {
				try {
					// Doing it this way allows us to access tr from the anonymous class
					Map<String,Object> tr2=client.getTaskResult(taskId);
					tr.clear();
					tr.putAll(tr2);
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						
						private void setDateField(Label l, String key) {
							Object ovalue=tr.get(key);
							System.out.println("field "+key+"="+(ovalue==null?"null":ovalue.getClass().getName()));
							Long value=(Long)ovalue;
							if(value!=null) {
								l.setText(value.toString());
							}
						}
						private void setField(Label l, String key) {
							setTextField(l, (String)tr.get(key));
						}
						private void setTextField(Label l, String value) {
							if(value!=null) {
								l.setText(value);
							}
						}
            public void run() {
            	setField(tTaskName,"name");
            	setField(tDescription,"description");
            	setField(tStatus,"status");
            	setField(tProgress,"progress");
            	setField(tStarted,"started");
            	setField(tCompleted,"completed");
            	setTextField(tLastUpdate, new Date().toString());
            	Map<String,String> attributes=(Map<String,String>)tr.get("attributes");
            	java.util.List<String> errors=(java.util.List<String>)tr.get("errors");
            	boolean repack=false;
            	if(errors!=null) {
            		listErrors.setVisible(true);
            		listErrors.removeAll();
            		for (String error: errors) {
            			listErrors.add(error);
            		}
            		System.out.println("list: repack");
            		repack=true;
            	}
            	if (attributes!=null) {
            		tblAttributes.removeAll();
            		for (String key: attributes.keySet()) {
            			String value=attributes.get(key);
            			TableItem itm=new TableItem(tblAttributes, SWT.NONE);
            			itm.setText(new String[] {key, value});
            		}
            		tblAttributes.layout();
            		repack=true;
            	}
            	if (repack) {
            		composite.getShell().pack(true);
            	}
            	composite.layout();
            }
          });
					try {
						sleep(1000);
					} catch (InterruptedException ie) {}
				} catch (ConnectionException ce) {
					System.out.println("ce: "+ce);
					stopped=true;
				}
				if(!stopped) {
					Object status = tr.get("status");
          stopped=(status!=null && !status.equals("pending.."));
				}
			}
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {						
					tProgress.setText("Completed");
					Button cancel = getButton(IDialogConstants.CANCEL_ID);
					cancel.setEnabled(false);
					Button ok = getButton(IDialogConstants.OK_ID);
					ok.setText("OK");
//					setButtonLayoutData(ok);
				}
			});
		}
	}

	public RunningTaskDialog(Shell parent, IIQRESTClient client, String taskId) {
		super(parent);
		this.client=client;
		this.taskId=taskId;
	}

	@Override
  protected void createButtonsForButtonBar(Composite parent) {
   super.createButtonsForButtonBar(parent);

   Button ok = getButton(IDialogConstants.OK_ID);
   ok.setText("Run in Background");
   setButtonLayoutData(ok);

   Button cancel = getButton(IDialogConstants.CANCEL_ID);
   cancel.setText("Request Termination");
   setButtonLayoutData(cancel);
}
  @Override
	public int open() {
		t=new UpdateThread(taskId);
		t.start();
		return super.open();
	}

  @Override
  protected Control createDialogArea(Composite parent) {

    composite = (Composite) super.createDialogArea(parent);
    
		composite.setLayout(new GridLayout(2, false));

		Label lblTaskName = new Label(composite, SWT.NONE);
		lblTaskName.setText("Task Name");

		tTaskName = new Label(composite, SWT.NONE);
		tTaskName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		Label lblDescription = new Label(composite, SWT.NONE);
		lblDescription.setText("Description");

		tDescription = new Label(composite, SWT.NONE);

		Label lblStatus = new Label(composite, SWT.NONE);
		lblStatus.setText("Status");

		tStatus = new Label(composite, SWT.NONE);

		Label lblProgress = new Label(composite, SWT.NONE);
		lblProgress.setText("Progress");

		tProgress = new Label(composite, SWT.NONE);

		Label lblStarted = new Label(composite, SWT.NONE);
		lblStarted.setText("Started");

		tStarted = new Label(composite, SWT.NONE);

		Label lblCompleted = new Label(composite, SWT.NONE);
		lblCompleted.setText("Completed");

		tCompleted = new Label(composite, SWT.NONE);

		Label lblLastUpdate = new Label(composite, SWT.NONE);
		lblLastUpdate.setText("Last Updated");
		
		tLastUpdate = new Label(composite, SWT.NONE);
		
		listErrors = new List(composite, SWT.BORDER);
		listErrors.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		listErrors.setVisible(false);
		
		Label lblAttributes = new Label(composite, SWT.NONE);
		lblAttributes.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblAttributes.setText("Attributes");

		tblAttributes = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		tblAttributes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tblAttributes.setHeaderVisible(true);
		tblAttributes.setLinesVisible(true);

		TableColumn tblclmnName = new TableColumn(tblAttributes, SWT.NONE);
		tblclmnName.setWidth(100);
		tblclmnName.setText("Name");

		TableColumn tblclmnValue = new TableColumn(tblAttributes, SWT.NONE);
		tblclmnValue.setWidth(266);
		tblclmnValue.setText("Value");

		return composite;
		
	}
  
  @Override
  protected void okPressed() {

    stopped=true;
    super.okPressed();
    
  }

	@Override
  protected void cancelPressed() {
	  System.out.println("RunningTaskDialog.cancelPressed");
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {						
				Button cancel = getButton(IDialogConstants.CANCEL_ID);
				cancel.setEnabled(false);
//				setButtonLayoutData(ok);
			}
		});
		try {
	  	client.terminateTask(taskId);
	  } catch (ConnectionException ce) {
	  	// Do something here
	  }
  }
  
  
}
