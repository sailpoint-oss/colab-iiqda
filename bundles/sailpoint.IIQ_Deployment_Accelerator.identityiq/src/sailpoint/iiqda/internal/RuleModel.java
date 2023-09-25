package sailpoint.iiqda.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sailpoint.iiqda.IIQPlugin;

public class RuleModel {

  private static final boolean DEBUG_MODEL = "true".equalsIgnoreCase(Platform
      .getDebugOption(IIQPlugin.PLUGIN_ID+"/debug/RuleModel"));
  
	private List<Rule> ruleList;

	public RuleModel(Document registryDOM, Document argsDOM) {

		ruleList=new ArrayList<Rule>();
		Element regDocEle = registryDOM.getDocumentElement();
		Element argsDocEle = argsDOM.getDocumentElement();

		//get a nodelist of elements
		NodeList regNL = regDocEle.getElementsByTagName("Rule");
		NodeList argNL = argsDocEle.getElementsByTagName("rule");

		int numrules=regNL.getLength();
		Node nDefaultArgs=null;
		
		for (int j=0;j<argNL.getLength(); j++) {
			
			Element eArgs=(Element)argNL.item(j);
			String sRuleType=eArgs.getAttribute("type"); 
			if("default".equals(sRuleType)) {
				nDefaultArgs=(Node)eArgs;
				continue;
			}
		}
		
		for (int i=0;i<numrules;i++) {
			Node nRule=regNL.item(i);
			String sRuleType=((Element)nRule).getAttribute("type"); 
			if(sRuleType==null || sRuleType.isEmpty()) {
				if(DEBUG_MODEL) {
				  IIQPlugin.logDebug("RuleModel.RuleModel: found default rule "+((Element)nRule).getAttribute("name"));
				}
			} else {
				Node nRuleArgs=findArgsByType(argNL, sRuleType); 
				Rule rule=new Rule(nRule, nDefaultArgs, nRuleArgs); 
				addRule(rule);
			}
		}
	}

	private Node findArgsByType(NodeList nl, String type) {

		if(nl==null||type==null) return null;

		for (int i=0;i<nl.getLength(); i++) {

			Element e=(Element)nl.item(i);
			if( type.equals(e.getAttribute("type")) ) {
				return e;
			}
		}

		return null;

	}

	public RuleModel() {

		ruleList=new ArrayList<Rule>();

	}

	public void addRule(Rule r) {
		ruleList.add(r);
	}

	public List<Rule> getRules() {
		return ruleList;
	}


	public Rule getRuleByType(String type) {
		if(type==null) return null;
		Iterator<Rule> it=ruleList.iterator();
		while(it.hasNext()) {
			Rule rl=it.next();
			if(rl.getType()!=null && rl.getType().equals(type)) {
				return rl;
			}
		}

		return null;
	}

}
