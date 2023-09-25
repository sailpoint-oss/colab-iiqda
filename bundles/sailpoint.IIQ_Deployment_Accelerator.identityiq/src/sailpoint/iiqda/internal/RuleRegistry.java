package sailpoint.iiqda.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RuleRegistry {

	private RuleModel model=null;

	public RuleRegistry() {

	}

	public void refresh() {

		InputStream registryIS=null;
		InputStream argsIS=null;
		registryIS= this.getClass().getResourceAsStream("/sailpoint/iiqda/DefaultRuleRegistry.xml");
		InputSource registrySource=new InputSource(registryIS);
		registrySource.setEncoding("UTF-8");
		
		argsIS= this.getClass().getResourceAsStream("/sailpoint/iiqda/DefaultRuleArgs.xml");
		InputSource argsSource=new InputSource(argsIS);
		argsSource.setEncoding("UTF-8");

		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		try {
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Document registryDOM=null;
		Document argsDOM=null;

		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			registryDOM = db.parse(registrySource);
			argsDOM = db.parse(argsSource);


		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}

		try {
			registryIS.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RuleModel r=new RuleModel(registryDOM, argsDOM);
		model=r;
	}

	public RuleModel getModel() {
		if(model==null) {
			refresh();
		}
		return model;
	}
}
