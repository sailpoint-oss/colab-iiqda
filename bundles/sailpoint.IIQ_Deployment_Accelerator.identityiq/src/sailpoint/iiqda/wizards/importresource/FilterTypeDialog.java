package sailpoint.iiqda.wizards.importresource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sailpoint.iiqda.IIQPlugin;

public class FilterTypeDialog extends Dialog {

  private String[] selectedTypes;

  private List<Button> allButtons;

  public FilterTypeDialog(Shell parentShell, String[] selectedTypes) {
    super(parentShell);
    this.selectedTypes=selectedTypes;   
  }

  @Override
  protected Control createDialogArea(Composite parent) {

    allButtons=new ArrayList<Button>();

    Composite container = (Composite) super.createDialogArea(parent);

    GridLayout gl=new GridLayout();
    gl.numColumns=4;
    container.setLayout(gl);

    Label lbl=new Label(container, SWT.NONE);
    lbl.setText("Please select which object classes to include in the list of recently modified objects to select from");
    GridData gld=new GridData();
    gld.horizontalSpan=4;
    lbl.setLayoutData(gld);

    Button selectAll=new Button(container, SWT.CHECK);
    selectAll.setText("Select All");
    selectAll.addSelectionListener(new AllSelectionChanger(this, true));

    Button deselectAll=new Button(container, SWT.CHECK);
    deselectAll.setText("Deselect All");
    deselectAll.addSelectionListener(new AllSelectionChanger(this, false));

    lbl=new Label(container, SWT.NONE);
    gld=new GridData();
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



  @Override
  protected void okPressed() {

    List<String> pressed=new ArrayList<String>();
    for(Button btn: allButtons) {
      if(btn.getSelection()) {
        pressed.add(btn.getText());
      }
    }
    selectedTypes=pressed.toArray(new String[pressed.size()]);

    super.okPressed();
    
  }

  public String[] getSelectedTypes() {
    return selectedTypes;
  }

  private class AllSelectionChanger implements SelectionListener {

    private FilterTypeDialog dlg;
    private boolean value;

    public AllSelectionChanger(FilterTypeDialog dlg, boolean value) {
      this.dlg=dlg;
      this.value=value;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
      ((Button)arg0.getSource()).setSelection(false);
      for(Button btn: dlg.getButtons()) {
        btn.setSelection(value);
      }
    }

    @Override
    public void widgetSelected(SelectionEvent arg0) {
      widgetDefaultSelected(arg0);
    }



  }

}
