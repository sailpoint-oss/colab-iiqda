package sailpoint.log4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import sailpoint.log4j.messages.IIQDALogMessage;
import sailpoint.log4j.messages.IIQDAMessage;
import sailpoint.log4j.messages.IIQDAShutdownMessage;

public class IIQDAAppender extends AppenderSkeleton {

  private Map<String,List<Handler>> loggerCounts;

  private final class AppenderListenerThread extends Thread {
	  
		private boolean active;
		
    private IIQDAAppender parent;
		
	  public AppenderListenerThread(IIQDAAppender serverSideAppender) {
      this.parent=serverSideAppender;
    }

    public void run() {
	  	active=true;
	  	try {
	  	  System.out.println("AppenderListenerThread listening on "+port);
	  		listener = new ServerSocket(port);
	  		while (active) {
	  			Handler handler=new Handler(listener.accept(), parent);
	  			handlers.add(handler);
	  			System.out.println("handler starting..");
	  			handler.start();
	  			System.out.println("..started");
	  		}
	  	} catch (IOException e) {
	  		// we probably closed the handler out from under it
	  	} finally {
	  		try {
	  			listener.close();
	  		} catch (Exception e) {}
	  	}
	  }

		public void quit() {
	    System.out.println("AppenderListenerThread.quit");
	    active=false;
	    try {
	      listener.close();
      } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
    }
  }

	int port=9999;
	ServerSocket listener;

	private List<Handler> handlers=new ArrayList<Handler>();
	private AppenderListenerThread listenerThread;

  public void setPort(int port) {
  	this.port = port;
  }
  public int getPort() {
  	return this.port;
  }
	
	public IIQDAAppender() {
	}

	@Override
  public void activateOptions() {
		System.out.println("ServerSideAppender.activateOptions");
		super.activateOptions();

		System.out.println("***************************************************");
		System.out.println("***************************************************");
		System.out.println("************ ServerSideAppender *******************");
		System.out.println("************ Activating Options *******************");
		System.out.println("***************************************************");
		System.out.println("***************************************************");

		listenerThread = new AppenderListenerThread(this);
		System.out.println("Starting Listener Thread on port "+port);
		listenerThread.start();
		System.out.println("done");
		
		loggerCounts=new HashMap<String,List<Handler>>();
  }
	@Override
	public void close() {
		System.out.println("***************************************************");
		System.out.println("***************************************************");
		System.out.println("************ ServerSideAppender *******************");
		System.out.println("************ Close ********************************");
		System.out.println("***************************************************");
		System.out.println("***************************************************");
		broadcast(new IIQDAShutdownMessage());
		for(Handler handler: handlers) {
			handler.closeHandler();
		}
		listenerThread.quit();
	}

	@Override
	public boolean requiresLayout() {
		System.out.println("RequiresLayout");
		return false;
	}

	@Override
	protected void append(LoggingEvent arg0) {
		IIQDALogMessage event=new IIQDALogMessage(arg0);
		broadcast(event);
	}

	private void broadcast(IIQDAMessage event) {
		int closedCount=0;
		List<Handler> closedHandlers=new ArrayList<Handler>();
	  for(Handler handler: handlers) {
			if(handler.isClosed()) {
				closedCount++;
				closedHandlers.add(handler);
			} else {
				if(event instanceof IIQDALogMessage) {
					LoggingEvent msg=((IIQDALogMessage)event).getMessage();
					if(!handler.wants(msg.getLoggerName(), msg.getLevel())) {
						return; // this handler doesn't want this log message
					}
				}
				handler.send(event);
			}
		}
	  //System.out.println("Total Handlers: "+handlers.size()+" : Closed Handlers: "+closedCount);
	  for(Handler ch: closedHandlers) {
	  	handlers.remove(ch);
	  }
  }

	public int numClients() {
		return handlers.size();
	}
	
	public void addLoggers(List<LogLevelSetting> loggers, Handler handler) {
	  // Here we need to go through this list
	  // If we find one in the loggerCounts, check whether we need to 'up' the log level
	  // If we don't find it, then
	  // Logger x=LoggerFactory.get(the logger)
	  //  x.addAppender(parent)
	  // Either way, add handler to the count
	  for (LogLevelSetting lvl: loggers) {
	    List<Handler> handlerList=loggerCounts.get(lvl.getLogClass());
	    if(handlerList==null) {
	      handlerList=new ArrayList<Handler>();
	      handlerList.add(handler);
	      loggerCounts.put(lvl.getLogClass(), handlerList);
	      Logger lgr = Logger.getLogger(lvl.getLogClass());
	      lgr.setLevel(lvl.getLevel().getLevel());
	      lgr.addAppender(this);
	    } else {
	      if(!handlerList.contains(handler)) {
	        handlerList.add(handler);
	      }
	      loggerCounts.put(lvl.getLogClass(), handlerList);
	      Logger lgr = Logger.getLogger(lvl.getLogClass());
	      if( lgr.getLevel().isGreaterOrEqual(lvl.getLevel().getLevel()) ) {
	        System.out.println("Changing logger "+lvl.getLogClass()+" from "+lgr.getLevel()+" to "+lvl.getLevel().getLevel() );
	        lgr.setLevel(lvl.getLevel().getLevel());
	      }
	    }
	  }
//	  System.out.println("addLoggers");
	  printLoggerCounts();
	}
	
	
  public void removeLoggers(List<LogLevelSetting> loggers, Handler handler) {
	  // Decrease the count. If it gets to zero, remove it
	  for (LogLevelSetting lvl: loggers) {
	    List<Handler> handlerList=loggerCounts.get(lvl.getLogClass());
	    if(handlerList!=null) {
	      handlerList.remove(handler);
	      if(handlerList.size()==0) {
	        loggerCounts.remove(lvl.getLogClass());
	        Logger lgr = Logger.getLogger(lvl.getLogClass());
	        lgr.removeAppender(this);
	      } else {
	        loggerCounts.put(lvl.getLogClass(), handlerList);
	      }
	    }
	  }	  
//	  System.out.println("removeLoggers");
	  printLoggerCounts();
	}
	
  private void printLoggerCounts() {
    for (String key: loggerCounts.keySet()) {
      Integer count=loggerCounts.get(key).size();
      System.out.println(key+" : "+count);
    }
    System.out.println("----");
  }

}
