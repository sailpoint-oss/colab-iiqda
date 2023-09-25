package sailpoint.iiqda.wizards.project;

import org.eclipse.jface.viewers.TableViewer;

public class SettingHelpEditingSupport extends SettingEditingSupport {

    public SettingHelpEditingSupport(TableViewer viewer) {
      super(viewer);
    }

    @Override
    protected Object getValue(Object element) {
      return ((Setting) element).getHelpText();
    }

    @Override
    protected void setValue(Object element, Object userInputValue) {
      String val = String.valueOf(userInputValue);
      ((Setting) element).setHelpText(val);
      if (val==null||val.length()==0) {
        ((Setting) element).setHelpText("");
      }
      update(element);
    }
}
