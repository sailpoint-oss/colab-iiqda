package sailpoint.iiqda.comparison;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import sailpoint.iiqda.core.CoreUtils;

class CompareItem implements IStreamContentAccessor,
ITypedElement /*, IModificationDate*/ {
	
	String contents;
	private String name;
	
	CompareItem(String name, IFile file) {
		this.name = name;
		try {
			this.contents = CoreUtils.readFile(file).toString();
		} catch (CoreException ce) {
			this.contents = "Unable to read contents of file '"+file.getName()+"\n"+ce;
		}
	}
	
	CompareItem(String name, String contents) {
		this.name = name;
		this.contents = contents;
	}
	
	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(contents.getBytes());
	}
	
	public Image getImage() {return null;}
//	public long getModificationDate() {return time;}
	public String getName() {return name;}
	public String getString() {return contents;}
	public String getType() {return ITypedElement.TEXT_TYPE;}
}
