package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;

import controller.Controller;

public class CreateBackup {
	private Controller controller = new Controller();
	Firestore db = controller.getDb();

	private final String FICHERO = "backup.xml";
	private final byte CLAVE = 0x5A;

	private byte[] xorBytes(byte[] data) {
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (byte) (data[i] ^ CLAVE);
		}
		return result;
	}

	public void saveBackupToXML() {
		if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
			System.err.println("[ERROR] FirebaseApp no está inicializado. No se puede hacer backup a XML.");
			return;
		}
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("backup");
			doc.appendChild(rootElement);
			Element usersElement = doc.createElement("users");
			rootElement.appendChild(usersElement);
			ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
			for (ExportedUserRecord user : page.getValues()) {
				Element userElement = doc.createElement("user");
				usersElement.appendChild(userElement);
				Element uid = doc.createElement("uid");
				uid.appendChild(doc.createTextNode(user.getUid()));
				userElement.appendChild(uid);
				Element email = doc.createElement("email");
				email.appendChild(doc.createTextNode(user.getEmail() != null ? user.getEmail() : ""));
				userElement.appendChild(email);
			}
			Iterable<CollectionReference> collections = db.listCollections();
			for (CollectionReference collection : collections) {
				Element collectionElement = doc.createElement("collection");
				collectionElement.setAttribute("name", collection.getId());
				rootElement.appendChild(collectionElement);
				addDocumentsToXML(collection, collectionElement, doc);
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(baos);
			transformer.transform(source, result);

			byte[] encryptedData = xorBytes(baos.toByteArray());

			try (FileOutputStream fos = new FileOutputStream(FICHERO)) {
				fos.write(encryptedData);
			}

			System.out.println("Backup saved to " + FICHERO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadBackupFromXML() {
		File file = new File(FICHERO);
		if (!file.exists() || file.length() == 0) {
			System.err.println("[ERROR] No backup file found or file is empty.");
			return;
		}

		try {
			byte[] fileBytes = Files.readAllBytes(Paths.get(FICHERO));
			byte[] decryptedData = xorBytes(fileBytes);

			ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			docBuilder.parse(bais);
			
			System.out.println("Backup loaded successfully from " + FICHERO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addDocumentsToXML(CollectionReference collection, Element parentElement, Document doc)
			throws Exception {
		ApiFuture<QuerySnapshot> future = collection.get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			Element documentElement = doc.createElement("document");
			documentElement.setAttribute("id", document.getId());
			parentElement.appendChild(documentElement);
			Map<String, Object> data = document.getData();
			for (Map.Entry<String, Object> entry : data.entrySet()) {
				Element field = doc.createElement(entry.getKey());
				field.appendChild(doc.createTextNode(entry.getValue().toString()));
				documentElement.appendChild(field);
			}

			Iterable<CollectionReference> subcollections = document.getReference().listCollections();
			for (CollectionReference subcollection : subcollections) {
				Element subcollectionElement = doc.createElement("subcollection");
				subcollectionElement.setAttribute("name", subcollection.getId());
				documentElement.appendChild(subcollectionElement);
				addDocumentsToXML(subcollection, subcollectionElement, doc);
			}
		}
	}
}