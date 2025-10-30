package model;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import controller.Controller;

public class OfflineHistoric {

	private final String FILE = "offlineHistoric.xml";

	private synchronized void appendToHistoricXml(String uid, Map<String, Object> data) throws Exception {
		File f = new File("historic.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc;
		Element root;
		if (f.exists() && f.length() > 0) {
			doc = dBuilder.parse(f);
			doc.getDocumentElement().normalize();
			root = doc.getDocumentElement();
		} else {
			doc = dBuilder.newDocument();
			root = doc.createElement("historicBackup");
			doc.appendChild(root);
		}

		Element userElem = doc.createElement("user");
		if (uid != null)
			userElem.setAttribute("uid", uid);

		for (Map.Entry<String, Object> e : data.entrySet()) {
			Element field = doc.createElement(e.getKey());
			String val = e.getValue() != null ? String.valueOf(e.getValue()) : "";
			field.appendChild(doc.createTextNode(val));
			userElem.appendChild(field);
		}

		root.appendChild(userElem);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		try (FileOutputStream fos = new FileOutputStream(f)) {
			StreamResult result = new StreamResult(fos);
			transformer.transform(source, result);
		}
	}

	public synchronized void addEntry(String uid, String email, Map<String, String> fields) {
		try {
			File f = new File(FILE);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc;
			Element root;
			if (f.exists() && f.length() > 0) {
				doc = dBuilder.parse(f);
				doc.getDocumentElement().normalize();
				root = doc.getDocumentElement();
			} else {
				doc = dBuilder.newDocument();
				root = doc.createElement("historicBackup");
				doc.appendChild(root);
			}

			Element userElem = doc.createElement("user");
			if (uid != null)
				userElem.setAttribute("uid", uid);
			if (email != null)
				userElem.setAttribute("email", email);

			for (Map.Entry<String, String> e : fields.entrySet()) {
				Element field = doc.createElement(e.getKey());
				field.appendChild(doc.createTextNode(e.getValue() != null ? e.getValue() : ""));
				userElem.appendChild(field);
			}

			root.appendChild(userElem);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			try (FileOutputStream fos = new FileOutputStream(f)) {
				StreamResult result = new StreamResult(fos);
				transformer.transform(source, result);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized boolean syncOfflineToDb(Boolean connect) {
		if (connect == null || !connect) {
			return false;
		}

		boolean allSynced = true;
		try {
			Controller controller = Controller.getInstance();
			Firestore db = controller.getDb();
			if (db == null) {
				return false;
			}

			File f = new File(FILE);
			if (!f.exists() || f.length() == 0) {
				return true; // No hay nada que sincronizar
			}

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);
			doc.getDocumentElement().normalize();

			NodeList users = doc.getElementsByTagName("user");
			List<Integer> syncedIndexes = new ArrayList<>();

			for (int i = 0; i < users.getLength(); i++) {
				org.w3c.dom.Node userNode = users.item(i);
				if (userNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
					continue;
				}
				Element userElem = (Element) userNode;
				String emailAttr = userElem.getAttribute("email");

				String email = emailAttr != null && !emailAttr.isEmpty() ? emailAttr : null;
				Map<String, Object> data = new HashMap<>();

				org.w3c.dom.NodeList children = userElem.getChildNodes();
				for (int c = 0; c < children.getLength(); c++) {
					org.w3c.dom.Node ch = children.item(c);
					if (ch.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
						continue;
					Element fe = (Element) ch;
					String key = fe.getTagName();
					String val = fe.getTextContent();

					if (val != null && val.matches("^-?\\d+$")) {
						try {
							data.put(key, Integer.parseInt(val));
						} catch (NumberFormatException nfe) {
							data.put(key, val);
						}
					} else if (val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")
							|| val.equalsIgnoreCase("bai") || val.equalsIgnoreCase("ez"))) {
						if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("bai"))
							data.put(key, true);
						else
							data.put(key, false);
					} else {
						data.put(key, val);
					}
				}

				String userDocId = null;
				if (email != null) {
					QuerySnapshot userQuery = db.collection("users").whereEqualTo("email", email).get().get();
					if (!userQuery.isEmpty()) {
						userDocId = userQuery.getDocuments().get(0).getId();
					}
				}

				if (userDocId != null) {
					try {
						CollectionReference history = db.collection("users").document(userDocId).collection("historic");
						DocumentReference newDoc = history.document();
						ApiFuture<WriteResult> future = newDoc.set(data);
						future.get(); // Esperar a que la operación se complete

						try {
							appendToHistoricXml(userDocId, data);
							syncedIndexes.add(i);
						} catch (Exception e) {
							System.err.println("Error appending to historic.xml: " + e.getMessage());
							allSynced = false;
						}
					} catch (InterruptedException | ExecutionException e) {
						System.err.println("Error al sincronizar con Firestore: " + e.getMessage());
						allSynced = false;
						continue;
					}
				} else {
					System.err.println("No se pudo encontrar el usuario para sincronizar");
					allSynced = false;
				}
			}

			if (!syncedIndexes.isEmpty()) {
				Document newDoc = dBuilder.newDocument();
				Element newRoot = newDoc.createElement("historicBackup");
				newDoc.appendChild(newRoot);

				for (int i = 0; i < users.getLength(); i++) {
					if (syncedIndexes.contains(i))
						continue;
					org.w3c.dom.Node userNode = users.item(i);
					org.w3c.dom.Node imported = newDoc.importNode(userNode, true);
					newRoot.appendChild(imported);
				}

				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(newDoc);
				try (FileOutputStream fos = new FileOutputStream(f)) {
					StreamResult result = new StreamResult(fos);
					transformer.transform(source, result);
				}
			}

			Document checkDoc = dBuilder.parse(f);
			checkDoc.getDocumentElement().normalize();
			if (!checkDoc.getDocumentElement().hasChildNodes()) {
				f.delete();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return allSynced;
	}

}
