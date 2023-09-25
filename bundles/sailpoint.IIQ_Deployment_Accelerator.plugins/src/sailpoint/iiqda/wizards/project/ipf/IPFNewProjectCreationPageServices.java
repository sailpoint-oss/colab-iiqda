package sailpoint.iiqda.wizards.project.ipf;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.i18n.IPFStrings;
import sailpoint.iiqda.wizards.project.ServiceDescription;

public class IPFNewProjectCreationPageServices extends WizardPage {
  private DataBindingContext m_bindingContext;
	
	PageDataServices data;
	private Text txtServiceName;
	private Button btnConfigureServices;
	private Composite baseComposite;
  private ListViewer lvDefinedServices;

  private List lServices;

  private Button btnAdd;
  private Text txtClazzName;
  
  private NewIPFProjectWizard wiz;

  protected boolean javaModified;
  protected String tempJavaMethodName;
  private Text txtInterval;

  private StyledText stDescription;

	/**
	 * Create the wizard.
	 */
	public IPFNewProjectCreationPageServices(NewIPFProjectWizard wiz) {
    super("REST");
    setTitle(IPFStrings.getString("wizard.newipfproject.title"));
    setDescription(IPFStrings.getString("wizard.newipfproject.page.service"));
		data=new PageDataServices();
		this.wiz=wiz;
	}

