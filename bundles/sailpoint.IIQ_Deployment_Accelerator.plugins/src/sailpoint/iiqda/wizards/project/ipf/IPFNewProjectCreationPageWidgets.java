package sailpoint.iiqda.wizards.project.ipf;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoObservables;
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
import sailpoint.iiqda.wizards.project.WidgetDescription;

public class IPFNewProjectCreationPageWidgets extends WizardPage {
  private DataBindingContext m_bindingContext;
	
	PageDataWidgets data;
	private Text txtWidgetName;
	private Button btnConfigureWidgets;
	private Composite baseComposite;
  private ListViewer lvDefinedWidgets;

  private List lServices;

  private Button btnAdd;
  private Text txtWidgetDescription;
  
  private NewIPFProjectWizard wiz;

  protected boolean javaModified;
  protected String tempJavaMethodName;

	/**
	 * Create the wizard.
	 */
	public IPFNewProjectCreationPageWidgets(NewIPFProjectWizard wiz) {
    super("REST");
    setTitle(IPFStrings.getString("wizard.newipfproject.title"));
    setDescription(IPFStrings.getString("wizard.newipfproject.page.widgets"));
		data=new PageDataWidgets();
		this.wiz=wiz;
	}

	public java.util.List<WidgetDescription> getWidgetDescriptions() {
		return data.getWidgetDescriptions();
	}
	
  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      // page is being moved into
      
    } else {
      // page is being moved away from
      if (data.getHasWidgets()) {
        for (WidgetDescription ep: data.getWidgetDescriptions()) {
          wiz.addRight(ep.getWidgetName()+"Right");
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
    
    btnConfigureWidgets = new Button(container, SWT.CHECK);
    btnConfigureWidgets.setText(IPFStrings.getString("IPFNewProjectCreationPageWidgets.btnConfigureWidgets.text")); //$NON-NLS-1$
    btnConfigureWidgets.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        widgetDefaultSelected(e);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        recursiveSetEnabled(baseComposite, btnConfigureWidgets.getSelection());
      }
      
    });
    
    baseComposite = new Composite(container, SWT.BORDER);
    baseComposite.setLayout(new GridLayout(2, false));
    baseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    
    Label lblWidgetName = new Label(baseComposite, SWT.NONE);
    lblWidgetName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblWidgetName.setText(IPFStrings.getString("IPFNewProjectCreationPageWidgets.lblWidgetName.text")); //$NON-NLS-1$
    
    txtWidgetName = new Text(baseComposite, SWT.BORDER);
    txtWidgetName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label lblWidgetDescription = new Label(baseComposite, SWT.NONE);
    lblWidgetDescription.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblWidgetDescription.setText(IPFStrings.getString("IPFNewProjectCreationPageWidgets.lblWidgetDescription.text")); //$NON-NLS-1$
    
    txtWidgetDescription = new Text(baseComposite, SWT.BORDER);
    txtWidgetDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    new Label(baseComposite, SWT.NONE);
    
    btnAdd = new Button(baseComposite, SWT.NONE);
    btnAdd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    btnAdd.setText("Add");
    btnAdd.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        
        System.out.println("SelectionListener.widgetDefaultSelected:");
        
        String widgetName=txtWidgetName.getText();
        String widgetDescription=txtWidgetDescription.getText();
        
        WidgetDescription sd=new WidgetDescription(widgetName, widgetDescription);
        data.addService(sd);
        
        txtWidgetName.setText("");
        txtWidgetDescription.setText("");
        btnAdd.setEnabled(false);

      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        System.out.println("SelectionListener.widgetSelected:");
        widgetDefaultSelected(e);
      }
      
    });
    
    Group grpDefinedWidgets = new Group(baseComposite, SWT.NONE);
    grpDefinedWidgets.setLayout(new GridLayout(2, false));
    grpDefinedWidgets.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
    grpDefinedWidgets.setText(IPFStrings.getString("IPFNewProjectCreationPageWidgets.grpDefinedWidgets.text")); //$NON-NLS-1$
    
    lvDefinedWidgets = new ListViewer(grpDefinedWidgets, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
    lServices = lvDefinedWidgets.getList();
    lServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    
    Button btnRemove = new Button(grpDefinedWidgets, SWT.NONE);
    btnRemove.setText("Remove");
    btnRemove.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] selected=lServices.getSelection();
        for (String selection: selected) {
          data.removeWidgetByName(selection);
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
        if (e.getSource()==txtWidgetName ) {
          if (!javaModified 
              && (txtWidgetName.getText()!=null && txtWidgetName.getText().length()>0)) {
            txtWidgetDescription.setText("sailpoint.server."+CoreUtils.toCamelCase(txtWidgetName.getText())+"Service");
          }
        }
        
        
        validateFields();
        
      }
    };
    txtWidgetName.addModifyListener(modLsnr);
    txtWidgetDescription.addModifyListener(modLsnr);
    
    m_bindingContext = initDataBindings();
    
    btnConfigureWidgets.setSelection(false);
    m_bindingContext.updateModels();
    m_bindingContext.updateTargets();
    
    recursiveSetEnabled(baseComposite, false);
     
	}
	
	private void validateFields() {
	  
	  boolean canAdd=true;
	  
    String sServiceName = txtWidgetName.getText();
    
    String sClazzName = txtWidgetDescription.getText();
    canAdd=sClazzName.length()>0;
    if (!canAdd) {
      setErrorMessage("Widget Descriptionmust be specified");
      btnAdd.setEnabled(false);
      return;
    }
    
    if (data.getWidgetByName(sServiceName)!=null) {
      setErrorMessage("Widget Name already exists");
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
  }

  public PageDataWidgets getData() {
    return data;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
    DataBindingContext bindingContext = new DataBindingContext();
    //
    ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
    IObservableMap observeMap = PojoObservables.observeMap(listContentProvider.getKnownElements(), WidgetDescription.class, "widgetName");
    lvDefinedWidgets.setLabelProvider(new ObservableMapLabelProvider(observeMap));
    lvDefinedWidgets.setContentProvider(listContentProvider);
    //
    IObservableList widgetsDataObserveList = BeanProperties.list("widgetDescriptions").observe(data);
    lvDefinedWidgets.setInput(widgetsDataObserveList);
    //
    IObservableValue observeEnabledBaseCompositeObserveWidget_1 = WidgetProperties.enabled().observe(baseComposite);
    IObservableValue observeSelectionBtnConfigureWidgetsObserveWidget_1 = WidgetProperties.selection().observe(btnConfigureWidgets);
    bindingContext.bindValue(observeEnabledBaseCompositeObserveWidget_1, observeSelectionBtnConfigureWidgetsObserveWidget_1, null, null);
    //
    IObservableValue observeTextTxtBaseEndpointObserveWidget_2 = WidgetProperties.text(SWT.Modify).observe(txtWidgetName);
    IObservableValue textTxtWidgetNameObserveValue = PojoProperties.value("text").observe(txtWidgetDescription);
    bindingContext.bindValue(observeTextTxtBaseEndpointObserveWidget_2, textTxtWidgetNameObserveValue, null, null);
    //
    IObservableValue observeSelectionBtnConfigureServicesObserveWidget = WidgetProperties.selection().observe(btnConfigureWidgets);
    IObservableValue hasWidgetsDataObserveValue = BeanProperties.value("hasWidgets").observe(data);
    bindingContext.bindValue(observeSelectionBtnConfigureServicesObserveWidget, hasWidgetsDataObserveValue, null, null);
    //
    return bindingContext;
  }
}
