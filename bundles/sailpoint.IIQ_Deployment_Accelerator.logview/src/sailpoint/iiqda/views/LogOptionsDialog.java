package sailpoint.iiqda.views;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.LogViewPlugin;
import sailpoint.iiqda.core.CorePlugin;
import sailpoint.log4j.LogLevelSetting;
import sailpoint.log4j.LogLevelSetting.LogLevel;


public class LogOptionsDialog extends Dialog {

  private static final boolean DEBUG_LOGGING = "true".equalsIgnoreCase(Platform
      .getDebugOption(CorePlugin.PLUGIN_ID+"/debug/Logging"));
  
	protected Object result;
	protected Shell shell;

	private LogOptionsList taskList;
	private Table table;
	private TableViewer tableViewer;
	private Text tAddress;
	private Text tPort;

	private String serverAddress;
	private int serverPort;
	private boolean isConnected;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param isConnected 
	 * @param serverPort 
	 * @param serverAddress 
	 * @param style
	 */
	public LogOptionsDialog(Shell parent, ArrayList<LogLevelSetting> settings, String serverAddress, int serverPort, boolean isConnected) {
		super(parent);
	  if(DEBUG_LOGGING) {
      LogViewPlugin.logDebug("Log Options Dialog");
    }
	  this.shell=parent;
		//		setText("Logging options");
		this.taskList=new LogOptionsList(settings);
		this.serverAddress=serverAddress;
		this.serverPort=serverPort;
		this.isConnected=isConnected;
	}

	public List<String> getColumnNames() {
		List<String> l=new ArrayList<String>();
		l.add("Enabled");
		l.add("Log Class");
		l.add("Log Level");
		return l;
	}

	public ArrayList<LogLevelSetting> getSettings() {
		return taskList.getLevelSettings();
	}

	public String getServerAddress() {
		return serverAddress;
	}
	
	public int getServerPort() {
	  return serverPort;
  }
	
	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Logging Options");
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite container = (Composite) super.createDialogArea(parent);
		
