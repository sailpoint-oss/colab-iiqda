package sailpoint.iiqda.resources;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class IIQDAUIMessages extends NLS {

	private static final String BUNDLE_NAME = "sailpoint.iiqda.IIQDAResources";//$NON-NLS-1$
	private static ResourceBundle fResourceBundle;

	public static ResourceBundle getResourceBundle() {
		try {
			if (fResourceBundle == null)
				fResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
		}
		catch (MissingResourceException x) {
			fResourceBundle = null;
		}
		return fResourceBundle;
	}

}
