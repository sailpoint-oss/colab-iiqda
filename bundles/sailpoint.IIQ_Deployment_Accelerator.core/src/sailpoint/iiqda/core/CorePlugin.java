package sailpoint.iiqda.core;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * The IIQ-specific pieces have been abstracted so that on lazy initialisation
 * They will only be instantiated if we have specified the location of identityiq.jar
 * 
 */

public class CorePlugin extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "sailpoint.IIQ_Deployment_Accelerator"; //$NON-NLS-1$

  private static final boolean DEBUG_PLUGIN = "true".equalsIgnoreCase(Platform
      .getDebugOption(PLUGIN_ID+"/debug/Plugin"));

  // The shared instance
  private static CorePlugin plugin;
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {

    if(DEBUG_PLUGIN) {
      CorePlugin.logDebug("CorePlugin.start: ");
    }
    super.start(context);
    plugin = this;
    
  }
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    if(DEBUG_PLUGIN) {
      CorePlugin.logDebug("CorePlugin.stop: ");
    }
//    ResourcesPlugin.getWorkspace().removeResourceChangeListener(_changeListener);
    super.stop(context);
  }

  /**
   * Get an ImageDescriptor for the for specified plugin
   * image.
   */
  public static ImageDescriptor getIconImageDescriptor(String imageName) {
    ImageDescriptor img = null;

    try {
      URL pluginURL = plugin.getBundle().getEntry("/");
      URL imgURL = new URL(pluginURL, "icons/" + imageName);

      img = ImageDescriptor.createFromURL(imgURL);
    } catch (MalformedURLException e) {
      //Activator.log(IStatus.INFO, "getIconImage", e);
    }

    return img;
  }
  
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
    plugin.getLog().log(new Status(sev, PLUGIN_ID, Status.OK, msg, e));
  }

}
