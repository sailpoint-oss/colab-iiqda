package sailpoint.iiqda.preferences;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sailpoint.iiqda.IIQPlugin;

public class IIQDAImportPreferencePage
	extends PreferencePage implements IWorkbenchPreferencePage {

  private List<Button> allButtons;
  private String[] selectedTypes;

	public IIQDAImportPreferencePage() {
		setDescription("Select which object classes will be included in the list of recently modified objects to select from when importing");
	}

  @Override
  protected Control createContents(Composite parent) {
    allButtons=new ArrayList<Button>();

    Composite container = new Composite(parent, SWT.NONE);

    GridLayout gl=new GridLayout();
    gl.numColumns=4;
    container.setLayout(gl);

    Button selectAll=new Button(container, SWT.CHECK);
    selectAll.setText("Select All");
    selectAll.addSelectionListener(new AllSelectionChanger(this, true));

    Button deselectAll=new Button(container, SWT.CHECK);
    deselectAll.setText("Deselect All");
    deselectAll.addSelectionListener(new AllSelectionChanger(this, false));

    Label lbl=new Label(container, SWT.NONE);
    GridData gld=new GridData();
    gld.horizontalSpan=2;
    lbl.setLayoutData(gld);


    for(String attr: IIQPlugin.IMPORTABLE_CLASSES.split(",")) {
      Button btn=new Button(container, SWT.CHECK);
      btn.setText(attr);
      if(ArrayUtils.contains(selectedTypes, attr)) {
        btn.setSelection(true);
      }
      allButtons.add(btn);
    }
    return container;
  }
  

  protected List<Button> getButtons() {
    return allButtons;
  }

	
  private class AllSelectionChanger implements SelectionListener {

    private IIQDAImportPreferencePage page;
    private boolean value;

    public AllSelectionChanger(IIQDAImportPreferencePage page, boolean value) {
      this.page=page;
      this.value=value;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
      ((Button)arg0.getSource()).setSelection(false);
      for(Button btn: page.getButtons()) {
        btn.setSelection(value);
      }
    }

    @Override
    public void widgetSelected(SelectionEvent arg0) {
      widgetDefaultSelected(arg0);
    }

  }


  @Override
  public void init(IWorkbench arg0) {
    // Set up the list of selected types
    // Get it from the preferences, or if that's empty, select all of them
    String sSelectedTypes=IIQPlugin.getDefault().getPreferenceStore().getString(IIQPreferenceConstants.P_IMPORT_TYPE_FILTER);
    if(sSelectedTypes==null || sSelectedTypes.length()==0) {
      sSelectedTypes=IIQPlugin.IMPORTABLE_CLASSES;
    }
    selectedTypes=sSelectedTypes.split(",");   
  }

  @Override
  public boolean performOk() {
    // TODO Auto-generated method stub
    StringBuilder bldr=new StringBuilder();
    boolean first=true;
    for (Button btn: getButtons()) {
      if(btn.getSelection()) {
        if(first) first=false;
        else bldr.append(",");
        bldr.append(btn.getText());
      }
    }
    IIQPlugin.getDefault().getPreferenceStore().setValue(IIQPreferenceConstants.P_IMPORT_TYPE_FILTER, bldr.toString());
    return true;
  }
  
  

}