package sailpoint.iiqda.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class provides the means to internationalize strings that are located
 * in the sailpoint.sailpoint.iiqda package.  It sets up a ResourceBundle based on the
 * file <code>IIQStrings.properties</code> in the sailpoint.sailpoint.iiqda
 * package, and allows retrieval of keyed strings from that bundle. 
 *
 */
public class IIQStrings
{
	private static final String BUNDLE_NAME = "sailpoint.iiqda.i18n.IIQResources";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	
	public static String getString(String key)
	{
		String result = key;
		try
		{
			result = RESOURCE_BUNDLE.getString(key).trim();
		}
		catch(MissingResourceException e)
		{
		}
		
		return(result);
	}
	
	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.  Substitutions will be made if
	 * supplied.
	 * 
	 * @param key
	 * @param substitutions
	 * @return String
	 */
	
	public static String getString(String key, Object[] substitutions)
	{
		String result = '!' + key + '!';
		try
		{
			result = RESOURCE_BUNDLE.getString(key).trim();
		}
		catch(MissingResourceException e)
		{
		}
		
		return MessageFormat.format(result, substitutions);
	}
	
	/**
	 * Returns the string from the plugin's resource bundle,
	 * or <code>null</code> if not found.
	 */
	
	public static String getBundleString(String key)
	{
		String result = null;
		try
		{
			result = RESOURCE_BUNDLE.getString(key).trim();
		}
		catch(MissingResourceException e)
		{
		}
		
		return(result);
	}
	
}
