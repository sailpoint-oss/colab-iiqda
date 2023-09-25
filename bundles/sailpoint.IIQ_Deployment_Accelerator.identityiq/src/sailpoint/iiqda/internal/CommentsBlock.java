package sailpoint.iiqda.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import org.apache.commons.lang.StringUtils;

public class CommentsBlock {

	private int maxLength=0;
	private List<String> comments;

	public CommentsBlock(){
		comments=new ArrayList<String>();
	}
	
	public void addComment(String comment) {
		
		String[] commentLines=comment.split("\n");
		for(int i=0;i<commentLines.length;i++) {
			comments.add(commentLines[i]);
			if(commentLines[i].length()>maxLength) maxLength=commentLines[i].length();
		}
		
	}
	
	public String toString() {
		
		StringBuffer buf=new StringBuffer();
		
		String s="***";//StringUtils.repeat("*", maxLength);
		buf.append("/****");
		buf.append(s);
		buf.append("\n");
		Iterator<String> iter=comments.iterator();
		
		while(iter.hasNext()) {
			String comm=iter.next();
			int spaces=0;
			if(comm!=null) spaces=maxLength-comm.length();
			buf.append(" * ");
			buf.append(comm);
			for(int i=0;i<spaces;i++) buf.append(' ');
			buf.append(" *\n");
		}
		buf.append(" ");
		buf.append(s);
		buf.append("***/\n");
		
		return buf.toString();
		
	}
}
