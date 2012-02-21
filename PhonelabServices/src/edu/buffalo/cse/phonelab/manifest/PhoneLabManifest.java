/**
 * @author fatih
 * PhoneLab Controller-Manifest Class
 * If any changes are made, then call saveDocument() method to save final version of the manifest.xml
 */

package edu.buffalo.cse.phonelab.manifest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import android.content.Context;
import android.util.Log;

/**
 * This class handles the manifest operations.
 */
public class PhoneLabManifest {
	//TODO Async Thread for this ?
	private Document document = null;
	private XPath xpath = null;
	private String xmlFullPath = null;
	private Context context = null;

	public PhoneLabManifest (String xmlFullPath, Context context) {
		if(xmlFullPath == null || xmlFullPath == "" || context == null)
			new Throwable("NullPointerException");
		this.xmlFullPath = xmlFullPath;
		this.context = context;
	}
	
	@SuppressWarnings("unused") 
	private PhoneLabManifest(){
		//DO Nothing. Meant to be used private constructor to prevent null assignment.
	}

	/**
	 * Method to get manifest. This method make sure that manifest exists. Caller should make sure to handle correctly if return null
	 * @return true if manifest exists otherwise return false
	 */
	public boolean getManifest () {
		try {
			document = getDocuments(xmlFullPath);
			xpath = XPathFactory.newInstance().newXPath();
			if (document != null)
				return true;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessage());
		}

