package sailpoint.iiqda.wizards.project.ipf;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sailpoint.iiqda.i18n.IPFStrings;
import sailpoint.iiqda.wizards.project.Capability;

public class IPFNewProjectCreationPageRights extends WizardPage {

	public static final String pageName="New IPF Project";
	
	private PageDataRights data;
	private Text txtSPRight;
	private Text txtCapName;
	private org.eclipse.swt.widgets.List list;
	private org.eclipse.swt.widgets.List listCapabilities;

	private NewIPFProjectWizard wiz;
	
	
	public IPFNewProjectCreationPageRights(NewIPFProjectWizard wiz) {

    super("Rights");
    setTitle(IPFStrings.getString("wizard.newipfproject.title"));
    setDescription(IPFStrings.getString("wizard.newipfproject.page.rights"));
    
    setPageComplete(false);
    data=new PageDataRights();
    this.wiz=wiz;

  }
  
  public PageDataRights getData() {
  	return data;
  }
  
  public void setRights(List<String> rights) {
  	data.setSPRights(rights);
  	String[] s=new String[rights.size()];
  	s=rights.toArray(s);
  	list.setItems(s);
  }

	@Override
  public void setVisible(boolean visible) {
	  if (visible) {
      // page is being moved into
	    data.setSPRights(wiz.getSPRights());
    } else {
      // page is being moved away from
    }
    super.setVisible(visible);
  }

  @Override
  public boolean isPageComplete() {
	  return true;
  }

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(2, false));
		
		Group grpNewSpright = new Group(container, SWT.NONE);
		grpNewSpright.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		grpNewSpright.setText("New SPRight");
		grpNewSpright.setLayout(new GridLayout(2, false));
		
		txtSPRight = new Text(grpNewSpright, SWT.BORDER);
		txtSPRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnAdd = new Button(grpNewSpright, SWT.NONE);
		btnAdd.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnAdd.setText("Add");
		btnAdd.addSelectionListener(new SelectionListener() {

			@Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
	      widgetSelected(arg0);
      }

			@Override
      public void widgetSelected(SelectionEvent arg0) {
	      System.out.println("SelectionListener.widgetSelected");
	      data.addSPRight(txtSPRight.getText());
	      list.add(txtSPRight.getText());
	      txtSPRight.setText("");
      }
			
		});
		
		Group grpNewCapability = new Group(container, SWT.NONE);
		grpNewCapability.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpNewCapability.setText("New Capability");
		grpNewCapability.setLayout(new GridLayout(3, false));
		
		Label lblName = new Label(grpNewCapability, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name");
		
		txtCapName = new Text(grpNewCapability, SWT.BORDER);
		txtCapName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Button btnCapAdd = new Button(grpNewCapability, SWT.NONE);
		btnCapAdd.setText("Add");
		btnCapAdd.addSelectionListener(new SelectionListener() {

			@Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
	      widgetSelected(arg0);
      }

			@Override
      public void widgetSelected(SelectionEvent arg0) {
	      List<String> rights=Arrays.asList(list.getSelection());
	      Capability cap=new Capability(txtCapName.getText(), rights);
	      data.addCapability(cap);
	      //listCapabilities.add(cap.getName());
	      txtCapName.setText("");
	      list.setSelection(new int[0]);
      }
			
		});
		
		Label lblSelectSprights = new Label(grpNewCapability, SWT.NONE);
		lblSelectSprights.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblSelectSprights.setText("Select SPRights");
		
		list = new org.eclipse.swt.widgets.List(grpNewCapability, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		listCapabilities = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		listCapabilities.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button btnRemove = new Button(composite, SWT.NONE);
		btnRemove.setText("Remove");
		btnRemove.addListener( SWT.Selection, event -> {
		  System.out.println("event="+event.getClass().getName());
		  String[] selected=listCapabilities.getSelection();
		  for (String selection: selected) {
		    data.removeCapabilityByName(selection);
		  }
		});
		initDataBindings();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
  protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableList itemsListObserveWidget = WidgetProperties.items().observe(list);
		IObservableList sPRightsDataObserveList = BeanProperties.list("SPRights").observe(data);
		bindingContext.bindList(itemsListObserveWidget, sPRightsDataObserveList, null, null);
		//
		IObservableList itemsListCapabilitiesObserveWidget = WidgetProperties.items().observe(listCapabilities);
		IObservableList capabilitiesDataObserveList = BeanProperties.list("capabilities").observe(data);
		bindingContext.bindList(itemsListCapabilitiesObserveWidget, capabilitiesDataObserveList, null, null);
		//
		return bindingContext;
	}
}
