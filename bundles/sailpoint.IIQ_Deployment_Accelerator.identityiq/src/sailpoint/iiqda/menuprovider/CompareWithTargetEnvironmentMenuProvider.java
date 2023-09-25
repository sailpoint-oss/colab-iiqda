package sailpoint.iiqda.menuprovider;

import org.eclipse.ui.services.IServiceLocator;

import sailpoint.iiqda.IIQPlugin;

public class CompareWithTargetEnvironmentMenuProvider extends
    TargetEnvironmentMenuProvider {

  @Override
  public void initialize(IServiceLocator serviceLocator) {
    super.initialize(serviceLocator);
    this.commandId=IIQPlugin.PLUGIN_ID+".commands.compareWithTargetEnvironmentCommand";
  }

  
  
}
