package sailpoint.iiqda.widgets.idn;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

public abstract class KVPairEditingSupport extends EditingSupport {

  protected final TableViewer viewer;
  protected final CellEditor editor;

  private List<Listener> listeners;
  
  public KVPairEditingSupport(TableViewer viewer) {
    super(viewer);
    this.listeners=new ArrayList<Listener>();
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

  protected void updateViewer(Object element) {
    KeyValuePair avPair = (KeyValuePair) element;
    if("".equals(avPair.value) && "".equals(avPair.key)) {
      viewer.remove(element);
    } else {
      viewer.update(element, null);
      notifyChange(avPair);
    }
    TableItem[] items=viewer.getTable().getItems();
    TableItem item=items[items.length-1];
    
    if( !"".equals(item.getText(0))||!"".equals(item.getText(1)) ){
      viewer.add(new KeyValuePair("",""));
    }
  }
  
  public void addModifyListener(Listener lsnr) {
    listeners.add(lsnr);
  }

  public void removeModifyListener(Listener lsnr) {
    listeners.remove(lsnr);
  }
  
  protected void notifyChange(KeyValuePair kvp) {
    Event evt=new Event();
    evt.type=SWT.Modify;
    evt.data=kvp;
    for (Listener lsnr: listeners) {
      lsnr.handleEvent(evt);
    }
  }
  
}
