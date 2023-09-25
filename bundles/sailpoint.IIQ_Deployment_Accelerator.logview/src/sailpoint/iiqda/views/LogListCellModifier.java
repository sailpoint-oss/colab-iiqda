package sailpoint.iiqda.views;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

import sailpoint.log4j.LogLevelSetting;
import sailpoint.log4j.LogLevelSetting.LogLevel;

public class LogListCellModifier implements ICellModifier {

	/**
	 * This class implements an ICellModifier
	 * An ICellModifier is called when the user modifes a cell in the 
	 * tableViewer
	 */

	private LogOptionsDialog dlg;
	private String[] columnNames;

	/**
	 * Constructor 
	 * @param TableViewerExample an instance of a TableViewerExample 
	 */
	public LogListCellModifier(LogOptionsDialog dlg) {
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
		int columnIndex = dlg.getColumnNames().indexOf(property);

		Object result = null;
		LogLevelSetting logSetting = (LogLevelSetting) element;

		switch (columnIndex) {
			case 0 : // COMPLETED_COLUMN 
				result = new Boolean(logSetting.isEnabled());
				break;
			case 1 : // DESCRIPTION_COLUMN 
				result = logSetting.getLogClass();
				break;
			case 2 : // OWNER_COLUMN 
				result = logSetting.getLevelInt();			
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
		int columnIndex	= dlg.getColumnNames().indexOf(property);

		TableItem item = (TableItem) element;
		LogLevelSetting task = (LogLevelSetting) item.getData();
		String valueString;

		switch (columnIndex) {
			case 0 : // ENABLED_COLUMN 
				task.setEnabled(((Boolean) value).booleanValue());
				break;
			case 1 : // CLASS_COLUMN 
				valueString = ((String) value).trim();
				task.setLogClass(valueString);
				break;
			case 2 : // LEVEL_COLUMN 
				LogLevel lvl=LogLevel.fromInt((Integer)value);
				task.setLevel(lvl);
				break;
			default :
		}
		dlg.updateTask(task);
		
	}
}
