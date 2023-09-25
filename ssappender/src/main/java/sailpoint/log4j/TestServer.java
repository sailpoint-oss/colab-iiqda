package sailpoint.log4j;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

public class TestServer {
	
	public static void main(String[] args) {
		
		Logger l = Logger.getLogger("test");
		
		IIQDAAppender app=new IIQDAAppender();
		l.addAppender(app);
		
		Random rnd=new Random();
		
		while(true){
			try {
	      Thread.sleep(10000);
      } catch (InterruptedException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
			int type=rnd.nextInt(5);
			System.out.print(new Date()+" " );
			switch(type) {
				case 0:
					l.error("Hello - this is an error message");
					break;
				case 1:
					l.warn("Hello - this is a warn message");
					break;
				case 2:
					l.info("Hello - this is an info message");
					break;
				case 3:
					l.debug("Hello - this is a debug message");
					break;
				default:
				l.trace("Hello - this is a trace message");
			}
		}
		
		//System.out.println("TestServer finished");
		
	}

}
