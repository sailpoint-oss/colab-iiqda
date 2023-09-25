package sailpoint.iiqda.wizards.project;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public abstract class SettingEditingSupport extends EditingSupport {

    private final TableViewer viewer;
    protected final CellEditor editor;

    public SettingEditingSupport(TableViewer viewer) {
      super(viewer);
      this.viewer = viewer;
      this.editor = new TextCellEditor(viewer.getTable());
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
      return editor;
    }

    @Override
    protected boolean canEdit(Object element) {
      return true;
    }

    protected void update(Object element) {
      viewer.update(element, null); 
      List<Setting> content=(List<Setting>)viewer.getInput();
      if (content.size()>0) {
        Setting last=content.get(content.size()-1);
        boolean hasname=(last.getName()!=null && last.getName().length()>0);
        boolean hasvalue=(last.getValue()!=null && last.getValue().length()>0);
        boolean hashelp=(last.getHelpText()!=null && last.getHelpText().length()>0);
        boolean haslabel=(last.getLabel()!=null && last.getLabel().length()>0);
        boolean hastype=(last.getDataType()!=null && last.getDataType().length()>0);
        if(hasname||hasvalue||hashelp||haslabel) {
          // Make sure we have an empty row
          Setting s=new Setting("","","","","");
          content.add(s);
        }
      }
      viewer.refresh();
      
    }
}