		return false;
	}

	/**
	 * This will return all the application presented in the manifest regardless of any attributes 
	 * @return appList
	 * @throws XPathExpressionException
	 */
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

	/** 
	 * @param constraintMap: should be provided as in sqlite where both key and value should be String object,
	 * Constraints are the attributes presented in the application tag of manifest.
	 * If constraintMap is null, method returns all the applications
	 * @return appList
	 * @throws XPathExpressionException
	 */
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

	/**
	 * Set an application or applications based on the constraintMap
	 * @param constraintMap: map of keys and values for selecting application
	 * @param values: map of keys and values to replace
	 * @throws XPathExpressionException
	 */
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

	/**
	 * 
	 * @param constraintMap: Constraints are the attributes presented in the application tag of manifest.
	 * Map should be provided as in sqlite where both key and value should be String object
	 * If constraintMap is null, method returns all parameter tags
	 * @return paramList
	 * @throws XPathExpressionException
	 */
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

	/**
	 * This will remove all applications 
	 * @throws XPathExpressionException
	 */
	public void removeAllApplications() throws XPathExpressionException {
		Node rootElement = document.getFirstChild();
		if (rootElement != null) { 
			NodeList list = (NodeList) xpath.evaluate("/manifest/application", document, XPathConstants.NODESET);
			for (int i = 0;i < list.getLength();i++) {
				Node node = list.item(i);
				rootElement.removeChild(node);
			}
		}
	}

	/**
	 * Remove an application based on package name
	 * @param packageName
	 * @throws XPathExpressionException
	 */
	public void removeApplication (String packageName) throws XPathExpressionException {
		Node rootElement = document.getFirstChild();
		NodeList list = (NodeList) xpath.evaluate("/manifest/application[@package_name='" + packageName + "']", document, XPathConstants.NODESET);
		for (int i = 0;i < list.getLength();i++) {
			Node node = list.item(i);
			rootElement.removeChild(node);
		}
	}

	/**
	 * Remove a status monitor parameter based on the name attribute
	 * @param name
	 * @throws XPathExpressionException
	 */
	public void removeStatParameters (String name) throws XPathExpressionException {
		Node statusMonitor = (Node) xpath.evaluate("/manifest/statusmonitor", document, XPathConstants.NODE);
		if (statusMonitor != null) {
			NodeList list = (NodeList) xpath.evaluate("/manifest/statusmonitor/parameter[@name='" + name + "']", document, XPathConstants.NODESET);
			for (int i = 0;i < list.getLength();i++) {
				Node node = list.item(i);
				statusMonitor.removeChild(node);
			}
		}
	}

	/**
	 * Add Status Monitor Parameter to manifest
	 * @param param parameter to add
	 * @throws XPathExpressionException
	 */
	public void addStatParameters (PhoneLabParameter param) throws XPathExpressionException {
		Node node = (Node) xpath.evaluate("/manifest/statusmonitor", document, XPathConstants.NODE);
		Element statusElement = null; 
		if (node == null) {//no status monitor
			statusElement = document.createElement("statusmonitor");
			document.getFirstChild().appendChild(statusElement);
		} else {
			statusElement = (Element) node;
		}

		Element newElement = document.createElement("parameter");
		if (param.getUnits() != null)
			newElement.setAttribute("units", param.getUnits());
		if (param.getValue() != null)
			newElement.setAttribute("value", param.getValue());
		if (param.getSetBy() != null)
			newElement.setAttribute("set_by", param.getSetBy());
		statusElement.appendChild(newElement);
	}

	/**
	 * Update Status Monitor Parameter 
	 * @param param parameter to replace
	 * @throws XPathExpressionException
	 */
	public void updateStatParameter (PhoneLabParameter param) throws XPathExpressionException {
		Element statusElement = (Element) xpath.evaluate("/manifest/statusmonitor", document, XPathConstants.NODE);
		Node pNode = (Node) xpath.evaluate("parameter[@name='" + param.getName() +  "']", statusElement, XPathConstants.NODE);
		statusElement.removeChild(pNode);
		Element newElement = document.createElement("parameter");
		if (param.getName() != null)
			newElement.setAttribute("name", param.getName());
		if (param.getUnits() != null)
			newElement.setAttribute("units", param.getUnits());
		if (param.getValue() != null)
			newElement.setAttribute("value", param.getValue());
		if (param.getSetBy() != null)
			newElement.setAttribute("set_by", param.getSetBy());
		statusElement.appendChild(newElement);
	}

	/**
	 * Internal method used to copy from manifest status monitor parameter to PhoneLabParameter class instance
	 * @param param 
	 * @param element
	 */
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

	/**
	 * Internal method used to copy from manifest Application to PhoneLabApplication class instance
	 * @param app
	 * @param element
	 */
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

	/**
	 * Add an application to manifest
	 * @param app
	 */
	public void addApplication (PhoneLabApplication app) {
		Element newElement = document.createElement("application");
		if (app.getPackageName() != null)
			newElement.setAttribute("package_name", app.getPackageName());
		if (app.getName() != null)
			newElement.setAttribute("name", app.getName());
		if (app.getDescription() != null)
			newElement.setAttribute("description", app.getDescription());
		if (app.getType() != null)
			newElement.setAttribute("type", app.getType());
		if (app.getStartTime() != null)
			newElement.setAttribute("start_time", app.getStartTime());
		if (app.getEndTime() != null)
			newElement.setAttribute("end_time", app.getEndTime());
		if (app.getDownload() != null)
			newElement.setAttribute("download", app.getDownload());
		if (app.getVersion() != null)
			newElement.setAttribute("version", app.getVersion());
		if (app.getAction() != null)
			newElement.setAttribute("action", app.getAction());
		document.getFirstChild().appendChild(newElement);
	}

	/**
	 * Update an application in the manifest
	 * @param app app to replace for
	 */
	public void updateApplication (PhoneLabApplication app) {
		try {
			Node node = (Node) xpath.evaluate("/manifest/statusmonitor/parameter[@package_name='" + app.getPackageName() + "']", document, XPathConstants.NODE);
			if (node != null) {//exist 
				Node parentNode = node.getParentNode();
				parentNode.removeChild(node);
				Element newElement = document.createElement("application");
				if (app.getPackageName() != null)
					newElement.setAttribute("package_name", app.getPackageName());
				if (app.getName() != null)
					newElement.setAttribute("name", app.getName());
				if (app.getDescription() != null)
					newElement.setAttribute("description", app.getDescription());
				if (app.getType() != null)
					newElement.setAttribute("type", app.getType());
				if (app.getStartTime() != null)
					newElement.setAttribute("start_time", app.getStartTime());
				if (app.getEndTime() != null)
					newElement.setAttribute("end_time", app.getEndTime());
				if (app.getDownload() != null)
					newElement.setAttribute("download", app.getDownload());
				if (app.getVersion() != null)
					newElement.setAttribute("version", app.getVersion());
				if (app.getAction() != null)
					newElement.setAttribute("action", app.getAction());
				document.getFirstChild().appendChild(newElement);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessage());
		}
	}

	/**
	 * Internal method used to get Document for manifest
	 * @param fileName
	 * @return if exists document if not exist null
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document getDocuments(String fileName) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();

		if (fileName.contains("new_manifest")) {
			File file = new File(fileName);
			if (file.exists()) {
				Document document = builder.parse(file);
				return document;
			} else {
				return null;
			}
		} else {
			try {
				FileInputStream fos = context.openFileInput(fileName);
				Document document = builder.parse(fos);
				fos.close();
				return document;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessage());
			}

			return null;
		}
	}

	/**
	 * Call this method to save modified manifest.xml file  
	 * @throws TransformerException
	 */
	public void saveDocument() throws TransformerException {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer transformer = tfactory.newTransformer();
		DOMSource source = new DOMSource(document);

		if (xmlFullPath.contains("new_manifest")) {
			StreamResult result = new StreamResult(new File(xmlFullPath));
			transformer.transform(source, result);
		} else {
			try {
				FileOutputStream fos = context.openFileOutput(xmlFullPath, Context.MODE_PRIVATE);
				StreamResult result = new StreamResult(fos);
				transformer.transform(source, result);
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("PhoneLab-" + getClass().getSimpleName(), e.getMessage());
			}
		}
	}
}
