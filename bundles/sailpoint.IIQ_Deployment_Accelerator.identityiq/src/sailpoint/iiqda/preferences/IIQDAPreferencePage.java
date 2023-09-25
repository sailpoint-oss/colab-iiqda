package sailpoint.iiqda.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sailpoint.iiqda.IIQPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class IIQDAPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public IIQDAPreferencePage() {
		super(GRID);
		setPreferenceStore(IIQPlugin.getDefault().getPreferenceStore());
		setDescription("IIQ Deployment Accelerator system-wide preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {

		addField(
			new BooleanFieldEditor(
				IIQPreferenceConstants.P_USE_SSB_TEMPLATE,
				"&Use SSB Template",
				getFieldEditorParent()));

		addField(
				new BooleanFieldEditor(
						IIQPreferenceConstants.P_IMPORT_AUTO_CDATA,
						"&Automatically add CDATA tags on Import",
						getFieldEditorParent()));

//		addField(new RadioGroupFieldEditor(
//				IIQPreferenceConstants.P_CHOICE,
//			"An example of a multiple-choice preference",
//			1,
//			new String[][] { { "&Choice 1", "choice1" }, {
//				"C&hoice 2", "choice2" }
//		}, getFieldEditorParent()));
//		addField(
//			new StringFieldEditor(IIQPreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}