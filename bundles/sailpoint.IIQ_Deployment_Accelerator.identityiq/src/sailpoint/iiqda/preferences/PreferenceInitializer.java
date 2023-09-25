package sailpoint.iiqda.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import sailpoint.iiqda.IIQPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = IIQPlugin.getDefault().getPreferenceStore();
		store.setDefault(IIQPreferenceConstants.P_USE_SSB_TEMPLATE, false);
		store.setDefault(IIQPreferenceConstants.P_IMPORT_AUTO_CDATA, true);
	}

}
