package sailpoint.iiqda.widgets.idn;

import org.eclipse.jface.viewers.TableViewer;

public class KeyEditingSupport extends KVPairEditingSupport {

  public KeyEditingSupport(TableViewer viewer) {
    super(viewer);
  }

  @Override
  protected Object getValue(Object element) {
    return ((KeyValuePair) element).key;
  }

  @Override
  protected void setValue(Object element, Object userInputValue) {
    KeyValuePair kvPair = (KeyValuePair) element;
    kvPair.key=(String)userInputValue;
    updateViewer(element);
  }

} 