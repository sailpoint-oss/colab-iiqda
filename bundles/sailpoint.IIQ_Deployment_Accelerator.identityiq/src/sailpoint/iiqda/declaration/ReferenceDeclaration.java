package sailpoint.iiqda.declaration;

public class ReferenceDeclaration extends DeclarationElement {

	private String reference=null;
	
	public ReferenceDeclaration(String reference) {
		this.reference=reference;
	}
	
	public String getReference() {
		return reference;
	}
}
