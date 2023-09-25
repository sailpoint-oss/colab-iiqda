package sailpoint.iiqda.internal;

public class Argument {

	private String name;
	private String description;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toXML() {
		
		String s="<Argument type=\"" +
				name +
				"\">\n<Description>\n" +
				description.trim() +
				"\n</Description>\n</Argument>";
		
		return s;
	}
	
}
