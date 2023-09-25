package sailpoint.iiqda.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PropertyPage;

import sailpoint.iiqda.IDNPlugin;
import sailpoint.iiqda.dialogs.IDNDetailsDialog;
import sailpoint.iiqda.idn.IDNEnvironment;
import sailpoint.iiqda.idn.IDNHelper;
import sailpoint.iiqda.preferences.IDNPreferenceConstants;
import sailpoint.iiqda.wizards.project.idn.IDNDetailsControl;

public class IDNProjectPropertyPage extends PropertyPage implements Listener {
  public IDNProjectPropertyPage() {
  }

  private Map<String,IDNEnvironment> orgData;

  private static final boolean DEBUG_PROJECT = "true".equalsIgnoreCase(Platform
      .getDebugOption(IDNPlugin.PLUGIN_ID+"/debug/ProjectPropertyPage"));
  private IDNDetailsControl projectGroup;

  private List configurations;

  @Override
  public void handleEvent(Event arg0) {

  }

  @Override
  public boolean performOk() {
    java.util.List<IDNEnvironment> outbound=new ArrayList<IDNEnvironment>();
    for (String conf: configurations.getItems()) {
      IDNEnvironment env=orgData.get(conf);
      outbound.add(env);
    }
    String endpoints=IDNHelper.convertEnvironmentstoString(outbound);


    IResource resource = (IResource) getElement();
    
    try {
      resource.setPersistentProperty(
        new QualifiedName("", IDNPreferenceConstants.P_IDN_ENDPOINTS), endpoints);
    } catch (CoreException ce) {
      IDNPlugin.logError("CoreException writing IDN environments: "+ce);
    }

    return true;
  }

  @Override
  protected Control createContents(Composite parent) {
    if (DEBUG_PROJECT) IDNPlugin.logDebug("parent="+Integer.toHexString(parent.hashCode()));

    Composite theControl=new Composite(parent, SWT.FILL);
    GridLayout gl_theControl = new GridLayout(2, false);
    theControl.setLayout(gl_theControl);

    Label lblConfiguredIdnOrganisations = new Label(theControl, SWT.NONE);
    lblConfiguredIdnOrganisations.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
    lblConfiguredIdnOrganisations.setText("Configured IDN Organisations");

    configurations = new List(theControl, SWT.BORDER);
    configurations.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));


    Composite buttons = new Composite(theControl, SWT.NONE);
    buttons.setLayout(new RowLayout(SWT.VERTICAL));
    Button btnAdd = new Button(buttons, SWT.NONE);
    btnAdd.setText("Add");
    btnAdd.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IDNDetailsDialog dlh=new IDNDetailsDialog(getShell());    
        IDNEnvironment env=dlh.open(null);
        if (env!=null) {
          orgData.put(env.getName(), env);
          configurations.add(env.getName());
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
      
    });
    
    
    Button btnEdit = new Button(buttons, SWT.NONE);
    btnEdit.setText("Edit");
    btnEdit.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {

        String[] selected=configurations.getSelection();
        if(selected.length==1) {
          String selection=selected[0];
          IDNDetailsDialog dlh=new IDNDetailsDialog(getShell());
          IDNEnvironment current=orgData.get(selection);
          IDNEnvironment env=dlh.open(current);
          if (env!=null) {
            if(!env.getName().equals(selection)) {
              // Name was changed
              orgData.remove(selection);
              configurations.remove(selection);
              configurations.add(env.getName());
            }
            orgData.put(env.getName(), env);
          }
        }
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
      
    });
    
    
    
    Button btnDel= new Button(buttons, SWT.NONE);
    btnDel.setText("Delete");
    btnDel.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        String[] selected=configurations.getSelection();
        for(String selection: selected) {
          configurations.remove(selection);
        }
        
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
      
    });


    //    projectGroup = new IDNDetailsControl(parent, SWT.NONE);
    //    projectGroup.addListener(SWT.Modify, this);
    orgData=new HashMap<String,IDNEnvironment>();
    populateWidgets();
    return theControl;

  }
  
  

  private void populateWidgets() {

    try {
      IResource resource=(IResource) getElement();
      String endpoints=resource.getPersistentProperty(
          new QualifiedName("", IDNPreferenceConstants.P_IDN_ENDPOINTS));
      if(endpoints!=null) {
        java.util.List<IDNEnvironment> envs= IDNHelper.convertStringToEnvironments(endpoints);
        for (IDNEnvironment env: envs) {
          orgData.put(env.getName(), env);
          configurations.add(env.getName());
        }
      }      
    } catch (CoreException e) {
      // Some kind of exception - just leave the list empty
    }
  }
}
