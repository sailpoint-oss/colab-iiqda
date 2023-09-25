package sailpoint.iiqda;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The activator class controls the plug-in life cycle
 * The IIQ-specific pieces have been abstracted so that on lazy initialisation
 * They will only be instantiated if we have specified the location of identityiq.jar
 * 
 */

public class IPFPlugin extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "sailpoint.IIQ_Deployment_Accelerator.IdentityIQ.Plugins"; //$NON-NLS-1$

  private static final boolean DEBUG_PLUGIN = "true".equalsIgnoreCase(Platform
      .getDebugOption(PLUGIN_ID+"/debug/Plugin"));

  public static final String LAST_USED_IIQ_LOCATION = "lastUsedIIQLocation";

  // The shared instance
  private static IPFPlugin plugin;
  private IEclipsePreferences prefs;

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    if(DEBUG_PLUGIN) {
      IPFPlugin.logDebug("CorePlugin.start: ");
    }
    super.start(context);
    //iiqLoader=new IIQClassLoader((org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader)this.getClass().getClassLoader());

    // load Plugin settings
    prefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
    plugin = this;
    
//    _changeListener = new MyResourceChangeReporter();
//    ResourcesPlugin.getWorkspace().addResourceChangeListener(
//       _changeListener, IResourceChangeEvent.POST_CHANGE);

  }
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    if(DEBUG_PLUGIN) {
      IPFPlugin.logDebug("CorePlugin.stop: ");
    }
//    ResourcesPlugin.getWorkspace().removeResourceChangeListener(_changeListener);
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static IPFPlugin getDefault() {
    return plugin;
  }
  
  /**
   * Get an ImageDescriptor for the for specified plugin
   * image.
   */
  public static ImageDescriptor getIconImageDescriptor(String imageName) {
    ImageDescriptor img = null;
    
    try {
      URL pluginURL = getDefault().getBundle().getEntry("/");
      URL imgURL = new URL(pluginURL, "icons/" + imageName);
      
      img = ImageDescriptor.createFromURL(imgURL);
    } catch (MalformedURLException e) {
      //Activator.log(IStatus.INFO, "getIconImage", e);
    }
    
    return img;
  }

  public String getPreference(String pref) {
    Preferences preferences = InstanceScope.INSTANCE.getNode(IPFPlugin.PLUGIN_ID);
    return preferences.get(pref, null);
  }

  public void setPreference(String preferenceName, String preferenceValue) {
    Preferences preferences = InstanceScope.INSTANCE.getNode(IPFPlugin.PLUGIN_ID);
    preferences.put(preferenceName, preferenceValue);
    try {
      // forces the application to save the preferences
      preferences.flush();
      } catch (BackingStoreException e) {
          e.printStackTrace();
      }
  }


  public boolean getBooleanPreference(String pref) {
    Preferences preferences = InstanceScope.INSTANCE.getNode(IPFPlugin.PLUGIN_ID);
    return preferences.getBoolean(pref, false);
  }

  public void setBooleanPreference(String preferenceName, boolean preferenceValue) {
    Preferences preferences = InstanceScope.INSTANCE.getNode(IPFPlugin.PLUGIN_ID);
    preferences.putBoolean(preferenceName, preferenceValue);
    try {
      // forces the application to save the preferences
      preferences.flush();
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
  }

  public static int countLF(String region) {
    int lastIndex=0;
    int count=0;
    while(lastIndex != -1){
      lastIndex = region.indexOf('\n', lastIndex);
      if( lastIndex != -1){
        count ++;
        lastIndex++;
      }
    }
    return count;

  }
  // I've made logDebug and logTrace separate, in case we want to do something
  // different with them later

  public static void logDebug(String msg) {
    log(Status.INFO, msg, null);
  }

  public static void logTrace(String msg) {
    log(Status.INFO, msg, null);
  }

  public static void logError(String msg) {
    log(Status.ERROR, msg, null);
  }

  public static void logException(String msg, Exception e) {
    log(Status.ERROR, msg, e);
  }

  public static void log(int sev, String msg, Exception e) {
    getDefault().getLog().log(new Status(sev, PLUGIN_ID, Status.OK, msg, e));
  }
  
  public static File getResource(String path) throws IOException {
    System.out.println("IPFPlugin.getResource: path="+path);

    IPFPlugin default1 = getDefault();
    Bundle bundle = default1.getBundle();    
    URL url=bundle.getEntry(path);
    System.out.println("IPFPlugin.getResource: url="+url);

    try {
      URL resolvedURL = FileLocator.toFileURL( url );
      URI resolvedURI = new URI(resolvedURL.getProtocol(), resolvedURL.getPath(), null);
      return new File( resolvedURI );
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  

}
