package sailpoint.iiqda;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * The IIQ-specific pieces have been abstracted so that on lazy initialisation
 * They will only be instantiated if we have specified the location of identityiq.jar
 * 
 */

public class LogViewPlugin extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "sailpoint.IIQ_Deployment_Accelerator.LogView"; //$NON-NLS-1$

  private static final boolean DEBUG_PLUGIN = "true".equalsIgnoreCase(Platform
      .getDebugOption(PLUGIN_ID+"/debug/Plugin"));

  // The shared instance
  private static LogViewPlugin plugin;
  private IEclipsePreferences prefs;

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    if(DEBUG_PLUGIN) {
      logDebug("CorePlugin.start: ");
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
    if(DEBUG_PLUGIN) {
      logDebug("CorePlugin.stop: ");
    }
    plugin = null;
//    ResourcesPlugin.getWorkspace().removeResourceChangeListener(_changeListener);
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static LogViewPlugin getDefault() {
    return plugin;
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

}
