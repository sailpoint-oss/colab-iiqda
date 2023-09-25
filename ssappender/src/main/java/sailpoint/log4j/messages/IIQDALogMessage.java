package sailpoint.log4j.messages;

import org.apache.log4j.spi.LoggingEvent;

public class IIQDALogMessage extends IIQDAMessage {

	/**
	 * 
	 */
  private static final long serialVersionUID = -2911110756247218480L;
  
	private LoggingEvent msg;

	public IIQDALogMessage(LoggingEvent arg0) {
	  this.msg=arg0;
	}

	public LoggingEvent getMessage() {
	  return msg;
  }

}
