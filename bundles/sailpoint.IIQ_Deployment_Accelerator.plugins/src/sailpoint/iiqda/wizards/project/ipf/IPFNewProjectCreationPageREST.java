package sailpoint.iiqda.wizards.project.ipf;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.i18n.IPFStrings;
import sailpoint.iiqda.wizards.project.RESTEndpoint;

public class IPFNewProjectCreationPageREST extends WizardPage {
	
	PageDataREST data;
	private Text txtBaseEndpoint;
  private Text txtBaseRight;
	private Text txtEndpoint;
	private Text txtJavaMethodName;
	private Combo comboReturnType;
	private Text txtSpecificRight;
	private Button btnConfigureRestEndpoints;
	private Composite baseComposite;
	private Button btnSpecificSPRight;
	

  private Combo comboMethod;
  private ListViewer lvDefinedEndpoints;

  private List lDefinedEndpoints;

  private Button btnAdd;
  private Text txtClazzName;
  
  private NewIPFProjectWizard wiz;

  private IObservableValue observeSelectionBtnSpecificSprightObserveWidget;

  protected boolean javaModified;
  protected String tempJavaMethodName;

	/**
	 * Create the wizard.
	 */
	public IPFNewProjectCreationPageREST(NewIPFProjectWizard wiz) {
    super("REST");
    setTitle(IPFStrings.getString("wizard.newipfproject.title"));
    setDescription(IPFStrings.getString("wizard.newipfproject.page.rest"));
		data=new PageDataREST();
		this.wiz=wiz;
	}

