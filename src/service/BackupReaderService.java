package service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.CryptoUtils;

public class BackupReaderService {

	private final String FICHERO = "backup.dat";
	private CryptoUtils cryptoUtils = new CryptoUtils();

	/**
	 * Backup-a modu seguruan kargatzen du, null itzultzen du errorea badago Carga
	 * el backup de forma segura, retornando null si hay error
	 * 
	 * Metodo laguntzaile estatikoa erabilera orokorrerako Método helper estático
	 * para uso común
	 * 
	 * @return BackupData edo null ez badago edo errorea badago / BackupData o null
	 *         si no existe o hay error
	 */
	public static BackupData loadBackupSafe() {
		try {
			BackupReaderService reader = new BackupReaderService();
			return reader.loadBackupData();
		} catch (Exception e) {
			System.err.println("[ERROR] Error cargando backup: " + e.getMessage());
			return null;
		}
	}

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

	public class DocumentData {
		public String id;
		public Map<String, String> fields = new HashMap<>();
		public Map<String, List<DocumentData>> subcollections = new HashMap<>();

		@Override
		public String toString() {
			return "DocumentData{id='" + id + "', fields=" + fields + ", subcollections=" + subcollections + "}";
		}
	}

	public class BackupData {
		public List<UserData> users = new ArrayList<>();
		public Map<String, List<DocumentData>> collections = new HashMap<>();

		@Override
		public String toString() {
			return "BackupData{users=" + users + ", collections=" + collections + "}";
		}
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
		File datFile = new File("backup.dat");
		if (!datFile.exists() || datFile.length() == 0) {
			File file = new File(FICHERO);
			if (!file.exists() || file.length() == 0) {
				System.err.println("[ERROR] Ez dago backup fitxategirik eskuragarri");
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
						String uid = cryptoUtils.xorDecrypt(getTagValue("uid", userElement));
						String email = cryptoUtils.xorDecrypt(getTagValue("email", userElement));
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
				System.err.println("[ERROR] Errorea backup-a irakurtzerakoan");
				return null;
			}

			System.out.println("[INFO] Backup XML kargatuta");
			return backup;
		}

		try {
			byte[] fileBytes = Files.readAllBytes(Paths.get("backup.dat"));
			byte[] decrypted = cryptoUtils.xorBytes(fileBytes);
			try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decrypted))) {
				Object obj = ois.readObject();

				if (!(obj instanceof List)) {
					System.err.println("[ERROR] Backup formatua ez da zuzena");
					return null;
				}

				@SuppressWarnings("unchecked")
				List<String> lines = (List<String>) obj;
				BackupData backup = new BackupData();

				String currentCollection = null;
				BackupReaderService.DocumentData currentDoc = null;
				for (int i = 0; i < lines.size(); i++) {
					String raw = lines.get(i);
					String line = raw.replaceAll("^\\s+", "");
					if (line.startsWith("USER_UID:")) {
						String v = line.substring("USER_UID:".length());
						String uid = cryptoUtils.xorDecrypt(v);
						String email = "";
						if (i + 1 < lines.size()) {
							String next = lines.get(i + 1).replaceAll("^\\s+", "");
							if (next.startsWith("USER_EMAIL:")) {
								i++;
								email = cryptoUtils.xorDecrypt(next.substring("USER_EMAIL:".length()));
							}
						}
						backup.users.add(new UserData(uid, email));
						continue;
					}

					if (line.startsWith("COLLECTION:")) {
						currentCollection = line.substring("COLLECTION:".length());
						backup.collections.putIfAbsent(currentCollection, new ArrayList<>());
						currentDoc = null;
						continue;
					}

					if (line.startsWith("DOCUMENT_ID:")) {
						String id = line.substring("DOCUMENT_ID:".length());
						currentDoc = new DocumentData();
						currentDoc.id = id;
						backup.collections.get(currentCollection).add(currentDoc);
						continue;
					}

					if (line.startsWith("FIELD:") && currentDoc != null) {
						String kv = line.substring("FIELD:".length());
						int eq = kv.indexOf('=');
						if (eq > 0) {
							String key = kv.substring(0, eq);
							String valEnc = kv.substring(eq + 1);
							String val = cryptoUtils.xorDecrypt(valEnc);
							currentDoc.fields.put(key, val);
						}
						continue;
					}

					if (line.startsWith("SUBCOLLECTION:")) {
						String subName = line.substring("SUBCOLLECTION:".length());
						int baseIndent = raw.indexOf(line);
						int j = i + 1;
						List<String> subLines = new ArrayList<>();
						for (; j < lines.size(); j++) {
							String r2 = lines.get(j);
							int ind2 = r2.indexOf(r2.replaceAll("^\\s+", ""));
							if (ind2 <= baseIndent)
								break;
							subLines.add(r2.substring(baseIndent + 2));
						}
						List<DocumentData> subDocs = parseDocumentsFromLines(subLines);
						if (currentDoc != null) {
							currentDoc.subcollections.put(subName, subDocs);
						}
						i = j - 1;
						continue;
					}
				}

				System.out.println("[INFO] Backup-a ondo kargatuta");
				return backup;
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Errorea backup-a desenkriptatzerakoan");
			return null;
		}
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
							String decryptedValue = cryptoUtils.xorDecrypt(field.getTextContent());
							documentData.fields.put(key, decryptedValue);
						}
					}
				}
				documentsList.add(documentData);
			}
		}
		return documentsList;
	}

	private List<DocumentData> parseDocumentsFromLines(List<String> lines) {
		List<DocumentData> docs = new ArrayList<>();
		DocumentData current = null;
		for (int i = 0; i < lines.size(); i++) {
			String raw = lines.get(i);
			String line = raw.replaceAll("^\\s+", "");
			if (line.startsWith("DOCUMENT_ID:")) {
				current = new DocumentData();
				current.id = line.substring("DOCUMENT_ID:".length());
				docs.add(current);
			} else if (line.startsWith("FIELD:") && current != null) {
				String kv = line.substring("FIELD:".length());
				int eq = kv.indexOf('=');
				if (eq > 0) {
					String key = kv.substring(0, eq);
					String valEnc = kv.substring(eq + 1);
					String val = cryptoUtils.xorDecrypt(valEnc);
					current.fields.put(key, val);
				}
			}
		}
		return docs;
	}
}