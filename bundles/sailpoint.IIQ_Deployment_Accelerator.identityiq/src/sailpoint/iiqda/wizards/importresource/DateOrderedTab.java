package sailpoint.iiqda.wizards.importresource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import sailpoint.iiqda.IIQPlugin;
import sailpoint.iiqda.core.CoreUtils;
import sailpoint.iiqda.deployer.IIQRESTClient;
import sailpoint.iiqda.exceptions.ConnectionException;
import sailpoint.iiqda.preferences.IIQPreferenceConstants;
import sailpoint.iiqda.wizards.IImportResourceWizardTab;
import sailpoint.iiqda.wizards.ImportResourceWizardTab;
import sailpoint.iiqda.wizards.ObjectDefinition;

public class DateOrderedTab extends ImportResourceWizardTab implements IImportResourceWizardTab {

  private final ImportResourceWizardPage wizardPage;
  private Table table;
  private IIQRESTClient client;
  private boolean initializedOK;

  private String[] selectedTypes;
  private List<String> recentObjects;
  private String sSelectedTypes;
  
  public DateOrderedTab(final ImportResourceWizardPage wizardPage, IIQRESTClient client, TabFolder fldr) {

    super(fldr, SWT.NULL);
    this.client=client;
    this.wizardPage=wizardPage;

    String sSelectedTypes = IIQPlugin.getDefault().getPreferenceStore().getString(IIQPreferenceConstants.P_IMPORT_TYPE_FILTER);
    if(sSelectedTypes==null || sSelectedTypes.length()==0) {
      sSelectedTypes=IIQPlugin.IMPORTABLE_CLASSES;
    }
    this.setSelectedTypes(sSelectedTypes);

    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.verticalSpacing = 9;
    layout.horizontalSpacing=10;
    setLayout(layout);

    Text text = new Text(this, SWT.NONE);
    text.setText("Select some recently modified objects");

    table = new Table(this, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
    table.setHeaderVisible(true);
    String[] titles = { "Type", "Name", "Modified", "id" };
    for (String title: titles) {
      TableColumn column = new TableColumn (table, SWT.NONE);
      column.setText (title);
      // Add a column for the id (for later use) but don't display it
      switch(title) {
        case "Type":
        case "Name":
          column.setWidth(100);
          break;
        case "Modified":
          column.setWidth(200);
          break;
        case "id":
          column.setWidth(0);
          column.setResizable(false);
      }
    }

    table.addSelectionListener(new CatSelectionListener(wizardPage));		
    GridData gd=new GridData();
    gd.heightHint = 200;
    table.setLayoutData(gd);
    Button btn=new Button(this, SWT.NONE);
    btn.setText("Filter types");
    btn.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        FilterTypeDialog ftd=new FilterTypeDialog(
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            selectedTypes);
        int res=ftd.open();
        if(res==0) {
          DateOrderedTab.this.setSelectedTypes(ftd.getSelectedTypes());
        }
        try {
          refreshList();
        } catch (ConnectionException e) {
          CoreUtils.showConnectionError(wizardPage.getShell(), e);
        }
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
    });
  }


  private void setSelectedTypes(String types) {
    this.sSelectedTypes=types;
    this.selectedTypes=types.split(",");
  }

  private void setSelectedTypes(String[] types) {
    this.selectedTypes=types;
    StringBuilder sb=new StringBuilder();
    boolean first=true;
    for(String type: types) {
      if(first) first=false;
      else sb.append(",");
      sb.append(type);
    }
    this.sSelectedTypes=sb.toString();
  }
  

  @Override
  public boolean canFinish() {
    return table.getSelectionCount()>0;
  }

  @Override
  public void initialize() {

    try {
      refreshList();
    } catch (ConnectionException e) {
      CoreUtils.showConnectionError(wizardPage.getShell(), e);
      initializedOK=false;
      return;
    }
    initializedOK=true;
    //wizardPage.getWizard().performCancel();

  }


  private void refreshList() throws ConnectionException {
    table.removeAll();
    // We receive objects as a CSV of
    // modifiedSortable + "," + className + "," + name + "," + id;
    recentObjects = client.getRecentObjects(this.sSelectedTypes, 100);

    for(String obj: recentObjects) {
      String[] objectData=obj.split(",");
      if(ArrayUtils.contains(selectedTypes, objectData[1])) {
        TableItem itm=new TableItem(table, SWT.NONE);
        itm.setText(0, objectData[1]);
        itm.setText(1, objectData[2]);
        itm.setText(2, objectData[0]);
        itm.setText(3, objectData[3]);
      }
    }
  }

  public List<ObjectDefinition> getSelectedObjects() {
    TableItem[] items=table.getSelection();
    List<ObjectDefinition> objects=new ArrayList<ObjectDefinition>();
    for(TableItem item: items) {
      objects.add(new ObjectDefinition(item.getText(0), item.getText(1), item.getText(2)));
    }
    return objects;
  }


  @Override
  public boolean initializedOK() {
    return initializedOK;
  }

}
