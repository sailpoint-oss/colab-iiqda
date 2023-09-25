package sailpoint.iiqda.wizards.project;

import org.eclipse.jface.viewers.TableViewer;

public class SettingNameEditingSupport extends SettingEditingSupport {

    public SettingNameEditingSupport(TableViewer viewer) {
      super(viewer);
    }

    @Override
    protected Object getValue(Object element) {
      return ((Setting) element).getName();
    }

    @Override
    protected void setValue(Object element, Object userInputValue) {
      String val = String.valueOf(userInputValue);
      ((Setting) element).setName(val);
      if (val==null||val.length()==0) {
        ((Setting) element).setValue("");
      }
      update(element);
      
    }
}
