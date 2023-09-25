package sailpoint.iiqda.wizards.project;

import org.eclipse.jface.viewers.TableViewer;

public class SettingLabelEditingSupport extends SettingEditingSupport {

    public SettingLabelEditingSupport(TableViewer viewer) {
      super(viewer);
    }

    @Override
    protected Object getValue(Object element) {
      return ((Setting) element).getLabel();
    }

    @Override
    protected void setValue(Object element, Object userInputValue) {
      String val = String.valueOf(userInputValue);
      ((Setting) element).setLabel(val);
      if (val==null||val.length()==0) {
        ((Setting) element).setLabel("");
      }
      update(element);
    }
}
