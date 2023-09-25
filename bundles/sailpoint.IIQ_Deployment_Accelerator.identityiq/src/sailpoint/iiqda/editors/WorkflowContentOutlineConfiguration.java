package sailpoint.iiqda.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.eclipse.wst.xml.ui.views.contentoutline.XMLContentOutlineConfiguration;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sailpoint.iiqda.IIQPlugin;

@SuppressWarnings("restriction")
public class WorkflowContentOutlineConfiguration extends XMLContentOutlineConfiguration {

  private ILabelProvider fAttributeShowingLabelProvider;
	private IContentProvider fContentProvider;

  private class AttributeShowingLabelProvider extends JFaceNodeLabelProvider {
    public boolean isLabelProperty(Object element, String property) {
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object o) {
      StringBuffer text = null;
      if (o instanceof Node) {
        Node node = (Node) o;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          String nodeName = node.getNodeName();
          text = new StringBuffer(); //nodeName);
          switch (nodeName) {
          	case "Call":
          		text.append("Call method: ");
          		text.append(node.getNodeValue());
          		break;
          	case "RuleLibraries":
          		text.append("Library References");
          		break;
          	case "Script":
          		text.append("BeanShell Script");
          		break;
            case "Step":
            case "Variable":
            case "Workflow":
              text.append(getAttribute(node, "name"));
              break;
            case "Transition":
              text.append(nodeName);
              text.append(" to ");
              text.append(getAttribute(node, "to"));
              break;
            default:
            	 text.append("Unhandled: "+nodeName);
          }
          return text.toString();
        }
      }
      return super.getText(o);
    }

