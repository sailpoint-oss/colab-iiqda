package sailpoint.log4j.messages;

import java.util.List;

import sailpoint.log4j.LogLevelSetting;

public class IIQDAJoinMessage extends IIQDAMessage {

	/**
	 * 
	 */
  private static final long serialVersionUID = 2813217272949272929L;

  private List<LogLevelSetting> interestingLogs;
  
  public IIQDAJoinMessage(List<LogLevelSetting> interestingLogs) {
  	this.interestingLogs=interestingLogs;
  }
  
  public List<LogLevelSetting> getLogSettings() {
  	return interestingLogs;
  }
  
}
