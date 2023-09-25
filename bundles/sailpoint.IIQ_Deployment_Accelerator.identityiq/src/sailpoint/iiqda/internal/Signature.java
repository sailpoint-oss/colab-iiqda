package sailpoint.iiqda.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sailpoint.iiqda.IIQPlugin;

public class Signature {

  private static final boolean DEBUG_SIGNATURE = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/Signature"));

	private String returnType=null;
	private List<Argument> inputs;
	private List<Argument> returns;

	public Signature(Node n) {
	  
		NamedNodeMap nnm=n.getAttributes();
		Node nReturnType=nnm.getNamedItem("returnType");
		if (nReturnType!=null) returnType=nReturnType.getNodeValue();

		inputs=new ArrayList<Argument>();
		returns=new ArrayList<Argument>();

		NodeList nl = n.getChildNodes();
		for(int i=0;i<nl.getLength(); i++) {
			Node nn=nl.item(i);
			String nodename=nn.getNodeName();
			if ("Inputs".equals(nodename)) processArguments(nn, inputs);
			if ("Returns".equals(nodename)) processArguments(nn, returns);
		}

	}

	private void processArguments(Node n, List<Argument> l) {

		NodeList nl = n.getChildNodes();

		for(int i=0;i<nl.getLength(); i++) {

			Node nn=nl.item(i);

			if ("Argument".equals(nn.getNodeName())) {
				Argument a=new Argument();

				NamedNodeMap attrs=nn.getAttributes();
				if(attrs==null && DEBUG_SIGNATURE) {
				  IIQPlugin.logDebug("processArguments: no attrs on "+nn.getNodeName());
				}
				else {
					Node nName=attrs.getNamedItem("name");
					if(nName!=null) {
						a.setName(nName.getNodeValue());
					}
				}

				NodeList nl2 = nn.getChildNodes();
				for(int j=0;j<nl2.getLength(); j++) {
					Node nn2=nl2.item(j);
					String nodename=nn2.getNodeName();
					if ("Description".equals(nodename)) {
						a.setDescription(nn2.getTextContent());
					}
				}

				l.add(a);
			}
		}
	}

	public String getReturnType() {
		return returnType;
	}

	public List<Argument> getInputs() {
		return inputs;
	}

	public List<Argument> getReturns() {
		return returns;
	}
	
	public String toXML() {
		StringBuffer buf=new StringBuffer();
		buf.append("<Signature returnType=\""+returnType+"\">\n");
		buf.append(argList("Inputs", inputs));
		buf.append(argList("Returns", returns));
		buf.append("</Signature>\n");
		return buf.toString();
		
	}

	private String argList(String tag, List<Argument> args) {
		
		StringBuffer buf=new StringBuffer();
		buf.append("<"+tag+">\n");
		
		Iterator<Argument> it=args.iterator();
		while(it.hasNext()) {
			Argument arg=it.next();
			buf.append(arg.toXML());
		}
		buf.append("</"+tag+">\n");
		
		return buf.toString();
	}
	
}