    private String getAttribute(Node node, String attrName) {
      NamedNodeMap attributes = node.getAttributes();
      if (attributes!=null) {
      	Node namedItem = attributes.getNamedItem(attrName);
	      if (namedItem!=null) {
	        String attrValue=namedItem.getNodeValue();
	        return attrValue;
	      }
      }
      return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
     */
    public String getToolTipText(Object element) {
      if (element instanceof Node) {
        switch (((Node) element).getNodeType()) {
          case Node.COMMENT_NODE :
          case Node.CDATA_SECTION_NODE :
          case Node.PROCESSING_INSTRUCTION_NODE :
          case Node.TEXT_NODE : {
            String nodeValue = ((Node) element).getNodeValue().trim();
            return prepareText(nodeValue);
          }
          case Node.ELEMENT_NODE : {
          	Node node=((Node)element);
          	String nodeName = node.getNodeName();
          	StringBuilder tooltip=new StringBuilder();
          	switch (nodeName) {
          		case "RuleLibraries":
            		tooltip.append( getReferences(node) );
            		break;
          		case "Script":
          			String value=node.getNodeValue();
          			if(!value.endsWith("\n")) {
          				value=value+"\n";
          			}
          			int i=0;
          			for(i=0;i<5;i++) {
          				int cr=value.indexOf('\n');
          				if (cr==-1) break;
          				if (cr<value.length()) {
          					tooltip.append(value.substring(0,cr+1));
          					value=value.substring(cr+1);
          				} else {
          					tooltip.append(value);
          					value="";
          				}
          			}
          			if (i==5) tooltip.append("...");
          			break;
              case "Step":
              case "Variable":
              	Node descr = getChild(node, "Description");
								if(descr!=null) {
									tooltip.append(IIQPlugin.getStringValue(descr).trim());
									tooltip.append("\n----------\n");
								}
								switch (nodeName) {
									case "Step":
		              	appendIfExists(tooltip, node, "icon");
		              	appendIfExists(tooltip, node, "posX");
		              	appendIfExists(tooltip, node, "posY");
		              	appendIfExists(tooltip, node, "resultVariable");
		              	break;
		              case "Variable":
		              	appendIfExists(tooltip, node, "input");
		              	appendIfExists(tooltip, node, "output");
		              	appendIfExists(tooltip, node, "initializer");
		              	appendIfExists(tooltip, node, "type");
		              	break;
								}
								break;
              case "Workflow":
              	appendIfExists(tooltip, node, "type");           
                break;
              case "Transition":
              	// show the preceding comment's tooltip information
                Node previous = ((Node) element).getPreviousSibling();
                if (previous != null && previous.getNodeType() == Node.TEXT_NODE)
                  previous = previous.getPreviousSibling();
                if (previous != null && previous.getNodeType() == Node.COMMENT_NODE)
                  tooltip.append(previous.getNodeValue()+"\n");
                appendIfExists(tooltip, node, "when");
          	}
          	return tooltip.toString().trim();
          }
        }
      }
      return "super.getToolTipText(element)";
    }
    
  	private String getReferences(Node n) {
  		StringBuilder sb=new StringBuilder();
  	  NodeList nl=n.getChildNodes();
  	  if(nl!=null) {
  	  	for (int i=0;i<nl.getLength();i++) {
  	  		Node node=nl.item(i);
  	  		if ("Reference".equals(node.getNodeName())) {
  	  			sb.append( node.getAttributes().getNamedItem("name").getNodeValue() );
  	  			sb.append("\n");
  	  		}
  	  	}
  	  }
  	  return sb.toString();
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
    
    private void appendIfExists(StringBuilder tooltip, Node node, String attrname) {
	    NamedNodeMap map=node.getAttributes();
	    if(map==null) return;
	    Node attr=map.getNamedItem(attrname);
	    if(attr==null) return;
	    Attr a=(Attr)attr;
	    tooltip.append(attrname);
	    tooltip.append(": ");
	    tooltip.append(a.getNodeValue());
	    tooltip.append("\n");
	    
    }

		/**
     * Remove leading indentation from each line in the give string.
     * @param text
     * @return
     */
    private String prepareText(String text) {
      StringBuffer nodeText = new StringBuffer();
      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        if (c != '\r' && c != '\n') {
          nodeText.append(c);
        }
        else if (c == '\r' || c == '\n') {
          nodeText.append('\n');
          while (Character.isWhitespace(c) && i < text.length()) {
            i++;
            c = text.charAt(i);
          }
          nodeText.append(c);
        }
      }
      return nodeText.toString();
    }

    @Override
    public Image getImage(Object element) {
      Image image = null;
      if (element instanceof ElementImpl) {
        ElementImpl el=((ElementImpl) element);
        // check for an image from the content model
        ImageRegistry registry = IIQPlugin.getDefault().getImageRegistry();
        image = registry.get(el.getNodeName());
        if (image==null)
          image = createImage(el.getNodeName());
      } 
      if (image!=null) {
        return image;
      }
      return super.getImage(element);
    }

//    protected Image createImage(Object object) {
//      Image image = null;
//      Node node = (Node) object;
//      switch (node.getNodeName()) {
//        case "Workflow" : {
//          image = createImage("/icons/element_workflow.gif");
//          break;
//        }
//      }
//      return image;
//    }

    private Image createImage(String resource) {

      String elURL=null;
      switch (resource) {
      	case "Transition" : {
      		elURL="icons/element_transition.png";
      		break;
      	}
        case "Variable" : {
          elURL="icons/element_variable.jpg";
          break;
        }
        case "Workflow" : {
        	elURL="icons/element_wf.gif";
        	break;
        }
        case "RuleLibraries" : {
          elURL="icons/library.png";
          break;
        }
        case "Step" : {
          elURL="icons/table.png";
          break;
        }
      }
      if(elURL==null) {
        return null;
      }
      ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(IIQPlugin.PLUGIN_ID, elURL);
      Image image = null;

      if (desc != null) {
        image = desc.createImage();
        // dont add the missing image descriptor image to the image
        // registry
        if (!desc.equals(ImageDescriptor.getMissingImageDescriptor())) {
          IIQPlugin.getDefault().getImageRegistry().put(resource, image);
        }
      }
      return image;
    }
  }
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.wst.sse.ui.views.contentoutline.ContentOutlineConfiguration#getLabelProvider(org.eclipse.jface.viewers.TreeViewer)
   */
  public ILabelProvider getLabelProvider(TreeViewer viewer) {
    if (fAttributeShowingLabelProvider == null) {
      fAttributeShowingLabelProvider = new AttributeShowingLabelProvider();
    }
    return fAttributeShowingLabelProvider;
  }
  
	public IContentProvider getContentProvider(TreeViewer viewer) {
		if (fContentProvider == null) {
			fContentProvider = new WorkflowJFaceNodeContentProvider();
		}
		return fContentProvider;
	}
	
	
}