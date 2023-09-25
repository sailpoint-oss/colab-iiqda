package sailpoint.iiqda.wizards.importresource.idn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;

import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.idn.IDNRestHandler;
import sailpoint.iiqda.objects.idn.Source;
import sailpoint.iiqda.objects.idn.Transform;
import sailpoint.iiqda.widgets.idn.NameValueListViewer;
import sailpoint.iiqda.wizards.IImportResourceWizardTab;
import sailpoint.iiqda.wizards.ImportResourceWizardTab;
import sailpoint.iiqda.wizards.ObjectDefinition;

public class ListOfObjectsTab extends ImportResourceWizardTab implements IImportResourceWizardTab, ISelectionChangedListener {

  private List<String> objectTypes;
  private Combo objectTypeSelector;
  private NameValueListViewer objectSelector;
  private final ImportResourceWizardPage wizardPage;
  private IDNRestHandler client;
  private boolean initializedOK=false;

  public ListOfObjectsTab(ImportResourceWizardPage wizardPage, IDNRestHandler restClient, TabFolder fldr) {
    super(fldr, SWT.NULL);
    this.client=restClient;
    this.wizardPage=wizardPage;
    // TODO Auto-generated method stub
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.verticalSpacing = 9;
    layout.horizontalSpacing=10;
    setLayout(layout);

    Label objectTypeLabel = new Label(this, SWT.NONE);
    objectTypeLabel.setText("Object Type");
    objectTypeSelector = new Combo (this, SWT.DROP_DOWN|SWT.READ_ONLY);

    GridData gd = new GridData(SWT.FILL, SWT.TOP, false, false);
    objectTypeSelector.setLayoutData(gd);
    objectTypeSelector.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        objectTypeChanged();
      }
    });

    Label objectNamesLabel = new Label(this, SWT.NONE);
    objectNamesLabel.setText("Object Name");
    gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
    objectNamesLabel.setLayoutData(gd);

    objectSelector = new NameValueListViewer(this, null);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    //gd.horizontalSpan=2;
    objectSelector.setLayoutData(gd);		
    objectTypeSelector.select(0);
  }

  private void objectTypeChanged() {
    List<IDNObject> objects=new ArrayList<IDNObject>();
    int iObjectType=objectTypeSelector.getSelectionIndex();
    if(iObjectType!=0) {
      String sObjectType=objectTypeSelector.getItem(iObjectType);
      try {
      	// TODO: make this more generic (I'm sure there's an enum somewhere)
      	Class clazz=null;
      	if ("Source".equals(sObjectType)) {
      		clazz=Source.class;
      	} else {
      		clazz=Transform.class;
      	}
        objects=(List<IDNObject>)client.getObjects(sObjectType, clazz);
      } catch (ConnectionException ce) {
        CoreUtils.showConnectionError(getShell(), ce);
      }
      if ("Source".equals(sObjectType)) {
      	objectSelector.setLabelType("name");
      } else {
      	objectSelector.setLabelType("id");
      }
      objectSelector.removeAll();
      for(IDNObject obj:objects) {
        objectSelector.add(obj);
      }
    }
  }

  public String getObjectType() {
    if(objectTypeSelector.getSelectionIndex()<1) {
      // -1 if nothing is selected
      // 0 if "--Select--"is selected
      return null;
    }
    return objectTypeSelector.getItem(objectTypeSelector.getSelectionIndex());
  }

  @Override
  public boolean canFinish() {
    return (objectSelector.getSelection()!=null);
  }

  @Override
  public void initialize() {

    objectTypes=new ArrayList<String>();
    objectTypes.add("Transform"); // TODO: abstract this out, make it a composite list of url/label lists
    objectTypes.add("Source");
    objectTypeSelector.add("--Select--");
    for(String ot: objectTypes) {
      objectTypeSelector.add(ot);
    }
    objectTypeSelector.select(0);
    initializedOK=true;
    //wizardPage.getWizard().performCancel();
  }

  public List<ObjectDefinition> getSelectedObjects() {
    IStructuredSelection items=(IStructuredSelection)objectSelector.getSelection();
    String type=objectTypeSelector.getText();
    List<ObjectDefinition> objects=new ArrayList<ObjectDefinition>();
    Iterator<IDNObject> iter=items.iterator();
    while(iter.hasNext()){
    	IDNObject obj=iter.next();
      objects.add(new ObjectDefinition(type, obj.getId(), obj.getName()));
    }
    return objects;
  }

  @Override
  public boolean initializedOK() {
    return initializedOK;
  }

	@Override
	public void selectionChanged(SelectionChangedEvent paramSelectionChangedEvent) {
		objectTypeChanged();
	}

}
