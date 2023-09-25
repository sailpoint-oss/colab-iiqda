package sailpoint.iiqda.wizards.project.ipf;

import java.io.File;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.IPFPlugin;
import sailpoint.iiqda.i18n.IPFStrings;
import sailpoint.iiqda.wizards.project.ListEntry;

public class IPFNewProjectCreationPageLibraries extends WizardPage {
	private static final String[] LIB_EXTENSIONS = new String[]{"*.jar", "*.zip"};
	private Text txtIIQLocation;
	
	PageDataLibraries data;
	private ListViewer listViewerReference;
	private ListViewer listViewerInclude;
  private List listRef;
  private List listInclude;

	/**
	 * Create the wizard.
	 */
	public IPFNewProjectCreationPageLibraries() {
    super("Full Pages");
    setTitle(IPFStrings.getString("wizard.newipfproject.title"));
    setDescription(IPFStrings.getString("wizard.newipfproject.page.libraries"));
		data=new PageDataLibraries();
	}

	public PageDataLibraries getData() {
		return data;
	}
	
	@Override
  public void setVisible(boolean visible) {
    if (visible) {
      // page is being moved into      
    } else {
      // page is being moved away from
    }
    super.setVisible(visible);
  }

  @Override
  public boolean isPageComplete() {
    // The only restriction on this page is that the IdentityIQ location must have an
    // entry of WEB-INF/lib/identityiq.jar in it.
    if (data.getIIQLocation()==null || data.getIIQLocation().length()==0) {
      setErrorMessage("IdentityIQ location must be specified");
      return false;
    }
    String loc=data.getIIQLocation();
    File f=new File(loc);
    IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(f);
    if (fileStore==null) {
      setErrorMessage("Cannot retrieve "+data.getIIQLocation());
      return false;
    }
    IFileStore iiqJar=fileStore.getChild("WEB-INF/lib/identityiq.jar");
    if(iiqJar==null || !(iiqJar.fetchInfo().exists())) {
      setErrorMessage("Cannot find identityiq.jar");
      return false;
    }
    setErrorMessage(null);
    return true;
  }
	
	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		Group grpIdentityiqLocation = new Group(container, SWT.NONE);
		grpIdentityiqLocation.setLayout(new GridLayout(2, false));
		grpIdentityiqLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpIdentityiqLocation.setText("IdentityIQ Location");
		
		txtIIQLocation = new Text(grpIdentityiqLocation, SWT.BORDER);
		txtIIQLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		String lastLoc=IPFPlugin.getDefault().getPreference(IPFPlugin.LAST_USED_IIQ_LOCATION);
		if (lastLoc!=null) {
		  data.setIIQLocation(lastLoc);
		}

		txtIIQLocation.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        System.out.println("modifyText: "+txtIIQLocation.getText());
        data.setIIQLocation(txtIIQLocation.getText());
        setPageComplete(isPageComplete());
      }
    });
		
		Button btnSearch = new Button(grpIdentityiqLocation, SWT.NONE);
		btnSearch.setText("Search");
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {			  
				DirectoryDialog fd=new DirectoryDialog(getShell(), SWT.OPEN);
				String lastLoc=IPFPlugin.getDefault().getPreference(IPFPlugin.LAST_USED_IIQ_LOCATION);
				if (lastLoc!=null) {
				  fd.setFilterPath(lastLoc);
				}
				String val=fd.open();
				if (val!=null){
					txtIIQLocation.setText(val);
					setPageComplete(isPageComplete());
				}
			}
		});
		
		Group grpLibrariesToReference = new Group(container, SWT.NONE);
		grpLibrariesToReference.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpLibrariesToReference.setText("Libraries to reference");
		grpLibrariesToReference.setLayout(new GridLayout(2, false));
		
		listViewerReference = new ListViewer(grpLibrariesToReference, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		listRef = listViewerReference.getList();
		listRef.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compReference = new Composite(grpLibrariesToReference, SWT.NONE);
		compReference.setLayout(new GridLayout(1, false));
		compReference.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		Button btnAddReference = new Button(compReference, SWT.CENTER);
		btnAddReference.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd=new FileDialog(getShell(), SWT.OPEN|SWT.MULTI);
				fd.setFilterExtensions(LIB_EXTENSIONS);
				String val=fd.open();
				if (val!=null){
				  for (String file: fd.getFileNames()) {
  					data.addReference(fd.getFilterPath()+File.separator+file);
				  }
				}
			}
		});
		btnAddReference.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddReference.setText("Add");
		
		Button btnRemoveReference = new Button(compReference, SWT.NONE);
		btnRemoveReference.setText("Remove");
		btnRemoveReference.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] selected=listRef.getSelection();
        for (String sel: selected) {
          data.removeReference(sel);
        }
      }
    });
		
		Group grpLibrariesToInclude = new Group(container, SWT.NONE);
		grpLibrariesToInclude.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpLibrariesToInclude.setText("Libraries to include");
		grpLibrariesToInclude.setLayout(new GridLayout(2, false));
		
		listViewerInclude = new ListViewer(grpLibrariesToInclude, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		listInclude = listViewerInclude.getList();
		listInclude.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compInclude = new Composite(grpLibrariesToInclude, SWT.NONE);
		compInclude.setLayout(new GridLayout(1, false));
		compInclude.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		Button btnAddInclude = new Button(compInclude, SWT.NONE);
		btnAddInclude.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddInclude.setText("Add");
		btnAddInclude.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        FileDialog fd=new FileDialog(getShell(), SWT.OPEN|SWT.MULTI);
        fd.setFilterExtensions(LIB_EXTENSIONS);
        
        String val=fd.open();
        if (val!=null){
          for (String file: fd.getFileNames()) {
            data.addInclude(fd.getFilterPath()+File.separator+file);
          }
        }
      }
    });
		
		Button btnRemoveInclude = new Button(compInclude, SWT.NONE);
		btnRemoveInclude.setText("Remove");
		btnRemoveInclude.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] selected=listInclude.getSelection();
        for (String sel: selected) {
          data.removeReference(sel);
        }
      }
    });
		initDataBindings();
	}
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    IObservableValue observeTextTxtIIQLocationObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtIIQLocation);
    IObservableValue iIQLocationDataObserveValue = BeanProperties.value("IIQLocation").observe(data);
    bindingContext.bindValue(observeTextTxtIIQLocationObserveWidget, iIQLocationDataObserveValue, null, null);
    //
    ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
    IObservableMap observeMap = BeansObservables.observeMap(listContentProvider.getKnownElements(), ListEntry.class, "value");
    listViewerReference.setLabelProvider(new ObservableMapLabelProvider(observeMap));
    listViewerReference.setContentProvider(listContentProvider);
    //
    IObservableList referencesDataObserveList = BeanProperties.list("references").observe(data);
    listViewerReference.setInput(referencesDataObserveList);
    //
    ObservableListContentProvider listContentProvider_1 = new ObservableListContentProvider();
    IObservableMap observeMap_1 = BeansObservables.observeMap(listContentProvider_1.getKnownElements(), ListEntry.class, "value");
    listViewerInclude.setLabelProvider(new ObservableMapLabelProvider(observeMap_1));
    listViewerInclude.setContentProvider(listContentProvider_1);
    //
    IObservableList includesDataObserveList = BeanProperties.list("includes").observe(data);
    listViewerInclude.setInput(includesDataObserveList);
    //
    return bindingContext;
  }
}
