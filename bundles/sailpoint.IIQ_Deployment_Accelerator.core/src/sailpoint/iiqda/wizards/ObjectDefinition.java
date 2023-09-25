package sailpoint.iiqda.wizards;

public class ObjectDefinition {

	private String objectType=null;
	private String objectName=null;
	private String objectId=null;

	public ObjectDefinition(String type, String name, String id) {
		this.objectType=type;
		this.objectName=name;
		this.objectId=id;
	}

	public String getObjectType() {
		return objectType;
	}
	public String getObjectName() {
		return objectName;
	}
	public String getObjectId() {
		return objectId;
	}
}
