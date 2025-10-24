package model;

import java.io.FileOutputStream;
import java.util.Base64;
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

	private final String FICHERO = "backup.xml";
	private final byte CLAVE = 0x5A;
	private Firestore db;

	private String xorEncrypt(String text) {
		byte[] data = text.getBytes();
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (byte) (data[i] ^ CLAVE);
		}
		return Base64.getEncoder().encodeToString(result);
	}

	public void saveBackupToXML(Boolean connect) {

		if (connect) {
			if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
				System.err.println("[ERROR] FirebaseApp no está inicializado. No se puede hacer backup a XML.");
				return;
			}
			
			Controller controller = new Controller(connect);
			db = controller.getDb();

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
					uid.appendChild(doc.createTextNode(xorEncrypt(user.getUid())));
					userElement.appendChild(uid);

					Element email = doc.createElement("email");
					email.appendChild(doc.createTextNode(xorEncrypt(user.getEmail() != null ? user.getEmail() : "")));
					userElement.appendChild(email);
				}

				// === COLECCIONES FIRESTORE ===
				Iterable<CollectionReference> collections = db.listCollections();
				for (CollectionReference collection : collections) {
					Element collectionElement = doc.createElement("collection");
					collectionElement.setAttribute("name", collection.getId());
					rootElement.appendChild(collectionElement);
					addDocumentsToXML(collection, collectionElement, doc);
				}

				// === GUARDADO ===
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new FileOutputStream(FICHERO));
				transformer.transform(source, result);

				System.out.println("Backup guardado en " + FICHERO);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
				String value = entry.getValue() != null ? entry.getValue().toString() : "";
				field.appendChild(doc.createTextNode(xorEncrypt(value)));
				documentElement.appendChild(field);
			}

			// Subcolecciones recursivas
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