		Composite server = new Composite(container, SWT.NONE);
		server.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout layout=new GridLayout(4, false);
		server.setLayout(layout);
		Label lbl=new Label(server, SWT.NONE);
		lbl.setText("Address");
		tAddress=new Text(server, SWT.BORDER);
		tAddress.setText(serverAddress);
		tAddress.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				serverAddress=tAddress.getText();
			}
		});
		GridData gd=new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace=true;
		tAddress.setLayoutData(gd);
		
		lbl=new Label(server, SWT.NONE);
		lbl.setText("Port");
		tPort=new Text(server, SWT.BORDER);
		tPort.setText(Integer.toString(serverPort));
		tPort.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					serverPort=Integer.parseInt(tPort.getText());
				} catch (NumberFormatException nfe) {}
			}
		});
		gd=new GridData();
		gd.widthHint=50;
		tPort.setLayoutData(gd);
	
		if(isConnected) {
			tAddress.setEnabled(false);
			tPort.setEnabled(false);
		}
		
		
		Button btn = new Button(container, SWT.NONE);
		btn.setText("Add Log");
		btn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				AddLogDialog dlg=new AddLogDialog(getShell());
				int result=dlg.open();
				if(result==Window.OK) {
					LogLevelSetting setting=new LogLevelSetting(true, dlg.getLogClass(), dlg.getLogLevel());
					taskList.add(setting);
				}	      
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);      
			}

		});

		table = new Table(container, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd_table = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_table.widthHint = 417;
		gd_table.heightHint = 172;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);


		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText("Enabled");
		column.setWidth(50);

		column = new TableColumn(table, SWT.NONE);
		column.setText("Log Class");
		column.setWidth(288);

		column = new TableColumn(table, SWT.NONE);
		column.setText("Log Level");
		column.setWidth(176);

		//		for(LogLevelSetting setting: taskList.getTasks()) {
		//			addLogSetting(table, setting);
		//		}

		tableViewer = new TableViewer(table);
		tableViewer.setColumnProperties( new String[] {"Enabled","Log Class","Log Level"});
		// Create the cell editors
		CellEditor[] editors = new CellEditor[3];
		editors[0]=new CheckboxCellEditor(table);
		editors[1]=new TextCellEditor(table);
		editors[2]=new ComboBoxCellEditor(table, LogLevel.labels());
		//EditorDeactivator ed=new EditorDeactivator((ComboBoxCellEditor)editors[2]);

		tableViewer.setCellEditors(editors);
		tableViewer.setLabelProvider(new LogListLabelProvider());
		tableViewer.setCellModifier(new LogListCellModifier(this));
		tableViewer.setContentProvider(new LogOptionsContentProvider());

		tableViewer.setInput(taskList);

		MenuManager manager = new MenuManager();
		tableViewer.getControl().setMenu(manager.createContextMenu(tableViewer.getControl()));

		manager.add(new Action("delete", null) {
			@Override
			public void run() {
				// get the current selection of the tableviewer
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				@SuppressWarnings("unchecked")
				Iterator<LogLevelSetting> iter=selection.iterator();
				while(iter.hasNext()) {
					LogLevelSetting lvs=iter.next();
					taskList.remove(lvs);
				}
			}
		});

		return container;

	}

	//	private void addLogSetting(Table table, LogLevelSetting setting) {
	//		TableItem itm=new TableItem(table, SWT.NONE);
	//		TableEditor editor = new TableEditor(table);
	//
	//		Button button = new Button(table, SWT.CHECK);
	//		editor.setEditor(button, itm, 0);
	//
	//		Text text=new Text(table, SWT.NONE);
	//		text.setText(setting.getLogClass());
	//
	//		Combo combo = new Combo(table, SWT.NONE);
	//		combo.setText(setting.getLevel().getLabel());
	//		combo.add("Error");
	//		combo.add("Warn");
	//		combo.add("Info");
	//		combo.add("Debug");
	//		combo.add("Trace");
	//	}

	public void updateTask(LogLevelSetting setting) {
		//tableViewer.update(setting,  null);
		taskList.update(setting);
	}

	class LogOptionsContentProvider implements IStructuredContentProvider {

		// Return the tasks as an array of Objects
		public Object[] getElements(Object parent) {
			return taskList.getLevelSettings().toArray();
		}

		/* (non-Javadoc)
		 * @see ITaskListViewer#addTask(ExampleTask)
		 */
		public void addTask(LogLevelSetting task) {
			tableViewer.add(task);
		}

		/* (non-Javadoc)
		 * @see ITaskListViewer#removeTask(ExampleTask)
		 */
		public void removeTask(LogLevelSetting task) {
			tableViewer.remove(task);			
		}

		/* (non-Javadoc)
		 * @see ITaskListViewer#updateTask(ExampleTask)
		 */
		public void updateTask(LogOptionsList task) {
			tableViewer.update(task, null);	
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class LogOptionsList {

		private ArrayList<LogLevelSetting> settings;

		public LogOptionsList(ArrayList<LogLevelSetting> settings) {
			this.settings=settings;
		}

		public void add(LogLevelSetting setting) {
			settings.add(setting);
			tableViewer.add(setting);
		}

		public void remove(LogLevelSetting setting) {
			settings.remove(setting);
			tableViewer.remove(setting);
		}

		public void update(LogLevelSetting setting) {
			tableViewer.update(setting, null);
		}

		public ArrayList<LogLevelSetting> getLevelSettings() {
			return settings;
		}

	}

//	private class EditorDeactivator {
//
//		final ComboBoxCellEditor ce;
//
//		public EditorDeactivator(final ComboBoxCellEditor ce) {
//			this.ce=ce;
//
//			Control control = ce.getControl();
//			CCombo c = (CCombo) control;
//			c.addSelectionListener(new SelectionListener() {
//				public void widgetDefaultSelected(SelectionEvent e) { }
//				public void widgetSelected(SelectionEvent e) {
//					ce.deactivate();
//				}
//			});
//		}
//	}
}
