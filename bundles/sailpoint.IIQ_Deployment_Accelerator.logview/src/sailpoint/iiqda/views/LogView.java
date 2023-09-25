package sailpoint.iiqda.views;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;

import sailpoint.iiqda.LogViewPlugin;
import sailpoint.log4j.LogLevelSetting;
import sailpoint.log4j.LogLevelSetting.LogLevel;
import sailpoint.log4j.messages.IIQDAClientQuitMessage;
import sailpoint.log4j.messages.IIQDAJoinMessage;
import sailpoint.log4j.messages.IIQDALogMessage;
import sailpoint.log4j.messages.IIQDAMessage;
import sailpoint.log4j.messages.IIQDAShutdownMessage;
import sailpoint.log4j.messages.IIQDAUpdateMessage;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class LogView extends ViewPart {

  private static final boolean DEBUG_LOGGING = "true".equalsIgnoreCase(Platform
      .getDebugOption(LogViewPlugin.PLUGIN_ID+"/debug/Logging"));
  
  
	private static final String LOG_SETTING = "LOG_SETTING";
	private static final String LOG_SERVER_ADDRESS = "LOG_SERVER_ADDRESS";
	private static final String LOG_SERVER_PORT = "LOG_SERVER_PORT";

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "sailpoint.iiqda.views.LogView";

	private static final String COLUMN_WIDTH_CLASS = "Class";
	private static final String COLUMN_WIDTH_THREAD = "Thread";
	private static final String COLUMN_WIDTH_MESSAGE = "Message|";
	private static final String COLUMN_WIDTH_LEVEL = "Level";
	private static final String COLUMN_WIDTH_DATE = "Date";

	private static final String CONNECTING_IMAGE = "connecting";
	private static final String CONNECTED_IMAGE = "connected";
	private static final String DISCONNECTED_IMAGE = "disconnected";


	private TableViewer viewer;
	private Action actConnect;
	private Action actOptions;
	private Action actClear;
	private Action doubleClickAction;

	private ArrayList<LogLevelSetting> settings;

	private LogComparator comparator;

	private IMemento memento;

	LogViewListener listenerThread;

	// For the checkbox images
	private static ImageRegistry imageRegistry = new ImageRegistry();

	private String serverAddress;
	private int serverPort;
	private boolean connected;

	/**
	 * Note: An image registry owns all of the image objects registered with it,
	 * and automatically disposes of them the SWT Display is disposed.
	 */ 
	static {
		String iconPath = "/icons/"; 
		imageRegistry.put(CONNECTING_IMAGE, ImageDescriptor.createFromFile(
				LogOptionsDialog.class, 
				iconPath + CONNECTING_IMAGE + ".png"
				)
				);
		imageRegistry.put(CONNECTED_IMAGE, ImageDescriptor.createFromFile(
				LogOptionsDialog.class, 
				iconPath + CONNECTED_IMAGE + ".png"
				)
				);
		imageRegistry.put(DISCONNECTED_IMAGE, ImageDescriptor.createFromFile(
				LogOptionsDialog.class, 
				iconPath + DISCONNECTED_IMAGE + ".png"
				)
				);	
	}

	private boolean isConnected() {
		return connected;
	}

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {

		private List<LoggingEvent> contents;

		public ViewContentProvider() {
			this.contents=new ArrayList<LoggingEvent>();
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			//		v.setInput(newInput);
		}
		public void dispose() {
		}

		public void addEntry(LoggingEvent lEvt) {
			this.contents.add(lEvt);
		}

		public Object[] getElements(Object parent) {
			//return new String[] { "One", "Two", "Three" };
			return contents.toArray(new LoggingEvent[contents.size()]);
		}
	}

	private void updateSettings(ArrayList<LogLevelSetting> settings) {
		this.settings=settings;
		if(isConnected()) {
			listenerThread.sendUpdate(settings);
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento=memento;
		settings=new ArrayList<LogLevelSetting>();
		IMemento[] logSettings=null;
		if(memento==null) {
		  serverAddress="localhost";
		  serverPort=9999;
		} else {
		  logSettings=memento.getChildren(LOG_SETTING);
  		if(logSettings!=null) {
  			for(IMemento mem: logSettings) {
  				String logClass=mem.getString("logClass");
  				boolean enabled=mem.getBoolean("enabled");
  				LogLevel level=LogLevel.fromString(mem.getString("level"));
  				settings.add(new LogLevelSetting(enabled, logClass, level));
  			}
  		}
  		serverAddress = memento.getString(LOG_SERVER_ADDRESS);
  		if(serverAddress==null) serverAddress="localhost";
  		Integer thePort = memento.getInteger(LOG_SERVER_PORT);
  		if(thePort==null) serverPort=9999;
  		else serverPort=thePort.intValue();
		}
		this.setTitleImage(imageRegistry.get(DISCONNECTED_IMAGE));
	}
	@Override
	public void saveState(IMemento memento) {
		TableColumn[] columns=viewer.getTable().getColumns();
		for(TableColumn column: columns) {
			String title=column.getText();
			int width=column.getWidth();
			memento.putInteger(title, width);
		}
		//		// remove the old log settings
		//		IMemento[] logs=memento.getChildren(LOG_SETTING);
		//		for(IMemento log: logs) {
		//			memento.remove(log);
		//		}
		for(LogLevelSetting setting: settings) {
			IMemento mem=memento.createChild(LOG_SETTING);
			mem.putString("logClass", setting.getLogClass());
			mem.putBoolean("enabled", setting.isEnabled());
			mem.putString("level", setting.getLevel().getLabel());
		}
		memento.putString(LOG_SERVER_ADDRESS, serverAddress);
		memento.putInteger(LOG_SERVER_PORT, new Integer(serverPort));

		//  IStructuredSelection ss = (IStructuredSelection) sel;
		//  StringBuffer buf = new StringBuffer();
		//  for (Iterator it = ss.iterator(); it.hasNext();) {
		//     buf.append(it.next());
		//     buf.append(',');
		//  }
		//  memento.putString(STORE_SELECTION, buf.toString());
		super.saveState(memento);
	}


	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	/**
	 * The constructor.
	 */
	public LogView() {

	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true); 
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		comparator=new LogComparator();
		viewer.setComparator(comparator);
		viewer.setInput(getViewSite());

		createLogColumn(viewer, getStoredInt(COLUMN_WIDTH_DATE, 190), "Date", new ColumnLabelProvider() {
			public String getText(Object element) {
				LoggingEvent msg=(LoggingEvent)element;
				String date = new Date(msg.getTimeStamp()).toString();
				return date;
			}
		});

		createLogColumn(viewer, getStoredInt(COLUMN_WIDTH_LEVEL, 100), "Level", new ColumnLabelProvider() {
			public String getText(Object element) {
				LoggingEvent msg=(LoggingEvent)element;
				return msg.getLevel().toString();
			}
		});

		createLogColumn(viewer, getStoredInt(COLUMN_WIDTH_MESSAGE, 400), "Message", new ColumnLabelProvider() {
			public String getText(Object element) {
				LoggingEvent msg=(LoggingEvent)element;
				String lbl = msg.getMessage().toString();
				return lbl;
			}
		});

		createLogColumn(viewer, getStoredInt(COLUMN_WIDTH_THREAD, 200), "Thread", new ColumnLabelProvider() {
			public String getText(Object element) {
				LoggingEvent msg=(LoggingEvent)element;
				String lbl = msg.getThreadName();
				return lbl;
			}
		});

		createLogColumn(viewer, getStoredInt(COLUMN_WIDTH_CLASS, 300), "Category", new ColumnLabelProvider() {
			public String getText(Object element) {
				LoggingEvent msg=(LoggingEvent)element;
				String lbl = msg.getLoggerName();
				return lbl;
			}
		});

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "sailpoint.iiqda.viewer");
		makeActions();
		setDisconnected();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private int getStoredInt(String columnName, int i) {
		if(memento==null) return i;
		Integer val=memento.getInteger(columnName);
		if(val==null) return i;
		return val.intValue();
	}

	private void createLogColumn(TableViewer viewer, int width, String columnTitle,
			ColumnLabelProvider columnLabelProvider) {
		TableViewerColumn c=new TableViewerColumn(viewer, SWT.NONE);
		TableColumn ct=c.getColumn();
		ct.setWidth(width);
		ct.setText(columnTitle);
		c.setLabelProvider(columnLabelProvider);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				LogView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actConnect);
		//manager.add(new Separator());
		manager.add(actOptions);
		manager.add(actClear);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actConnect);
		manager.add(actOptions);
		manager.add(actClear);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actConnect);
		manager.add(actOptions);
		manager.add(actClear);		
	}

	public void stopListener() {
	  
	  if (DEBUG_LOGGING) LogViewPlugin.logDebug("LogView.stopListener");
		listenerThread.terminate();

	}
	
	public void cancelListener() {

	  if (DEBUG_LOGGING) LogViewPlugin.logDebug("LogView.cancelListener");
		listenerThread.terminate();
		
	}
	
	private void startListener() {

		listenerThread=new LogViewListener(serverAddress, serverPort);
		Thread t=new Thread(listenerThread);
		t.start();
	}

	private void setConnecting() {
		// Set the title icon
		setTitleImage(imageRegistry.get(CONNECTING_IMAGE));
		// Update the menu option
		actConnect.setText("Connecting.. Click to cancel");
//		actConnect.setEnabled(false);
	}

	private void setConnected() {
		// Set the title icon
		setTitleImage(imageRegistry.get(CONNECTED_IMAGE));
		// Update the menu option
		actConnect.setText("Disconnect");
		actConnect.setToolTipText("Disconnect from the IdentityIQ Log");
		//		this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
		//				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		actConnect.setEnabled(true);
		setConnectedFlag(true);
	}

	private void setConnectedFlag(boolean b) {
		connected=b;	  
	}

	private void setDisconnecting() {
		// Update the menu option
		actConnect.setText("Disconnecting..");
		actConnect.setEnabled(false);

	}

	private void setDisconnected() {
		// Set the title icon
		setTitleImage(imageRegistry.get(DISCONNECTED_IMAGE));
		// Update the menu option
		actConnect.setText("Connect");
		actConnect.setEnabled(true);
		actConnect.setToolTipText("Connect to the IdentityIQ Log");
		setConnectedFlag(false);
	}

	private void makeActions() {

		actConnect = new ConnectionAction(this);
		actOptions = new OptionsAction();
		actClear = new ClearAction();
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				LoggingEvent evt = (LoggingEvent)((IStructuredSelection)selection).getFirstElement();
				LogEntryDialog dlg=new LogEntryDialog(viewer.getControl().getShell(), evt);
				/*Object ret=*/dlg.open();
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	WorkbenchJob revealJob = new WorkbenchJob("Reveal End of Document") {//$NON-NLS-1$
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (viewer != null && !viewer.getTable().isDisposed()) {
				int lineCount = viewer.getTable().getItemCount();	
				viewer.getTable().setTopIndex(lineCount - 1);
			}
			return Status.OK_STATUS;
		}
	};

	private class LogViewListener implements Runnable {

		private String serverAddress;
		private int port;

		private boolean terminated=false;

		Socket socket = null;
		ObjectOutputStream outToServer=null;
		ObjectInputStream inFromServer=null;
		private boolean connected;

		public LogViewListener(String serverAddress, int port) {
			this.serverAddress=serverAddress;
			this.port=port;
		}

		public void terminate() {
			try {
			  if (outToServer!=null) {
			  	outToServer.writeObject(new IIQDAClientQuitMessage());
			  }
				if (socket!=null) {
					socket.close();
				}
			} catch (IOException e) {}
			terminated=true;
		}

		public void sendUpdate(ArrayList<LogLevelSetting> settings) {
			try {
			  outToServer.reset();
				outToServer.writeObject(new IIQDAUpdateMessage(settings));
			} catch(IOException ioe) {
				// TODO: what to do when this fails?
			}
		}

		public void run() {

			while (!terminated) {
				setConnecting();
				connected = false;
				try {
					socket=new Socket(serverAddress, port);
					outToServer = new ObjectOutputStream(socket.getOutputStream());
					inFromServer = new ObjectInputStream(socket.getInputStream());
					outToServer.writeObject(new IIQDAJoinMessage(settings));
					connected=true;
					setConnected();
				} catch (IOException e1) {
				  if (DEBUG_LOGGING) LogViewPlugin.logDebug("Couldn't start LogViewListenerSocket - retrying");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
				}
				if(connected) {
					// Process all messages from server, according to the protocol.
					boolean shutdown=false;
					while (!shutdown) {
						IIQDAMessage msg;
						try {
							msg=(IIQDAMessage)inFromServer.readObject();
							if (msg instanceof IIQDALogMessage) {
								LoggingEvent lEvt=((IIQDALogMessage)msg).getMessage();
								((ViewContentProvider)viewer.getContentProvider()).addEntry(lEvt);
								PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
									public void run() {
										viewer.refresh();
										revealJob.schedule(50);
									}
								});
							} else if (msg instanceof IIQDAShutdownMessage) {
							  if (DEBUG_LOGGING) LogViewPlugin.logDebug("Shutdown");
								shutdown=true;
							}
						} catch (IOException e) {
						  if (DEBUG_LOGGING) LogViewPlugin.logDebug("Connection unexpectedly terminated");
							shutdown=true;
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				// Close stuff. Don't worry if it doesn't work, it's probably already closed
				try {
					inFromServer.close();
				} catch (Exception e) {}
				try {
					outToServer.close();
				} catch (Exception e) {}
				try {
					socket.close();
				} catch (Exception e) {}
			}
			setDisconnected();
		}
	};

	private class ConnectionAction extends Action {

		private boolean isConnected=false;
		private boolean isConnecting=false;
		
		private LogView parent;

		public ConnectionAction(LogView parentViewer) {
			this.parent=parentViewer;
			this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		}

		public void run() {
			if(!isConnected && !isConnecting ) {
			  if (DEBUG_LOGGING) LogViewPlugin.logDebug("start");
				isConnecting=true;
				startListener();
			} else if (!isConnected && isConnecting) {
			  if (DEBUG_LOGGING) LogViewPlugin.logDebug("Cancel");
				isConnecting=false;
				cancelListener();
			} else {
			  if (DEBUG_LOGGING) LogViewPlugin.logDebug("stop");
				isConnected=false;
				stopListener();
			}
		}
//		public void setConnecting(boolean connecting) {
//			this.isConnecting=connecting;
//		}
	}
	
	class OptionsAction extends Action {

		public OptionsAction() {
			this.setText("Options");
			this.setToolTipText("Configure view options");
			this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		}

		public void run() {

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					LogOptionsDialog dialog = new LogOptionsDialog(shell, settings, serverAddress, serverPort, isConnected() );
					dialog.create();
					if (dialog.open() == Window.OK) {
						updateSettings(dialog.getSettings());
						if(!isConnected()) {
							serverAddress=dialog.getServerAddress();
							serverPort=dialog.getServerPort();
						}
					} 			
				}
			});
		}

	}
	
	class ClearAction extends Action {
		
		public ClearAction() {
			this.setText("Clear");
			this.setToolTipText("Clears the log");
			this.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR));
		}
		
		public void run() {
			
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					viewer.getTable().clearAll();			
				}
			});
		}
		
	}


}