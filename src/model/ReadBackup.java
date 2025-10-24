package model;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReadBackup {

	private final String FICHERO = "backup.xml";
	private final byte CLAVE = 0x5A;

	public static class UserData {
		public String uid;
		public String email;

		public UserData(String uid, String email) {
			this.uid = uid;
			this.email = email;
		}

		@Override
		public String toString() {
			return "UserData{uid='" + uid + "', email='" + email + "'}";
		}
	}

	public static class DocumentData {
		public String id;
		public Map<String, String> fields = new HashMap<>();
		public Map<String, List<DocumentData>> subcollections = new HashMap<>();

		@Override
		public String toString() {
			return "DocumentData{id='" + id + "', fields=" + fields + ", subcollections=" + subcollections + "}";
		}
	}

	public static class BackupData {
		public List<UserData> users = new ArrayList<>();
		public Map<String, List<DocumentData>> collections = new HashMap<>();

		@Override
		public String toString() {
			return "BackupData{users=" + users + ", collections=" + collections + "}";
		}
	}

	private String xorDecrypt(String base64Text) {
		if (base64Text == null || base64Text.isEmpty())
			return "";

		byte[] encryptedBytes = Base64.getDecoder().decode(base64Text);
		byte[] result = new byte[encryptedBytes.length];
		for (int i = 0; i < encryptedBytes.length; i++) {
			result[i] = (byte) (encryptedBytes[i] ^ CLAVE);
		}
		return new String(result);
	}

	private static String getTagValue(String tag, Element element) {
		NodeList nodeList = element.getElementsByTagName(tag);
		if (nodeList.getLength() > 0) {
			Node node = nodeList.item(0);
			return node.getTextContent();
		}
		return "";
	}
	
	public BackupData loadBackupData() {
		File file = new File(FICHERO);
		if (!file.exists() || file.length() == 0) {
			System.err.println("No se encontró el archivo " + FICHERO);
			return null;
		}

		BackupData backup = new BackupData();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(FICHERO);
			doc.getDocumentElement().normalize();

			NodeList users = doc.getElementsByTagName("user");
			for (int i = 0; i < users.getLength(); i++) {
				Node userNode = users.item(i);
				if (userNode.getNodeType() == Node.ELEMENT_NODE) {
					Element userElement = (Element) userNode;
					String uid = xorDecrypt(getTagValue("uid", userElement));
					String email = xorDecrypt(getTagValue("email", userElement));
					backup.users.add(new UserData(uid, email));
				}
			}

			NodeList collections = doc.getElementsByTagName("collection");
			for (int i = 0; i < collections.getLength(); i++) {
				Element collectionElement = (Element) collections.item(i);
				String collectionName = collectionElement.getAttribute("name");
				List<DocumentData> documents = parseDocuments(collectionElement);
				backup.collections.put(collectionName, documents);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return backup;
	}

	private List<DocumentData> parseDocuments(Element parentElement) {
		List<DocumentData> documentsList = new ArrayList<>();
		NodeList documents = parentElement.getElementsByTagName("document");

		for (int i = 0; i < documents.getLength(); i++) {
			Node docNode = documents.item(i);

			if (docNode.getParentNode() != parentElement)
				continue;

			if (docNode.getNodeType() == Node.ELEMENT_NODE) {
				Element docElement = (Element) docNode;
				DocumentData documentData = new DocumentData();
				documentData.id = docElement.getAttribute("id");

				NodeList children = docElement.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node child = children.item(j);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						Element field = (Element) child;
						if (field.getNodeName().equals("subcollection")) {
							String subName = field.getAttribute("name");
							documentData.subcollections.put(subName, parseDocuments(field));
						} else {
							String key = field.getNodeName();
							String decryptedValue = xorDecrypt(field.getTextContent());
							documentData.fields.put(key, decryptedValue);
						}
					}
				}
				documentsList.add(documentData);
			}
		}
		return documentsList;
	}
	
	public void readBackup() {
	    ReadBackup reader = new ReadBackup();
	    ReadBackup.BackupData backup = reader.loadBackupData();

	    if (backup != null) {
	        System.out.println("Usuarios:");
	        for (ReadBackup.UserData u : backup.users) {
	            System.out.println("  UID: " + u.uid + ", Email: " + u.email);
	        }

	        System.out.println("\nColecciones:");
	        backup.collections.forEach((name, docs) -> {
	            System.out.println("📂 " + name);
	            for (ReadBackup.DocumentData d : docs) {
	                System.out.println("  📝 Doc ID: " + d.id);
	                d.fields.forEach((k, v) -> System.out.println("     - " + k + ": " + v));

	                d.subcollections.forEach((subname, subdocs) -> {
	                    System.out.println("     🔁 Subcolección: " + subname);
	                    for (ReadBackup.DocumentData subDoc : subdocs) {
	                        System.out.println("        🧩 " + subDoc.id + " → " + subDoc.fields);
	                    }
	                });
	            }
	        });
	    }
	}

}