	public java.util.List<ServiceDescription> getServiceDescriptions() {
		return data.getServiceDescriptions();
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

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
	  Composite container = new Composite(parent, SWT.NULL);
    setControl(container);
    container.setLayout(new GridLayout(1, false));
    
    btnConfigureServices = new Button(container, SWT.CHECK);
    btnConfigureServices.setText(IPFStrings.getString("IPFNewProjectCreationPageServices.btnConfigureServices.text")); //$NON-NLS-1$
    btnConfigureServices.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        recursiveSetEnabled(baseComposite, btnConfigureServices.getSelection());
      }
      
    });
    
    baseComposite = new Composite(container, SWT.BORDER);
    baseComposite.setLayout(new GridLayout(2, false));
    baseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    
    Label lblServiceName = new Label(baseComposite, SWT.NONE);
    lblServiceName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblServiceName.setText(IPFStrings.getString("IPFNewProjectCreationPageServices.lblServiceName.text")); //$NON-NLS-1$
    
    txtServiceName = new Text(baseComposite, SWT.BORDER);
    txtServiceName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblClassName = new Label(baseComposite, SWT.NONE);
    lblClassName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblClassName.setText("Class Name");
    
    txtClazzName = new Text(baseComposite, SWT.BORDER);
    txtClazzName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblInterval = new Label(baseComposite, SWT.NONE);
    lblInterval.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblInterval.setText(IPFStrings.getString("IPFNewProjectCreationPageServices.lblInterval.text"));
    
    txtInterval = new Text(baseComposite, SWT.BORDER);
    txtInterval.setText(""); //$NON-NLS-1$
    txtInterval.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblDescription = new Label(baseComposite, SWT.NONE);
    lblDescription.setText(IPFStrings.getString("IPFNewProjectCreationPageServices.lblDescription.text_1")); //$NON-NLS-1$
    
    stDescription = new StyledText(baseComposite, SWT.BORDER);
    stDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    new Label(baseComposite, SWT.NONE);
    
    btnAdd = new Button(baseComposite, SWT.NONE);
    btnAdd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    btnAdd.setText("Add");
    btnAdd.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        
        System.out.println("SelectionListener.widgetDefaultSelected:");
        
        String serviceName=txtServiceName.getText();
        String clazzName=txtClazzName.getText();
        String sInterval=txtInterval.getText();
        int iInterval=-1;
        try {
          iInterval=Integer.parseInt(sInterval);
        } catch (Exception ex) {
          // don't care why the exception; just means we don't use the value
        }
        String description=stDescription.getText();
        
        ServiceDescription sd=new ServiceDescription(serviceName, clazzName, iInterval, description);
        data.addService(sd);
        
        txtServiceName.setText("");
        txtClazzName.setText("");
        txtInterval.setText("");
        stDescription.setText("");
        btnAdd.setEnabled(false);

      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        System.out.println("SelectionListener.widgetSelected:");
        widgetDefaultSelected(e);
      }
      
    });
    
    Group grpDefinedServices = new Group(baseComposite, SWT.NONE);
    grpDefinedServices.setLayout(new GridLayout(2, false));
    grpDefinedServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
    grpDefinedServices.setText(IPFStrings.getString("IPFNewProjectCreationPageServices.grpDefinedServices.text")); //$NON-NLS-1$
    
    lvDefinedServices = new ListViewer(grpDefinedServices, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
    lServices = lvDefinedServices.getList();
    lServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    
    Button btnRemove = new Button(grpDefinedServices, SWT.NONE);
    btnRemove.setText("Remove");
    btnRemove.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] selected=lServices.getSelection();
        for (String selection: selected) {
          data.removeServiceByName(selection);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }
      
    });
    
    ModifyListener modLsnr=new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        
        // If we've modified the method or the REST Endpoint name
        // and we haven't modified the java method name manually then generate it
        if (e.getSource()==txtServiceName ) {
          if (!javaModified 
              && (txtServiceName.getText()!=null && txtServiceName.getText().length()>0)) {
            txtClazzName.setText("sailpoint.server."+CoreUtils.camelCase(txtServiceName.getText())+"Service");
          }
        }
        
        
        validateFields();
        
      }
    };
    txtServiceName.addModifyListener(modLsnr);
    txtClazzName.addModifyListener(modLsnr);
    txtInterval.addModifyListener(modLsnr);
    
    m_bindingContext = initDataBindings();
    
    btnConfigureServices.setSelection(false);
    m_bindingContext.updateModels();
    m_bindingContext.updateTargets();
    
    recursiveSetEnabled(baseComposite, false);
     
	}
	
	private void validateFields() {
	  
	  boolean canAdd=true;
	  
    String sServiceName = txtServiceName.getText();
    
    String sClazzName = txtClazzName.getText();
    canAdd=sClazzName.length()>0;
    if (!canAdd) {
      setErrorMessage("Java method name must be specified");
      btnAdd.setEnabled(false);
      return;
    }
    
    IStatus status=JavaConventions.validateJavaTypeName(sClazzName, JavaCore.VERSION_1_8, JavaCore.VERSION_1_8);
    canAdd=(status.getSeverity()==IStatus.OK);
    if (!canAdd) {
      setErrorMessage("Invalid java class name");
      btnAdd.setEnabled(false);
      return;
    }
    
    if (data.getServiceByName(sServiceName)!=null) {
      setErrorMessage("Service Name already exists");
      btnAdd.setEnabled(false);
      return;
      
    }
    
    if (data.getServiceByClass(sClazzName)!=null) {
      setErrorMessage("Java class already exists");
      btnAdd.setEnabled(false);
      return;
      
    }
    
    String sInterval=txtInterval.getText();
    if (sInterval!=null && sInterval.length()>0) {
      try {
        int iInterval=Integer.parseInt(sInterval);
      } catch (Exception e) {        
        setErrorMessage("Interval must be an integer");
        btnAdd.setEnabled(false);
        return;
      }
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
  }

  public PageDataServices getData() {
    return data;
  }
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
    IObservableMap observeMap = PojoObservables.observeMap(listContentProvider.getKnownElements(), ServiceDescription.class, "serviceName");
    lvDefinedServices.setLabelProvider(new ObservableMapLabelProvider(observeMap));
    lvDefinedServices.setContentProvider(listContentProvider);
    //
    IObservableList servicesDataObserveList = BeanProperties.list("serviceDescriptions").observe(data);
    lvDefinedServices.setInput(servicesDataObserveList);
    //
    IObservableValue observeEnabledBaseCompositeObserveWidget_1 = WidgetProperties.enabled().observe(baseComposite);
    IObservableValue observeSelectionBtnConfigureServicesObserveWidget_1 = WidgetProperties.selection().observe(btnConfigureServices);
    bindingContext.bindValue(observeEnabledBaseCompositeObserveWidget_1, observeSelectionBtnConfigureServicesObserveWidget_1, null, null);
    //
    IObservableValue observeTextTxtBaseEndpointObserveWidget_2 = WidgetProperties.text(SWT.Modify).observe(txtServiceName);
    IObservableValue textTxtJavaMethodNameObserveValue = PojoProperties.value("text").observe(txtClazzName);
    UpdateValueStrategy strategy_1 = new UpdateValueStrategy();
    strategy_1.setConverter(new PageFourAClassConverter());
    bindingContext.bindValue(observeTextTxtBaseEndpointObserveWidget_2, textTxtJavaMethodNameObserveValue, strategy_1, null);
    //
    IObservableValue observeSelectionBtnConfigureServicesObserveWidget = WidgetProperties.selection().observe(btnConfigureServices);
    IObservableValue hasServicesDataObserveValue = BeanProperties.value("hasServices").observe(data);
    bindingContext.bindValue(observeSelectionBtnConfigureServicesObserveWidget, hasServicesDataObserveValue, null, null);
    //
    return bindingContext;
  }
}
