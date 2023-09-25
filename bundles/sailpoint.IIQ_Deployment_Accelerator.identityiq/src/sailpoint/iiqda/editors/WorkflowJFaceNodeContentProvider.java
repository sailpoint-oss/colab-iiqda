package sailpoint.iiqda.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeContentProvider;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sailpoint.iiqda.IIQPlugin;

public class WorkflowJFaceNodeContentProvider extends JFaceNodeContentProvider {

	@Override
  public Object[] getChildren(Object object) {
		// Filter the list - nothing below 'Script' - and fake others
	  Object[] objArray=super.getChildren(object);
	  List<Object> list=new ArrayList<Object>();
	  if (object instanceof Node) {
	  	Node node=(Node)object;
	  	String name=node.getNodeName();
	  	if(name!=null && name.equals("Step")) {
	  		NamedNodeMap attributes = node.getAttributes();
				if(attributes!=null && attributes.getNamedItem("action")!=null) {
					Attr attr=(Attr)attributes.getNamedItem("action");
					String action=attr.getNodeValue();
					if(action.startsWith("call:")) {
						FakeNode fn=new FakeNode("Call", action.substring(5));
						list.add(fn);
					}
				} else if (findScriptSource(node)!=null) {
					FakeNode fn=new FakeNode("Script", findScriptSource(node).trim());
					list.add(fn);
				}
	  	}
	  }
	  for(Object o: objArray) {
	  	if(o instanceof Node) {
	  		Node n=(Node)o;
	  		if(!skippable(n)) {
	  			list.add(o);
	  		}
	  	}
	  }
	  return list.toArray(new Object[list.size()]);
  }

	private boolean skippable(Node n) {
		
		if(n.getNodeType()==Node.COMMENT_NODE) return true;
		if(n.getNodeType()==Node.DOCUMENT_TYPE_NODE) return true;
		
		
		String nodeName=n.getNodeName();
		if(nodeName==null) {
			return false;
		}
	  // nodes that can be skipped
		if (nodeName.equals("Approval")) return true;
		if (nodeName.equals("Arg")) return true;
		if (nodeName.equals("Reference")) return true;
		if (nodeName.equals("Script")) return true;
		
		return false;
  }

	private String findScriptSource(Node node) {
	  
		Node script=getChild(node, "Script");
		if (script!=null) {
			Node source=getChild(script, "Source");
			if(source!=null) {
				return IIQPlugin.getStringValue(source);
			}
		}
		return null;
  }

	private Node getChild(Node parent, String name) {
		NodeList childNodes=parent.getChildNodes();
		if(childNodes!=null) {
			for(int i=0;i<childNodes.getLength(); i++) {
				Node child=childNodes.item(i);
				if(child.getNodeType()==Node.ELEMENT_NODE && child.getNodeName().equals(name)) {
					return child;
				}
			}
		}
		return null;
  }

	@Override
  public boolean hasChildren(Object object) {
	  System.out.println("WorkflowJFaceNodeContentProvider.hasChildren");
	  Object[] list=getChildren(object);
		for (Object obj: list) {
			if (obj instanceof Node) {
				Node child=(Node)obj;
				if(child.getNodeType() != Node.TEXT_NODE) {
					return true;
				}
			}
		}
		return false;
  }

	
	
}
