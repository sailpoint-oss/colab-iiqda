package sailpoint.iiqda.widgets.idn;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

public class TableCellModifier implements ICellModifier {

	/**
	 * This class implements an ICellModifier
	 * An ICellModifier is called when the user modifes a cell in the 
	 * tableViewer
	 */

	private TransformControl dlg;
	private String[] columnNames;

	/**
	 * Constructor 
	 * @param TableViewerExample an instance of a TableViewerExample 
	 */
	public TableCellModifier(TransformControl dlg) {
		super();
		this.dlg = dlg;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		return true;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {

		// Find the index of the column
		int columnIndex = property.equals("key")?0:1;

		Object result = null;
		KeyValuePair kvp = (KeyValuePair) element;

		switch (columnIndex) {
			case 0 : // Key column
				result = new String(kvp.key);
				break;
			case 1 : // Value column 
				result = kvp.value;
				break;
			default :
				result = "";
		}
		return result;	
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {	

		// Find the index of the column 
	  int columnIndex = property.equals("key")?0:1;

		TableItem item = (TableItem) element;
		KeyValuePair kvp = (KeyValuePair) item.getData();
		String valueString;

		kvp.oldKey=kvp.key;
		switch (columnIndex) {
			case 0 : // ENABLED_COLUMN 
				kvp.key=(String)value;
				break;
			case 1 : // CLASS_COLUMN 
			  kvp.value=(String)value;
			  break;
			default :
		}
		dlg.updateTask(kvp);
		
	}
}
