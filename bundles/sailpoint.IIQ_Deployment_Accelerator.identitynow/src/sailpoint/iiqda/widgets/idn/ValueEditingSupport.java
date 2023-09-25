package sailpoint.iiqda.widgets.idn;

import org.eclipse.jface.viewers.TableViewer;

public class ValueEditingSupport extends KVPairEditingSupport {

    public ValueEditingSupport(TableViewer viewer) {
      super(viewer);
    }
  
    @Override
    protected Object getValue(Object element) {
      return ((KeyValuePair) element).value;
    }

    @Override
    protected void setValue(Object element, Object userInputValue) {
      KeyValuePair kvPair = (KeyValuePair) element;
      kvPair.value=(String)userInputValue;
      updateViewer(element);
    }
  } 