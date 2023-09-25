package sailpoint.iiqda.widgets.idn;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

class TableValueLabelProvider implements ITableLabelProvider {

	public String getColumnText(Object element, int columnIndex) {
		String result = "";
		KeyValuePair row = (KeyValuePair) element;
		switch (columnIndex) {
			case 0: 
			  result=row.key;
				break;
			case 1 : //class
				result = row.value;
				break;
		}
		return result;
	}	

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

  @Override
  public Image getColumnImage(Object arg0, int arg1) {
    return null;
  }
}