package sailpoint.iiqda.menuprovider.idn;

import org.eclipse.ui.services.IServiceLocator;

import sailpoint.iiqda.IDNPlugin;

public class DeployEnvironmentMenuProvider extends
    TargetEnvironmentMenuProvider {

  @Override
  public void initialize(IServiceLocator serviceLocator) {
    super.initialize(serviceLocator);
    System.out.println("DeployEnvironmentMenuProvider.initialize: ");

    this.commandId=IDNPlugin.PLUGIN_ID+".commands.deployObjectCommand";
  }

  
  
}
