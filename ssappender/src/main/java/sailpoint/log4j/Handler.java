package sailpoint.log4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

import sailpoint.log4j.messages.IIQDAClientQuitMessage;
import sailpoint.log4j.messages.IIQDAJoinMessage;
import sailpoint.log4j.messages.IIQDAMessage;
import sailpoint.log4j.messages.IIQDAShutdownMessage;
import sailpoint.log4j.messages.IIQDAUpdateMessage;

public class Handler extends Thread {

//  private String name;
	private Socket socket;
	private ObjectOutputStream outToClient;
	private ObjectInputStream inFromClient;
	private boolean closed=false;

	// The Appender that started this Handler
	private IIQDAAppender parent;
	
	private List<LogLevelSetting> settings=null;
	
	/**
	 * Constructs a handler thread, squirreling away the socket.
	 * All the interesting work is done in the run method.
	 */
	public Handler(Socket socket, IIQDAAppender parent) {
	  
		this.socket = socket;
		this.parent = parent;
		this.settings=new ArrayList<LogLevelSetting>();
	}
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Services this thread's client by repeatedly requesting a
	 * screen name until a unique one has been submitted, then
	 * acknowledges the name and registers the output stream for
	 * the client in a global set, then repeatedly gets inputs and
	 * broadcasts them.
	 */
	public void run() {
		try {

			// Create character streams for the socket.
			outToClient = new ObjectOutputStream(socket.getOutputStream());
			inFromClient = new ObjectInputStream(socket.getInputStream());

			// Request a name from this client.  Keep requesting until
			// a name is submitted that is not already used.  Note that
			// checking for the existence of a name and adding the name
			// must be done while locking the set of names.
			boolean close=false;
			while (!close) {
				Object o=inFromClient.readObject();
				if(!(o instanceof IIQDAMessage)) {
			    System.out.println("Strange.. o was "+o.getClass().getName());
				} else {
					IIQDAMessage msg=(IIQDAMessage)o;
					if (msg instanceof IIQDAJoinMessage) {
						updateSettings(((IIQDAJoinMessage) msg).getLogSettings());
					} else if (msg instanceof IIQDAUpdateMessage) {
					  updateSettings(((IIQDAUpdateMessage) msg).getLogSettings());
					} else if (msg instanceof IIQDAClientQuitMessage) {
					  System.out.println("Client Quit!");
						parent.removeLoggers(settings, this);
						close=true;
					} else {
				    System.out.println("messageType="+msg.getClass().getSimpleName());
					}
				}
				o=null;
			}
		} catch (IOException e) {
			// We probably closed the connection out from under the handler
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
			closed=true;
		}
	}
  private void updateSettings(List<LogLevelSetting> settings) {
    List<LogLevelSetting> toRemove=new ArrayList<LogLevelSetting>();
    
    for(LogLevelSetting current: this.settings) {
      boolean shouldRemove=true;
      for(LogLevelSetting newSetting: settings) {
        if(current.getLogClass().equals(newSetting.getLogClass())) {
          // old setting is still there in the new set of settings
          shouldRemove=false;
          break;
        }
      }
      if(shouldRemove) {
        toRemove.add(current);
      }
    }
    
    this.settings=settings;
    parent.removeLoggers(toRemove, this);
    parent.addLoggers(settings, this);
  }
  
	public void send(IIQDAMessage setMessage) {
		try {
	    outToClient.writeObject(setMessage);
	    outToClient.flush();
    } catch (IOException e) {
    	System.out.println("send: IOException "+e);
    }
  }

	
	public void closeHandler() {
		send(new IIQDAShutdownMessage());
		try {
			inFromClient.close();
		} catch (IOException ioe) {}
	}
	public boolean wants(String loggerName, Level level) {
	  if(settings==null) return false; // no required log settings have been received
	  
	  //if(debug) {
    //  IIQDeploymentAccelerator.logDebug("wants: "+loggerName+", "+level.toString());
	  //}
	  
	  for(LogLevelSetting setting: settings) {
	  	if( loggerName.startsWith(setting.getLogClass()) &&
	  			setting.getLevel().wants(level) ) return true;
	  }
	  return false;
  }
	
}