	public PageDataREST getData() {
		return data;
	}
	
  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      // page is being moved into
      
    } else {
      // page is being moved away from
      if (data.getHasREST()) {
        wiz.addRight(data.getBaseEndpointRight());
        for (RESTEndpoint ep: data.getEndpoints()) {
          if (ep.hasSpecificRight()) {
            wiz.addRight(ep.getSpRight());
          }
        }
      }
    }
    super.setVisible(visible);
  }

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
	  Composite container = new Composite(parent, SWT.NULL);
    setControl(container);
    container.setLayout(new GridLayout(1, false));
    
    btnConfigureRestEndpoints = new Button(container, SWT.CHECK);
    btnConfigureRestEndpoints.setText("Configure REST Endpoints");
    btnConfigureRestEndpoints.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        recursiveSetEnabled(baseComposite, btnConfigureRestEndpoints.getSelection());
      }
      
    });
    
    baseComposite = new Composite(container, SWT.BORDER);
    baseComposite.setLayout(new GridLayout(2, false));
    baseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    
    Label lblBaseEndpoint = new Label(baseComposite, SWT.NONE);
    lblBaseEndpoint.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblBaseEndpoint.setText("Base Endpoint");
    
    txtBaseEndpoint = new Text(baseComposite, SWT.BORDER);
    txtBaseEndpoint.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblBaseRight = new Label(baseComposite, SWT.NONE);
    lblBaseRight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblBaseRight.setText("Base SPRight");
    
    txtBaseRight = new Text(baseComposite, SWT.BORDER);
    txtBaseRight.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblClassName = new Label(baseComposite, SWT.NONE);
    lblClassName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblClassName.setText("Class Name");
    
    txtClazzName = new Text(baseComposite, SWT.BORDER);
    txtClazzName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Composite detailsComposite = new Composite(baseComposite, SWT.NONE);
    detailsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    detailsComposite.setLayout(new GridLayout(1, false));
    
    Composite row1 = new Composite(detailsComposite, SWT.NONE);
    row1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    row1.setLayout(new GridLayout(4, false));
    
    Label lblMethod = new Label(row1, SWT.NONE);
    lblMethod.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblMethod.setText("Method");
    
    comboMethod = new Combo(row1, SWT.READ_ONLY);
    String[] items=new String[] { "GET", "POST", "PUT", "DELETE" };
    comboMethod.setItems(items);
    comboMethod.setText(IPFStrings.getString("IPFNewProjectCreationPageREST.comboMethod.text")); //$NON-NLS-1$
    
    Label lblRestEndpointName = new Label(row1, SWT.NONE);
    lblRestEndpointName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblRestEndpointName.setText("REST Endpoint Name");
    
    txtEndpoint = new Text(row1, SWT.BORDER);
    txtEndpoint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Composite row2 = new Composite(detailsComposite, SWT.NONE);
    row2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    row2.setLayout(new GridLayout(2, false));
    
    Label lblJavaMethodName = new Label(row2, SWT.NONE);
    lblJavaMethodName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblJavaMethodName.setText("Java method Name");
    
    txtJavaMethodName = new Text(row2, SWT.BORDER);
    txtJavaMethodName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    // Focus listener; so we can set a flag if the user modifies the value
    txtJavaMethodName.addFocusListener(new FocusListener() {

      @Override
      public void focusLost(FocusEvent e) {
          if (tempJavaMethodName.equals(txtJavaMethodName.getText())) {
              // do nothing
          } else {
              javaModified=true;
          }
      }

      @Override
      public void focusGained(FocusEvent e) {
          tempJavaMethodName = txtJavaMethodName.getText();

      }
  });
    
    Composite row3 = new Composite(detailsComposite, SWT.NONE);
    row3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    row3.setLayout(new GridLayout(3, false));
    
    Label lblReturnType = new Label(row3, SWT.NONE);
    lblReturnType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblReturnType.setText("Return type");
    
    comboReturnType = new Combo(row3, SWT.BORDER);
    comboReturnType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    String[] retItems=new String[] { "String", "Map", "List" };
    comboReturnType.setItems(retItems);
    new Label(row3, SWT.NONE);
    
    /*Button btnSelectType = new Button(row3, SWT.NONE);
    btnSelectType.setText("Select Type");
    btnSelectType.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        
        IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
        LibraryLocation[] locations= JavaRuntime.getLibraryLocations(vmInstall);

        IJavaElement[] els=new IJavaElement[locations.length];
        for (int i=0;i<locations.length;i++) {
          LibraryLocation loc=locations[i];
          IPath p=loc.getSystemLibraryPath();
          
          IFileStore store = EFS.getLocalFileSystem().getStore(p);
          IFile f=(IFile)store.getAdapter(IFile.class);
          
          IPackageFragmentRoot root=JavaCore.createJarPackageFragmentRootFrom(f);
          els[i]=root;
        }
        
        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(els);

        
        
        
        FilteredTypesSelectionDialog dialog= new FilteredTypesSelectionDialog(getShell(), false,
          getWizard().getContainer(), scope, IJavaSearchConstants.CLASS);
        dialog.setTitle(NewWizardMessages.NewTypeWizardPage_SuperClassDialog_title);
        dialog.setMessage(NewWizardMessages.NewTypeWizardPage_SuperClassDialog_message);
        dialog.setInitialPattern("");

        if (dialog.open() == Window.OK) {
          //return (IType) dialog.getFirstResult();
        }
        //return null;
      }
      
    });*/
    
    Composite composite = new Composite(detailsComposite, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    composite.setLayout(new GridLayout(2, false));
    
    btnSpecificSPRight = new Button(composite, SWT.CHECK);
    btnSpecificSPRight.setText("Specific SPRight");
    
    txtSpecificRight = new Text(composite, SWT.BORDER);
    txtSpecificRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    btnAdd = new Button(detailsComposite, SWT.NONE);
    btnAdd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    btnAdd.setText("Add");
    btnAdd.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        
        System.out.println("SelectionListener.widgetDefaultSelected:");
        
        String method=comboMethod.getText();
        String restName=txtEndpoint.getText();
        String javaName=txtJavaMethodName.getText();
        String returnType=comboReturnType.getText();
        String spRight=null;
        boolean hasRight=btnSpecificSPRight.getSelection();
        if (hasRight) spRight=txtSpecificRight.getText();
        
        RESTEndpoint ep=new RESTEndpoint(method, restName, javaName, returnType, hasRight, spRight);
        data.addEndpoint(ep);
        
        comboMethod.setText("");
        txtEndpoint.setText("");
        txtJavaMethodName.setText("");
        comboReturnType.setText("");
        txtSpecificRight.setText("");
        txtSpecificRight.setEnabled(false); 
        observeSelectionBtnSpecificSprightObserveWidget.setValue(Boolean.FALSE);
        btnAdd.setEnabled(false);
        javaModified=false;
        tempJavaMethodName=null;

      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        System.out.println("SelectionListener.widgetSelected:");
        widgetDefaultSelected(e);
      }
      
    });
    
    Group grpDefinedEndpoint = new Group(baseComposite, SWT.NONE);
    grpDefinedEndpoint.setLayout(new GridLayout(2, false));
    grpDefinedEndpoint.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
    grpDefinedEndpoint.setText("Defined Endpoints");
    
    lvDefinedEndpoints = new ListViewer(grpDefinedEndpoint, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
    lDefinedEndpoints = lvDefinedEndpoints.getList();
    lDefinedEndpoints.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    
    Button btnRemove = new Button(grpDefinedEndpoint, SWT.NONE);
    btnRemove.setText("Remove");
    btnRemove.addListener(SWT.Selection, (event) -> {
      String[] selected=lDefinedEndpoints.getSelection();
      for (String selection: selected) {
        data.removeEndpoint(selection);
      }
    });
    
    ModifyListener modLsnr=new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        
        // If we've modified the method or the REST Endpoint name
        // and we haven't modified the java method name manually then generate it
        if (e.getSource()==comboMethod || e.getSource()==txtEndpoint) {
          if (!javaModified 
              && (comboMethod.getText()!=null && comboMethod.getText().length()>0)
              && (txtEndpoint.getText()!=null && txtEndpoint.getText().length()>0)) {
            txtJavaMethodName.setText(comboMethod.getText().toLowerCase()+
                txtEndpoint.getText().substring(0, 1).toUpperCase()+
                txtEndpoint.getText().substring(1) );
          }
        }
        
        
        validateFields();
        
      }
    };
    
    
    comboMethod.addModifyListener(modLsnr);
    txtEndpoint.addModifyListener(modLsnr);
    txtJavaMethodName.addModifyListener(modLsnr);
    comboReturnType.addModifyListener(modLsnr);
    txtSpecificRight.addModifyListener(modLsnr);
    btnSpecificSPRight.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        validateFields();
      }
    });
    
    DataBindingContext m_bindingContext = initDataBindings();
    
    btnConfigureRestEndpoints.setSelection(false);
    m_bindingContext.updateModels();
    m_bindingContext.updateTargets();
    
    recursiveSetEnabled(baseComposite, false);
    observeSelectionBtnSpecificSprightObserveWidget.setValue(Boolean.FALSE);
     
	}
	
	private void validateFields() {
	  boolean canAdd=comboMethod.getText().length()>0;
    if (!canAdd) {
      setErrorMessage("Method must be specified");
      btnAdd.setEnabled(false);
      return;
    }
    
    String sEndpoint = txtEndpoint.getText();
    canAdd=sEndpoint.length()>0;
    if (!canAdd) {
      setErrorMessage("REST Endpoint must be specified");
      btnAdd.setEnabled(false);
      return;
    }
    
    String sMethod = txtJavaMethodName.getText();
    canAdd=sMethod.length()>0;
    if (!canAdd) {
      setErrorMessage("Java method name must be specified");
      btnAdd.setEnabled(false);
      return;
    }
    
    canAdd=comboReturnType.getText().length()>0;
    if (!canAdd) {
      setErrorMessage("Return type must be specified");
      btnAdd.setEnabled(false);
      return;
    }
    
    if (btnSpecificSPRight.getSelection()) {
      System.out.println("checking specific right: value="+txtSpecificRight.getText());
      if (txtSpecificRight.getText().length()==0) {
        setErrorMessage("If specific right is enabled, a value must be specified");
        btnAdd.setEnabled(false);
        return;
      }
    }
    
    if (data.getEndpointByEndpoint(sEndpoint)!=null) {
      setErrorMessage("REST Endpoint already exists");
      btnAdd.setEnabled(false);
      return;
      
    }
    
    if (data.getEndpointByMethod(sMethod)!=null) {
      setErrorMessage("Java method already exists");
      btnAdd.setEnabled(false);
      return;
      
    }
    
    btnAdd.setEnabled(true);
    setErrorMessage(null);
	}
  
  public void recursiveSetEnabled(Control ctrl, boolean enabled) {
    if (ctrl instanceof Composite) {
       Composite comp = (Composite) ctrl;
       for (Control c : comp.getChildren())
          recursiveSetEnabled(c, enabled);
    } else {
       ctrl.setEnabled(enabled);
    }
    txtSpecificRight.setEnabled(btnSpecificSPRight.getSelection());
  }
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    observeSelectionBtnSpecificSprightObserveWidget = WidgetProperties.selection().observe(btnSpecificSPRight);
    IObservableValue observeEnabledTxtSpecificRightObserveWidget = WidgetProperties.enabled().observe(txtSpecificRight);
    bindingContext.bindValue(observeSelectionBtnSpecificSprightObserveWidget, observeEnabledTxtSpecificRightObserveWidget, null, null);
    //
    IObservableValue observeTextTxtBaseEndpointObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtBaseEndpoint);
    IObservableValue baseEndpointDataObserveValue = BeanProperties.value("baseEndpoint").observe(data);
    bindingContext.bindValue(observeTextTxtBaseEndpointObserveWidget, baseEndpointDataObserveValue, null, null);
    //
    IObservableValue observeTextTxtBaseRightObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtBaseRight);
    IObservableValue baseEndpointRightDataObserveValue = BeanProperties.value("baseEndpointRight").observe(data);
    bindingContext.bindValue(observeTextTxtBaseRightObserveWidget, baseEndpointRightDataObserveValue, null, null);
    //
    IObservableValue observeTextTxtBaseEndpointObserveWidget_1 = WidgetProperties.text(SWT.Modify).observe(txtBaseEndpoint);
    IObservableValue textTxtBaseRightObserveValue = PojoProperties.value("text").observe(txtBaseRight);
    UpdateValueStrategy strategy = new UpdateValueStrategy();
    strategy.setConverter(new Page5RightConverter());
    bindingContext.bindValue(observeTextTxtBaseEndpointObserveWidget_1, textTxtBaseRightObserveValue, strategy, null);
    //
    ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
    IObservableMap observeMap = BeansObservables.observeMap(listContentProvider.getKnownElements(), RESTEndpoint.class, "endpointName");
    lvDefinedEndpoints.setLabelProvider(new ObservableMapLabelProvider(observeMap));
    lvDefinedEndpoints.setContentProvider(listContentProvider);
    //
    IObservableList endpointsDataObserveList = BeanProperties.list("endpoints").observe(data);
    lvDefinedEndpoints.setInput(endpointsDataObserveList);
    //
    IObservableValue observeEnabledBaseCompositeObserveWidget_1 = WidgetProperties.enabled().observe(baseComposite);
    IObservableValue observeSelectionBtnConfigureRestEndpointsObserveWidget_1 = WidgetProperties.selection().observe(btnConfigureRestEndpoints);
    bindingContext.bindValue(observeEnabledBaseCompositeObserveWidget_1, observeSelectionBtnConfigureRestEndpointsObserveWidget_1, null, null);
    //
    IObservableValue observeTextTxtClazzNameObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtClazzName);
    IObservableValue baseClazzNameDataObserveValue = BeanProperties.value("baseClazzName").observe(data);
    bindingContext.bindValue(observeTextTxtClazzNameObserveWidget, baseClazzNameDataObserveValue, null, null);
    //
    IObservableValue observeTextTxtBaseEndpointObserveWidget_2 = WidgetProperties.text(SWT.Modify).observe(txtBaseEndpoint);
    IObservableValue textTxtJavaMethodNameObserveValue = PojoProperties.value("text").observe(txtClazzName);
    UpdateValueStrategy strategy_1 = new UpdateValueStrategy();
    strategy_1.setConverter(new PageFourClassConverter());
    bindingContext.bindValue(observeTextTxtBaseEndpointObserveWidget_2, textTxtJavaMethodNameObserveValue, strategy_1, null);
    //
    IObservableValue observeSelectionBtnConfigureRestEndpointsObserveWidget = WidgetProperties.selection().observe(btnConfigureRestEndpoints);
    IObservableValue hasRESTDataObserveValue = BeanProperties.value("hasREST").observe(data);
    bindingContext.bindValue(observeSelectionBtnConfigureRestEndpointsObserveWidget, hasRESTDataObserveValue, null, null);
    //
    
    return bindingContext;
  }
}
