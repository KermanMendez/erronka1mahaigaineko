package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * XML eragiketetarako utilitateak
 */
public class XMLUtils {

	/**
	 * XML fitxategia parseatu eta Document itzultzen du
	 * 
	 * @param fileName XML fitxategiaren izena
	 * @return Parseaturiko dokumentua edo null errorea badago
	 */
	public static Document parseXmlDocument(String fileName) {
		File file = new File(fileName);
		if (!file.exists() || file.length() == 0) {
			return null;
		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println("[ERROR] Error parseando XML: " + fileName);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Existitzen den XML fitxategia parseatu edo berria sortzen du ez badago
	 * 
	 * @param fileName    Fitxategiaren izena
	 * @param rootElement Erro elementuaren izena berria sortu behar bada
	 * @return Parseaturiko edo berri sortutako dokumentua
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document parseOrCreateXmlDocument(String fileName, String rootElement)
			throws ParserConfigurationException, SAXException, IOException {
		File file = new File(fileName);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		if (file.exists() && file.length() > 0) {
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			return doc;
		} else {
			Document doc = builder.newDocument();
			Element root = doc.createElement(rootElement);
			doc.appendChild(root);
			return doc;
		}
	}

	/**
	 * Erro elementu batekin XML dokumentu berria sortzen du
	 * 
	 * @param rootElement Erro elementuaren izena
	 * @return Dokumentu berria
	 * @throws ParserConfigurationException
	 */
	public static Document createNewDocument(String rootElement) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element root = doc.createElement(rootElement);
		doc.appendChild(root);
		return doc;
	}

	/**
	 * XML dokumentua formatu indentatuarekin fitxategi batean gordetzen du
	 * 
	 * @param doc      Gordetzeko dokumentua
	 * @param fileName Helmuga fitxategiaren izena
	 * @throws TransformerException
	 * @throws IOException
	 */
	public static void saveXmlDocument(Document doc, String fileName) throws TransformerException, IOException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		DOMSource source = new DOMSource(doc);
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			StreamResult result = new StreamResult(fos);
			transformer.transform(source, result);
		}
	}

	/**
	 * Konfiguratutako DocumentBuilder lortzen du
	 * Obtiene el DocumentBuilder configurado
	 * 
	 * @return DocumentBuilder
	 * @throws ParserConfigurationException
	 */
	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		return factory.newDocumentBuilder();
	}

	/**
	 * Tag izenaren arabera seme elementu baten testu balioa lortzen du
	 * 
	 * @param tag     Bilatzeko tag-aren izena
	 * @param element Guraso elementua
	 * @return Elementuaren testua edo kate hutsa ez badago
	 */
	public static String getTagValue(String tag, Element element) {
		org.w3c.dom.NodeList nodeList = element.getElementsByTagName(tag);
		if (nodeList.getLength() > 0) {
			org.w3c.dom.Node node = nodeList.item(0);
			return node.getTextContent();
		}
		return "";
	}
}
