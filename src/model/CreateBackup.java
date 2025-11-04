package model;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;

import controller.Controller;
import util.CryptoUtils;

public class CreateBackup {

	private final String FICHERO = "backup.dat";
	private Firestore db;
	private CryptoUtils cryptoUtils = new CryptoUtils();

	public void saveBackup(Boolean connect) {

		if (connect) {
			if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
				System.err.println("[ERROR] FirebaseApp ez dago hasieratuta. Ezin da backup-ik egin.");
				return;
			}

			Controller controller = Controller.getInstance();
			db = controller.getDb();

			if (db == null) {
				System.err.println("[ERROR] Firestore DB ez dago eskuragarri. Ezin da backup-ik egin.");
				return;
			}

			try {
				List<String> lines = new ArrayList<>();

				ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
				for (ExportedUserRecord user : page.getValues()) {
					lines.add("USER_UID:" + cryptoUtils.xorEncrypt(user.getUid()));
					lines.add("USER_EMAIL:" + cryptoUtils.xorEncrypt(user.getEmail() != null ? user.getEmail() : ""));
				}

				Iterable<CollectionReference> collections = db.listCollections();
				List<HistoricRecord> historicList = new ArrayList<>();
				for (CollectionReference collection : collections) {
					lines.add("COLLECTION:" + collection.getId());
					addDocumentsToDat(collection, lines, "", historicList);
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(lines);
				oos.close();
				byte[] encrypted = cryptoUtils.xorBytes(baos.toByteArray());
				try (FileOutputStream fos = new FileOutputStream(FICHERO)) {
					fos.write(encrypted);
				}

				if (!historicList.isEmpty()) {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document histDoc = dBuilder.newDocument();
					Element rootElement = histDoc.createElement("historicBackup");
					histDoc.appendChild(rootElement);

					for (HistoricRecord hr : historicList) {
						Element userElem = histDoc.createElement("user");
						userElem.setAttribute("uid", hr.userId);
						rootElement.appendChild(userElem);

						for (Map.Entry<String, String> e : hr.fields.entrySet()) {
							Element field = histDoc.createElement(e.getKey());
							field.appendChild(histDoc.createTextNode(e.getValue()));
							userElem.appendChild(field);
						}
					}

					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
					DOMSource source = new DOMSource(histDoc);
					StreamResult result2 = new StreamResult(new FileOutputStream("historic.xml"));
					transformer.transform(source, result2);
				}

				System.out.println("[INFO] Backup-a ondo gordeta: " + FICHERO);
			} catch (Exception e) {
				System.err.println("[ERROR] Errorea backup-a gordetzean");
			}
		}
	}

	private void addDocumentsToDat(CollectionReference collection, List<String> lines, String indent,
			List<HistoricRecord> historicList) throws Exception {
		ApiFuture<QuerySnapshot> future = collection.get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();

		for (QueryDocumentSnapshot document : documents) {
			lines.add(indent + "DOCUMENT_ID:" + document.getId());

			Map<String, Object> data = document.getData();
			for (Map.Entry<String, Object> entry : data.entrySet()) {
				String value = entry.getValue() != null ? entry.getValue().toString() : "";
				lines.add(indent + "FIELD:" + entry.getKey() + "=" + cryptoUtils.xorEncrypt(value));
			}

			Iterable<CollectionReference> subcollections = document.getReference().listCollections();
			for (CollectionReference subcollection : subcollections) {
				if (collection != null && "users".equalsIgnoreCase(collection.getId())
						&& "historic".equalsIgnoreCase(subcollection.getId())) {
					ApiFuture<QuerySnapshot> hf = subcollection.get();
					List<QueryDocumentSnapshot> hdocs = hf.get().getDocuments();
					for (QueryDocumentSnapshot hd : hdocs) {
						HistoricRecord hr = new HistoricRecord();
						hr.userId = document.getId();
						hr.fields = new java.util.LinkedHashMap<>();
						Map<String, Object> hdata = hd.getData();
						for (Map.Entry<String, Object> he : hdata.entrySet()) {
							String val = he.getValue() != null ? he.getValue().toString() : "";
							hr.fields.put(he.getKey(), val);
						}
						historicList.add(hr);
					}
					continue;
				}
				lines.add(indent + "SUBCOLLECTION:" + subcollection.getId());
				addDocumentsToDat(subcollection, lines, indent + "  ", historicList);
			}
		}
	}

	private class HistoricRecord {
		String userId;
		Map<String, String> fields;
	}
}
