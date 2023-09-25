package sailpoint.iiqda.menuprovider.idn;

import org.eclipse.ui.services.IServiceLocator;

import sailpoint.iiqda.IDNPlugin;

public class ImportEnvironmentMenuProvider extends
    TargetEnvironmentMenuProvider {

  @Override
  public void initialize(IServiceLocator serviceLocator) {
    super.initialize(serviceLocator);
    System.out.println("ImportEnvironmentMenuProvider.initialize: ");

    this.commandId=IDNPlugin.PLUGIN_ID+".commands.importArtifactCommand";
  }

  
  
}
