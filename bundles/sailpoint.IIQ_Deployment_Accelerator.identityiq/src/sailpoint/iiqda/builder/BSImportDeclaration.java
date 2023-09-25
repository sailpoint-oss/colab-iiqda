package sailpoint.iiqda.builder;

public class BSImportDeclaration {

	private String declaration;
	private int start;
	private int length;
	private int lineNumber;
	private boolean onDemand=false;
	
	public BSImportDeclaration(String declaration, boolean onDemand, int start, int length) {
	  this(declaration, onDemand, start, length, 0);
	}
  public BSImportDeclaration(String declaration, boolean onDemand, int start, int length, int lineNumber) {
		  this.declaration=declaration;
		  this.onDemand=onDemand;
		  this.start=start;
		  this.length=length;
		  this.lineNumber=lineNumber;
	}

	public String getImport() {
	  if(onDemand) {
	    return declaration+".*";
	  }
	  return declaration;
  }

	public boolean isOnDemand() {
	  return onDemand;
	}
	
	public int getStart() {
		return start;
	}

	public int getLength() {
		return length;
	}
	
	public int getLineNumber() {
	  return lineNumber;
	}
}
