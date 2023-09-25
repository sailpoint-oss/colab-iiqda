package sailpoint.iiqda.internal;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Rule {

	private String language;
	private String name;
	private String type;
	private String Description;
	private Signature signature;

	private String returnType;
	private Map<String,String> argSignatures;
	
	private boolean hasRuleArgs=false;

	/**
	 * @param nRule
	 * @param defaultArgs
	 * @param ruleArgs
	 */
	public Rule(Node nRule, Node defaultArgs, Node ruleArgs) {

		NamedNodeMap attrs=nRule.getAttributes();
		Node n=attrs.getNamedItem("language");
		if(n!=null) {
			setLanguage(n.getNodeValue());
		}
		n=attrs.getNamedItem("name");
		if(n!=null) {
			setName(n.getNodeValue());
		}
		n=attrs.getNamedItem("type");
		if(n!=null) {
			setType(n.getNodeValue());
		}

		NodeList nl2 = nRule.getChildNodes();
		for(int j=0;j<nl2.getLength(); j++) {
			Node nn=nl2.item(j);
			String nodename=nn.getNodeName();
			if ("Description".equals(nodename)) setDescription(nn.getTextContent().replace((char)160, ' '));
			if ("Signature".equals(nodename)) setSignature(new Signature(nn));
		}

		// read arg Signatures from default rule and specific rule
		argSignatures=new HashMap<String,String>();
		if (defaultArgs!=null){
			NodeList args=defaultArgs.getChildNodes();
			for(int k=0;k<args.getLength(); k++) {
				Node nn=args.item(k);
				String nodename=nn.getNodeName();
				if ("arg".equals(nodename)) {
					Element el=(Element)nn;
					String name=el.getAttribute("name");
					String clazz=el.getAttribute("class");
					if(name!=null && clazz!=null) {
						argSignatures.put(name, clazz);
					}
				}

			}
		}

		if(ruleArgs!=null) {
			this.hasRuleArgs=true;
			NodeList args=ruleArgs.getChildNodes();
			for(int k=0;k<args.getLength(); k++) {
				Node nn=args.item(k);
				String nodename=nn.getNodeName();
				if ("arg".equals(nodename)) {
					Element el=(Element)nn;
					String name=el.getAttribute("name");
					String clazz=el.getAttribute("class");
					if(name!=null && clazz!=null) {
						argSignatures.put(name, clazz);
					}
				}
			}

		}
		if (ruleArgs != null) {
			this.returnType=((Element)ruleArgs).getAttribute("return");
		}
		if(this.returnType==null) this.returnType="void";

	}
	
	public boolean hasRuleArgs() {
		return hasRuleArgs;
	}

	public String getReturnClass() {
		return returnType;
	}
	/**
	 * @return a Map  of name, class pairs of the arguments
	 */
	public Map<String,String> getArgumentSignatures() {
		return argSignatures;
	}

	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getReturnType() {
		if(signature!=null) {
			return signature.getReturnType();
		}
		return null;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public Signature getSignature() {
		return signature;
	}
	public void setSignature(Signature signature) {
		this.signature = signature;
	}

}
