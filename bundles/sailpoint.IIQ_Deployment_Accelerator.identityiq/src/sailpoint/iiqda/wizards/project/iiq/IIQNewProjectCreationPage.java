package sailpoint.iiqda.wizards.project.iiq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;
import sailpoint.iiqda.wizards.project.iiq.NewIIQProjectWizard.CreationType;

public class IIQNewProjectCreationPage extends WizardNewProjectCreationPage implements Listener {

  private static final boolean DEBUG_WIZARDS = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Wizards"));

  public static final String pageName="New IdentityIQ Project";

  private Text tIiqurl;
  private Text tIiqUsername;
  private Text tIiqPassword;
  private Text tDebugTransport;
  private Text tDebugPort;

  private TabFolder js;

  private Object remote;

  private Object fromProject;

  private Object fileSystem;

  private Combo projectCombo;

  private List<FileSelector> fileSelectors;

	private Button btn;

	private String debug_transport;
	private int debug_port;
	
  private Map<String,Map<String,IPath>> projectLocations;
  
  private List<String> requiredJars;

  // This page just subclasses WizardNewProjectCreationPage
  // In order to get the new Project name

  public IIQNewProjectCreationPage(List<String> requiredJars) {

    super(pageName);
    setPageComplete(false);
    projectLocations=new HashMap<String, Map<String,IPath>>();
    this.requiredJars=requiredJars;

  }

  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    if (DEBUG_WIZARDS) IIQPlugin.logDebug("parent="+Integer.toHexString(parent.hashCode()));

    Composite thecontrol = (Composite)getControl();
    Composite projectGroup = new Composite(thecontrol, SWT.NONE);
    if (DEBUG_WIZARDS) IIQPlugin.logDebug("projectGroup="+Integer.toHexString(projectGroup.hashCode()));

    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    projectGroup.setLayout(layout);
    
    GridData gridData = new GridData();
    gridData.horizontalAlignment=GridData.FILL;
    gridData.verticalAlignment=GridData.FILL;
    gridData.grabExcessVerticalSpace=true;    
    projectGroup.setLayoutData(gridData);

    Label l=new Label(projectGroup, SWT.FILL);
    l.setText("This should be the URL including the deployed name e.g. http://localhost:8080/identityiq");			
    gridData = new GridData(GridData.VERTICAL_ALIGN_END);
    gridData.horizontalSpan = 2;
    gridData.horizontalAlignment = GridData.FILL;
    l.setLayoutData(gridData);

    l=new Label(projectGroup, SWT.NONE);
    l.setText("IIQ URL* :");			
    tIiqurl=new Text(projectGroup, SWT.BORDER);
    tIiqurl.setText("http://localhost:8080/identityiq");
    gridData = new GridData(GridData.VERTICAL_ALIGN_END);
    gridData.horizontalAlignment = GridData.FILL;
    tIiqurl.setLayoutData(gridData);
    tIiqurl.addListener(SWT.CHANGED, this);

    l=new Label(projectGroup, SWT.NONE);
    l.setText("Username* :");			
    tIiqUsername=new Text(projectGroup, SWT.BORDER);
    tIiqUsername.setText("spadmin");
    gridData = new GridData(GridData.VERTICAL_ALIGN_END);
    gridData.horizontalAlignment = GridData.FILL;
    tIiqUsername.setLayoutData(gridData);
    tIiqUsername.addListener(SWT.CHANGED, this);


    l=new Label(projectGroup, SWT.NONE);
    l.setText("Password* :");			
    tIiqPassword=new Text(projectGroup, SWT.BORDER);
    tIiqPassword.setText("admin");
    gridData = new GridData(GridData.VERTICAL_ALIGN_END);
    gridData.horizontalAlignment = GridData.FILL;
    tIiqPassword.setLayoutData(gridData);
    tIiqPassword.addListener(SWT.CHANGED, this);

    l=new Label(projectGroup, SWT.NONE);
    l.setText("Debug Transport Name");
    tDebugTransport=new Text(projectGroup, SWT.BORDER);
    tDebugTransport.setText("dt_socket");
    gridData = new GridData(GridData.VERTICAL_ALIGN_END);
    gridData.horizontalAlignment = GridData.FILL;
    tIiqPassword.setLayoutData(gridData);
    
    l=new Label(projectGroup, SWT.NONE);
    l.setText("Debug Port");
    tDebugPort=new Text(projectGroup, SWT.BORDER);
    tDebugPort.setText("8000");
    gridData = new GridData(GridData.VERTICAL_ALIGN_END);
    gridData.horizontalAlignment = GridData.FILL;
    tIiqPassword.setLayoutData(gridData);
    tIiqPassword.addListener(SWT.CHANGED, this);
    
    l=new Label(projectGroup, SWT.FILL);
    l.setText("Select a method to retrieve required JAR files");			
    gridData = new GridData(GridData.VERTICAL_ALIGN_END);
    gridData.horizontalSpan = 2;
    gridData.horizontalAlignment = GridData.FILL;
    l.setLayoutData(gridData);

