package sailpoint.iiqda.editors;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public class FakeNode implements Node {

	private String name;
	private String value;
	
	public FakeNode(String name, String value) {
		this.name=name;
		this.value=value;
	}
	
	@Override
	public String getNodeName() {
		return name;
	}

	@Override
	public String getNodeValue() throws DOMException {
		return value;
	}

	@Override
	public void setNodeValue(String nodeValue) throws DOMException {
		System.out.println("FakeNode.setNodeValue");

	}

	@Override
	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}

	@Override
	public Node getParentNode() {
		System.out.println("FakeNode.getParentNode");
		return null;
	}

	@Override
	public NodeList getChildNodes() {
		System.out.println("FakeNode.getChildNodes");
		return null;
	}

	@Override
	public Node getFirstChild() {
		System.out.println("FakeNode.getFirstChild");
		return null;
	}

	@Override
	public Node getLastChild() {
		System.out.println("FakeNode.getLastChild");
		return null;
	}

	@Override
	public Node getPreviousSibling() {
		System.out.println("FakeNode.getPreviousSibling");
		return null;
	}

	@Override
	public Node getNextSibling() {
		System.out.println("FakeNode.getNextSibling");
		return null;
	}

	@Override
	public NamedNodeMap getAttributes() {
		System.out.println("FakeNode.getAttributes");
		return null;
	}

	@Override
	public Document getOwnerDocument() {
		System.out.println("FakeNode.getOwnerDocument");
		return null;
	}

	@Override
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		System.out.println("FakeNode.insertBefore");
		return null;
	}

	@Override
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		System.out.println("FakeNode.replaceChild");
		return null;
	}

	@Override
	public Node removeChild(Node oldChild) throws DOMException {
		System.out.println("FakeNode.removeChild");
		return null;
	}

	@Override
	public Node appendChild(Node newChild) throws DOMException {
		System.out.println("FakeNode.appendChild");
		return null;
	}

	@Override
	public boolean hasChildNodes() {
		System.out.println("FakeNode.hasChildNodes");
		return false;
	}

	@Override
	public Node cloneNode(boolean deep) {
		System.out.println("FakeNode.cloneNode");
		return null;
	}

	@Override
	public void normalize() {
		System.out.println("FakeNode.normalize");

	}

	@Override
	public boolean isSupported(String feature, String version) {
		System.out.println("FakeNode.isSupported");
		return false;
	}

	@Override
	public String getNamespaceURI() {
		System.out.println("FakeNode.getNamespaceURI");
		return null;
	}

	@Override
	public String getPrefix() {
		System.out.println("FakeNode.getPrefix");
		return null;
	}

	@Override
	public void setPrefix(String prefix) throws DOMException {
		System.out.println("FakeNode.setPrefix");

	}

	@Override
	public String getLocalName() {
		System.out.println("FakeNode.getLocalName");
		return null;
	}

	@Override
	public boolean hasAttributes() {
		System.out.println("FakeNode.hasAttributes");
		return false;
	}

	@Override
	public String getBaseURI() {
		System.out.println("FakeNode.getBaseURI");
		return null;
	}

	@Override
	public short compareDocumentPosition(Node other) throws DOMException {
		System.out.println("FakeNode.compareDocumentPosition");
		return 0;
	}

	@Override
	public String getTextContent() throws DOMException {
		System.out.println("FakeNode.getTextContent");
		return null;
	}

	@Override
	public void setTextContent(String textContent) throws DOMException {
		System.out.println("FakeNode.setTextContent");

	}

	@Override
	public boolean isSameNode(Node other) {
		System.out.println("FakeNode.isSameNode");
		return false;
	}

	@Override
	public String lookupPrefix(String namespaceURI) {
		System.out.println("FakeNode.lookupPrefix");
		return null;
	}

	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		System.out.println("FakeNode.isDefaultNamespace");
		return false;
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
		System.out.println("FakeNode.lookupNamespaceURI");
		return null;
	}

	@Override
	public boolean isEqualNode(Node arg) {
		System.out.println("FakeNode.isEqualNode");
		return false;
	}

	@Override
	public Object getFeature(String feature, String version) {
		System.out.println("FakeNode.getFeature");
		return null;
	}

	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		System.out.println("FakeNode.setUserData");
		return null;
	}

	@Override
	public Object getUserData(String key) {
		System.out.println("FakeNode.getUserData");
		return null;
	}

}
