package sailpoint.iiqda.wizards.project;

import org.eclipse.jface.viewers.TableViewer;

public class SettingValueEditingSupport extends SettingEditingSupport {

  public SettingValueEditingSupport(TableViewer viewer) {
    super(viewer);
  }

  @Override
  protected Object getValue(Object element) {
    return ((Setting) element).getValue();
  }

  @Override
  protected void setValue(Object element, Object userInputValue) {
    String val = String.valueOf(userInputValue);
    ((Setting) element).setValue(val);
    if (val==null||val.length()==0) {
      ((Setting) element).setValue("");
    }
    update(element);
  }
}