    js=jarSelector(projectGroup);

		btn = new Button(projectGroup, SWT.CHECK);
		btn.setText("Use SSB Template");
		btn.setSelection(IIQPlugin.getDefault().getBooleanPreference(IIQPreferenceConstants.P_USE_SSB_TEMPLATE));

    //setControl(projectGroup);
    new Label(projectGroup, SWT.NONE);
    
  }

  public boolean copySSB() {
    return btn.getSelection();
  }

  public void handleEvent(Event event) {
    setPageComplete(validatePage());
  }

  @Override
  public boolean validatePage() {
    // if any of these is false, we can return straight away
    boolean ret=super.validatePage();
    if(!ret) return false;
    ret=tIiqurl.getText().length()>0 &&
        tIiqUsername.getText().length()>0 &&
        tIiqPassword.getText().length()>0;
        if(!ret) return false;
        TabItem itm=js.getItem(js.getSelectionIndex());
        String sPort=tDebugPort.getText();
        try {
          Integer.parseInt(sPort);
        } catch (Exception e) {
          return false; // something wrong creating integer from string
        }
        if(itm==remote) {
          // remote - can always finish
          return true;
        } else if (itm==fromProject) {
          // check if a project is selected
          // if the combo is disabled, we didn't find any projects
          // if it's enabled, we found at least one (and one will be selected)
          return projectCombo.isEnabled();
        } else if (itm==fileSystem) {
          // check if files are selected
        	boolean allSelected=true;
        	for (FileSelector f: fileSelectors) {
        		if (f.getSelectedFile()==null) {
        			allSelected=false;
        			break;
        		}
        	}
          return allSelected;
        }
        return false;

  }

  public String getURL() {
    if(tIiqurl==null) return null;
    return tIiqurl.getText();
  }

  public String getUsername() {
    if(tIiqUsername==null) return null;
    return tIiqUsername.getText();
  }

  public String getPassword() {
    if(tIiqPassword==null) return null;
    return tIiqPassword.getText();
  }

  private TabFolder jarSelector(Composite parent) {
    TabFolder tf=new TabFolder(parent, SWT.NONE);

    remote=remoteJarSelectorTab(tf);
    fromProject=projectJarSelectorTab(tf);
    fileSystem=filesystemJarSelectorTab(tf);

    GridData gridData=new GridData();
    gridData.horizontalAlignment=GridData.FILL;
    gridData.verticalAlignment=GridData.FILL;
    gridData.grabExcessVerticalSpace=true;
    gridData.horizontalSpan = 2;
    gridData.heightHint=85;
    tf.setLayoutData(gridData);
    tf.addSelectionListener(new TabFolderSelectionHandler(this));

    return tf;

  }

  private TabItem remoteJarSelectorTab(TabFolder tf) {
    TabItem itm=new TabItem(tf, SWT.NONE);
    itm.setText("Remote Location");
    Composite container = new Composite(tf, SWT.NONE);
    GridLayout layout = new GridLayout(1, true);
    container.setLayout(layout);
    Label l=new Label(container, SWT.NONE);
    l.setText("No further configuration required");

    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.CENTER;
    l.setLayoutData(gridData);
    itm.setControl(container);
    return itm;
  }

  private TabItem projectJarSelectorTab(TabFolder tf) {
    TabItem itm=new TabItem(tf, SWT.NONE);
    itm.setText("Existing Project");
    Composite container = new Composite(tf, SWT.BORDER);
    GridLayout layout = new GridLayout(1, true);
    container.setLayout(layout);
    projectCombo = new Combo(container, SWT.NONE);

    findMatchingProjects(projectCombo);


    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL_HORIZONTAL;
    gridData.grabExcessHorizontalSpace = true;
    projectCombo.setLayoutData(gridData);
    if(projectCombo.getItemCount()>0) {
      projectCombo.select(0);
    }
    itm.setControl(container);
    return itm;
  }

  private TabItem filesystemJarSelectorTab(TabFolder tf) {
    TabItem itm=new TabItem(tf, SWT.NONE);
    itm.setText("Local Folder");
    Composite container = new Composite(tf, SWT.NONE);
    if (DEBUG_WIZARDS) IIQPlugin.logDebug("fs: container="+Integer.toHexString(container.hashCode()));
    GridLayout layout = new GridLayout();
    layout.numColumns=1;
    layout.verticalSpacing=1;
    layout.marginHeight=1;
    layout.marginWidth=1;
    container.setLayout(layout);

    fileSelectors=new ArrayList<FileSelector>();
    
    for (String file: requiredJars) {
    	FileSelector jarSelector = new FileSelector(container, SWT.NONE, file, this);
    	GridData gridData = new GridData();
    	gridData.horizontalAlignment = GridData.FILL;
    	gridData.grabExcessHorizontalSpace = true;
    	jarSelector.setLayoutData(gridData);
    	fileSelectors.add(jarSelector);
    }
 
    itm.setControl(container);
    return itm;
  }

  private void findMatchingProjects(Combo c) {
    // Find a project that has identityiq.jar and commons-logging-1.1.jar
    IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

    for(IProject project: myWorkspaceRoot.getProjects()) {
      if(hasJars(project)) {
        c.add(project.getName());
      }
    }
    if(c.getItemCount()==0) {
      c.add("No projects found containing required JARs");
      c.setEnabled(false);
    }

  }

  private boolean hasJars(IProject project) {
    
    Map<String,IPath> locations=projectLocations.get(project.getName());
    if(locations==null) {
      locations=new HashMap<String,IPath>();
    }
    JarVisitor visitor = new JarVisitor(locations);
    try {
      project.accept(visitor);
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    projectLocations.put(project.getName(), locations);
    return visitor.hasJars();
  }

  private class JarVisitor implements IResourceVisitor {

    private Map<String, IPath> locations;

    public JarVisitor(Map<String,IPath> locations) {
      this.locations=locations;
    }

    @Override
    public boolean visit(IResource resource) throws CoreException {
      for (String jarName: requiredJars) {
      	if(resource.getName().equals(jarName)) {
	        locations.put(jarName, resource.getFullPath());
	        break;
	      }
      }
      return true;
    }

    public boolean hasJars() {
      for (String jarName: requiredJars) {
      	if(!locations.containsKey(jarName)) {
      		return false;
      	}
      }
      return true;
    }

  };

  private class FileSelector extends Composite implements SelectionListener {

    private IPath selectedFile;
    private Label thelbl;
    private String filename;
    private IIQNewProjectCreationPage page;

    public FileSelector(Composite parent, int style, String filename, IIQNewProjectCreationPage page) {
      super(parent, style);
      GridLayout layout = new GridLayout();
      layout.numColumns=2;
      layout.verticalSpacing=1;
      layout.marginHeight=1;
      layout.marginWidth=1;
      setLayout(layout);

      Button theButton=new Button(this, SWT.PUSH);
      theButton.setText("Select "+filename);
      theButton.addSelectionListener(this);
      thelbl=new Label(this, SWT.NONE);
      thelbl.setText("string");

      GridData gridData = new GridData();
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;

      thelbl.setLayoutData(gridData);

      this.filename=filename;
      this.selectedFile=null;
      this.page=page;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      widgetSelected(e);
    }
    @Override
    public void widgetSelected(SelectionEvent e) {

      FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
      fd.setText("Select "+filename);
      fd.setFilterNames(new String[] { filename });
      String selected = fd.open();
      if (DEBUG_WIZARDS) IIQPlugin.logDebug(selected);
      if (selected!=null) {
        thelbl.setText(selected);
        this.selectedFile=new Path(selected);
      }
      // TODO: Should we bother making this fit the normal event pattern?
      // Or is it ok to do this because it's internal and only used here?
      page.setPageComplete(page.validatePage());
      
    }

    public IPath getSelectedFile() {
      return selectedFile;
    }
  }

  private class TabFolderSelectionHandler extends SelectionAdapter {

    private IIQNewProjectCreationPage page;

    public TabFolderSelectionHandler(IIQNewProjectCreationPage page) {
      this.page=page;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      // Fire an event to the parent (this page)
      if (DEBUG_WIZARDS) IIQPlugin.logDebug("tf selector");
      page.setPageComplete(page.validatePage());
    }

  }

  public NewIIQProjectWizard.CreationType getCreationType() {
    TabItem itm=js.getItem(js.getSelectionIndex());
    if(itm==remote) {
      return CreationType.REMOTE;
    } else if (itm==fromProject) {
      return CreationType.PROJECT;      
    } else if (itm==fileSystem) {
      return CreationType.FILESYSTEM;
    }
    return null; // Some page we don't know about

  }

  public String getProjectReference() {
    return projectCombo.getItem(projectCombo.getSelectionIndex());
  }

	public List<IPath> getJarPaths() {
		List<IPath> paths=new ArrayList<IPath>();
		switch(getCreationType()) {
      case PROJECT:
      	String selectedProject=projectCombo.getItem(projectCombo.getSelectionIndex());
      	Map<String,IPath> proj=projectLocations.get(selectedProject);
      	for(Entry<String,IPath> pathEntry: proj.entrySet()) {
      		paths.add(pathEntry.getValue());
      	}
      	return paths;
      case FILESYSTEM:
      	for (FileSelector fs: fileSelectors) {
      		paths.add(fs.getSelectedFile());
      	}
      	return paths;
      case REMOTE:
        return null;
		
		}
		return null;
  }

  public String getDebugTransport() {
    if(tDebugTransport==null) return null;
    return tDebugTransport.getText();
  }

  public String getDebugPort() {
    if(tDebugPort==null) return "8000";
    try {
      Integer.parseInt(tDebugPort.getText());
    } catch (Exception e) {
      return "8000";
    }
    return tDebugPort.getText();
  }
  
}


