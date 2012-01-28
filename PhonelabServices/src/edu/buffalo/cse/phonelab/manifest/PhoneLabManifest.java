/*
 * Muhammed Fatih Bulut
 * PhoneLab Controller-Manifest Class
 * If any changes are made, then call saveDocument() method to save final version of the manifest.xml
 * */

package edu.buffalo.cse.phonelab.manifest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PhoneLabManifest {

	private Document document = null;
	private XPath xpath = null;
	private String xmlFullPath;
	public PhoneLabManifest (String xmlFullPath) throws ParserConfigurationException, SAXException, IOException {
		this.xmlFullPath = xmlFullPath;
		document = getDocuments(xmlFullPath);
		xpath = XPathFactory.newInstance().newXPath();
	}

	//This will return all the application presented in the manifest regardless of any attributes
	public ArrayList<PhoneLabApplication> getAllApplications () throws XPathExpressionException {
		ArrayList<PhoneLabApplication> appList = new ArrayList<PhoneLabApplication>();
		NodeList list = (NodeList) xpath.evaluate("/manifest/application", document, XPathConstants.NODESET);
		for (int i = 0;i < list.getLength();i++) {
			PhoneLabApplication app = new PhoneLabApplication();
			copyApp (app, (Element) list.item(i));
			appList.add(app);
		}

		return appList;
	}

	//Constraints are the attributes presented in the application tag of manifest.
	//Map should be provided as in sqlite where both key and value should be String object
	//If constraintMap is null, method returns all the applications
	public ArrayList<PhoneLabApplication> getApplicationsByConstraints (HashMap<String, String> constraintMap) throws XPathExpressionException {
		ArrayList<PhoneLabApplication> appList = new ArrayList<PhoneLabApplication>();
		String constaintStr = "";
		if (constraintMap != null) {
			for (Map.Entry<String, String> entry:constraintMap.entrySet()) {
				if (!constaintStr.equals(""))
					constaintStr += " and @" + entry.getKey() + "='" + entry.getValue() + "'";
				else
					constaintStr += "[@" + entry.getKey() + "='" + entry.getValue() + "'";
			}

			if (!constaintStr.equals(""))
				constaintStr += "]";
		}
		NodeList list = (NodeList) xpath.evaluate("/manifest/application" + constaintStr, document, XPathConstants.NODESET);
		for (int i = 0;i < list.getLength();i++) {
			PhoneLabApplication app = new PhoneLabApplication();
			copyApp (app, (Element) list.item(i));
			appList.add(app);
		}

		return appList;
	}
	
	public void setApplication(HashMap<String, String> constraintMap, HashMap<String, String> values) throws XPathExpressionException {
		String constaintStr = "";
		if (constraintMap != null) {
			for (Map.Entry<String, String> entry:constraintMap.entrySet()) {
				if (!constaintStr.equals(""))
					constaintStr += " and @" + entry.getKey() + "='" + entry.getValue() + "'";
				else
					constaintStr += "[@" + entry.getKey() + "='" + entry.getValue() + "'";
			}

			if (!constaintStr.equals(""))
				constaintStr += "]";
		}
		Element element = (Element) xpath.evaluate("/manifest/application" + constaintStr, document, XPathConstants.NODE);
		for (Map.Entry<String, String> entry:values.entrySet()) {
			element.setAttribute(entry.getKey(), entry.getValue());
		}
	}

	//Constraints are the attributes presented in the application tag of manifest.
	//Map should be provided as in sqlite where both key and value should be String object
	//If constraintMap is null, method returns all parameter tags
	public ArrayList<PhoneLabParameter> getStatParamaterByConstraints (HashMap<String, String> constraintMap) throws XPathExpressionException {
		ArrayList<PhoneLabParameter> paramList = new ArrayList<PhoneLabParameter>();
		String constaintStr = "";
		if (constraintMap != null) {
			for (Map.Entry<String, String> entry:constraintMap.entrySet()) {
				if (!constaintStr.equals(""))
					constaintStr += " and @" + entry.getKey() + "='" + entry.getValue() + "'";
				else
					constaintStr += "[@" + entry.getKey() + "='" + entry.getValue() + "'";
			}

			if (!constaintStr.equals(""))
				constaintStr += "]";
		}
		NodeList list = (NodeList) xpath.evaluate("/manifest/statusmonitor/parameter" + constaintStr, document, XPathConstants.NODESET);
		for (int i = 0;i < list.getLength();i++) {
			PhoneLabParameter param = new PhoneLabParameter();
			copyParameter (param, (Element) list.item(i));
			paramList.add(param);
		}

		return paramList;
	}

	//This will remove all application tag which has action attributes: uninstall
	public void removeApplications() throws XPathExpressionException {
		Node rootElement = document.getFirstChild();
		if (rootElement != null) { 
			NodeList list = (NodeList) xpath.evaluate("/manifest/application[@action='uninstall']", document, XPathConstants.NODESET);
			for (int i = 0;i < list.getLength();i++) {
				Node node = list.item(i);
				rootElement.removeChild(node);
			}
		}
	}

	//Constraints are the attributes presented in the application tag of manifest.
	//Map should be provided as in sqlite where both key and value should be String object
	//Don't forget to add uninstall="yes" key value pair if you just want to remove the ones that has uninstall tag
	//if constraintMap is null, method will remove all the applications
	public void removeApplicationsByConstraints (HashMap<String, String> constraintMap) throws XPathExpressionException {
		String constaintStr = "";
		if (constraintMap != null) {
			for (Map.Entry<String, String> entry:constraintMap.entrySet()) {
				if (!constaintStr.equals(""))
					constaintStr += " and @" + entry.getKey() + "='" + entry.getValue() + "'";
				else
					constaintStr += "[@" + entry.getKey() + "='" + entry.getValue() + "'";
			}

			if (!constaintStr.equals(""))
				constaintStr += "]";
		}
		Node rootElement = document.getFirstChild();
		NodeList list = (NodeList) xpath.evaluate("/manifest/application" + constaintStr, document, XPathConstants.NODESET);
		for (int i = 0;i < list.getLength();i++) {
			Node node = list.item(i);
			rootElement.removeChild(node);
		}
	}

	//Constraints are the attributes presented in the application tag of manifest.
	//Map should be provided as in sqlite where both key and value should be String object
	//Don't forget to add action="remove" key value pair if you just want to remove the ones that has remove tag
	//if constraintMap is null, method will remove all the applications
	public void removeStatParametersByConstraints (HashMap<String, String> constraintMap) throws XPathExpressionException {
		String constaintStr = "";
		if (constraintMap != null) {
			for (Map.Entry<String, String> entry:constraintMap.entrySet()) {
				if (!constaintStr.equals(""))
					constaintStr += " and @" + entry.getKey() + "='" + entry.getValue() + "'";
				else
					constaintStr += "[@" + entry.getKey() + "='" + entry.getValue() + "'";
			}

			if (!constaintStr.equals(""))
				constaintStr += "]";
		}
		Node statusMonitor = (Node) xpath.evaluate("/manifest/statusmonitor", document, XPathConstants.NODE);
		if (statusMonitor != null) {
			NodeList list = (NodeList) xpath.evaluate("/manifest/statusmonitor/parameter" + constaintStr, document, XPathConstants.NODESET);
			for (int i = 0;i < list.getLength();i++) {
				Node node = list.item(i);
				statusMonitor.removeChild(node);
			}
		}
	}
	
	public void removeAllStatParameters () throws XPathExpressionException {
		Node statusMonitor = (Node) xpath.evaluate("/manifest/statusmonitor", document, XPathConstants.NODE);
		if (statusMonitor != null) {
			NodeList list = (NodeList) xpath.evaluate("/manifest/statusmonitor/parameter[@action='remove']", document, XPathConstants.NODESET);
			for (int i = 0;i < list.getLength();i++) {
				Node node = list.item(i);
				statusMonitor.removeChild(node);
			}
		}
	}

	private void copyParameter(PhoneLabParameter param, Element element) {
		NamedNodeMap map = element.getAttributes();
		for (int i=0;i < map.getLength();i++) {
			Node attr =  map.item(i);
			if (attr.getNodeName().equals("name")) {
				param.setName(attr.getNodeValue());
			} else if (attr.getNodeName().equals("value")) {
				param.setValue(attr.getNodeValue());
			} else if (attr.getNodeName().equals("units")) {
				param.setUnits(attr.getNodeValue());
			} else if (attr.getNodeName().equals("setby")) {
				param.setSetBy(attr.getNodeValue());
			}			
		}
	}

	private void copyApp(PhoneLabApplication app, Element element) {
		NamedNodeMap map = element.getAttributes();
		for (int i=0;i < map.getLength();i++) {
			Node attr =  map.item(i);
			if (attr.getNodeName().equals("intent_name")) {
				app.setIntentName(attr.getNodeValue());
			} else if (attr.getNodeName().equals("package_name")) {
				app.setPackageName(attr.getNodeValue());
			} else if (attr.getNodeName().equals("name")) {
				app.setName(attr.getNodeValue());
			} else if (attr.getNodeName().equals("description")) {
				app.setDescription(attr.getNodeValue());
			} else if (attr.getNodeName().equals("type")) {
				app.setType(attr.getNodeValue());
			} else if (attr.getNodeName().equals("participantinitiated")) {
				if (attr.getNodeValue().equals("yes"))
					app.setParticipantInitiated(true);
				else
					app.setParticipantInitiated(false);
			} else if (attr.getNodeName().equals("download")) {
				app.setDownload(attr.getNodeValue());
			} else if (attr.getNodeName().equals("version")) {
				app.setVersion(attr.getNodeValue());
			} else if (attr.getNodeName().equals("action")) {
				app.setAction(attr.getNodeValue());
			}
		}
	}

	private Document getDocuments(String fileName) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(new File(fileName));
		return document;
	}

	//call this method to save modified manifest.xml file
	public void saveDocument() throws TransformerException {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer transformer = tfactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(new File(xmlFullPath));
		transformer.transform(source, result);
	}
}
