package sailpoint.iiqda.wizards.project;

import org.eclipse.jface.viewers.TableViewer;

public class SettingTypeEditingSupport extends SettingEditingSupport {

    public SettingTypeEditingSupport(TableViewer viewer) {
      super(viewer);
    }

    @Override
    protected Object getValue(Object element) {
      return ((Setting) element).getDataType();
    }

    @Override
    protected void setValue(Object element, Object userInputValue) {
      String val = String.valueOf(userInputValue);
      ((Setting) element).setDataType(val);
      if (val==null||val.length()==0) {
        ((Setting) element).setDataType("");
      }
      update(element);
      
    }
}
