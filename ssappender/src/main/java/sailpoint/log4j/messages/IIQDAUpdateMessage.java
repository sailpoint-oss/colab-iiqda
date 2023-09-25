package sailpoint.log4j.messages;

import java.util.ArrayList;
import java.util.List;

import sailpoint.log4j.LogLevelSetting;

public class IIQDAUpdateMessage extends IIQDAMessage {

	/**
   * 
   */
  private static final long serialVersionUID = 955923650487243723L;
  private List<LogLevelSetting> settings;

	public IIQDAUpdateMessage(ArrayList<LogLevelSetting> logSettings) {
		this.settings=new ArrayList<LogLevelSetting>();
		if(logSettings!=null) {
		  for(LogLevelSetting lls: logSettings) {
		    this.settings.add(lls);
		  }
		}
	}
	
	public List<LogLevelSetting> getLogSettings() {
		return settings;
	}
	
}
