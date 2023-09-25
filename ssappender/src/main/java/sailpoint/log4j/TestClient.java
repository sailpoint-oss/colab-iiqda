package sailpoint.log4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.spi.LoggingEvent;

import sailpoint.log4j.LogLevelSetting.LogLevel;
import sailpoint.log4j.messages.IIQDAJoinMessage;
import sailpoint.log4j.messages.IIQDALogMessage;
import sailpoint.log4j.messages.IIQDAMessage;
import sailpoint.log4j.messages.IIQDAShutdownMessage;

public class TestClient {

	public static void main(String[] args) throws Exception {
		System.out.println("TestClient.main");

		// Make connection and initialize streams
		String serverAddress = "localhost";
		Socket socket = new Socket(serverAddress, 9999);

		ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inFromServer = new ObjectInputStream(socket.getInputStream());

		List<LogLevelSetting> settings=new ArrayList<LogLevelSetting>();
		settings.add(new LogLevelSetting(true, "SERI", LogLevel.DEBUG));
		//settings.add(new LogLevelSetting(true, "sailpoint.workflow", LogLevel.DEBUG));
		
		outToServer.writeObject(new IIQDAJoinMessage(settings));
		System.out.println("Joined!");

		// Process all messages from server, according to the protocol.
		boolean shutdown=false;
		while (!shutdown) {
			IIQDAMessage msg;
			try {
				msg=(IIQDAMessage)inFromServer.readObject();
				if (msg instanceof IIQDALogMessage) {
					IIQDALogMessage lMsg=(IIQDALogMessage)msg;
					LoggingEvent lEvt=lMsg.getMessage();
					System.out.println("received message: "+lEvt.getLevel()+" - "+lEvt.getMessage());
				} else if (msg instanceof IIQDAShutdownMessage) {
					System.out.println("Shutdown");
					shutdown=true;
				}
			} catch (IOException e) {
				System.out.println("Connection unexpectedly terminated");
				shutdown=true;
			}
		}
		System.out.println("Feeneeeshed!!");
		socket.close();
	}
}
