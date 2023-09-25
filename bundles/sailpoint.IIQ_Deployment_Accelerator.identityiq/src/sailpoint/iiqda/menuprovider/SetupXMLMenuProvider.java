package sailpoint.iiqda.menuprovider;

import org.eclipse.ui.services.IServiceLocator;

import sailpoint.iiqda.IIQPlugin;

public class SetupXMLMenuProvider extends
    TargetEnvironmentMenuProvider {

  @Override
  public void initialize(IServiceLocator serviceLocator) {
    super.initialize(serviceLocator);
    System.out.println("SetupXMLMenuProvider: initialize");
    this.commandId=IIQPlugin.PLUGIN_ID+".commands.setupXMLCommand";
  }

  
  
}
