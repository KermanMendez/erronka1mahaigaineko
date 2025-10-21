package controller;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
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
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import view.Inter;

public class DBConnection {

	Controller controller;
	Firestore db;

	private static final String API_KEY = "AIzaSyBhHBYyK1vmvbrbP-tWUfFNxRqbeu2AOu4";
	private static final OkHttpClient HTTP_BEZEROA = new OkHttpClient();
	private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");
	private static final java.text.SimpleDateFormat DATA_FORMATUA = new java.text.SimpleDateFormat("dd/MM/yyyy");

	public DBConnection(Controller controller) {
		this.controller = controller;
		this.db = controller.getDb();
	}

	public static void initialize() {
		try {
			List<FirebaseApp> apps = FirebaseApp.getApps();
			if (!apps.isEmpty()) {
				return;
			}

			FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

			FirebaseApp.initializeApp(options);
			System.out.println("Firebase conectado con éxito");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean eskaeraRegistratu(String izena, String abizena1, String abizena2, String email, String password,
			Date birthdate, Boolean isTrainer) {
		if (email.isEmpty() || password.isEmpty() || izena.isEmpty() || abizena1.isEmpty() || abizena2.isEmpty()
				|| birthdate == null) {
			JOptionPane.showMessageDialog(null, "Datu Guztiak Bete.", "Errorea", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		String birthdateString = DATA_FORMATUA.format(birthdate);

		try {
			createUser(izena, abizena1, abizena2, email, password, birthdateString, isTrainer);
			JOptionPane.showMessageDialog(null, "Registratu zara", "Login", JOptionPane.INFORMATION_MESSAGE);
			return true;
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Erabiltzaile hau registratuta dago. Saioa hasi edo beste email bat erabili", "Erregistroa Ezezta",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public void createUser(String name, String surname1, String surname2, String email, String password,
			String birthdate, Boolean isTrainer) throws Exception {

		CreateRequest request = new CreateRequest().setEmail(email).setEmailVerified(false).setPassword(password)
				.setDisabled(false);

		UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

		DocumentReference uidDoc = db.collection("users").document(userRecord.getUid());
		Map<String, Object> erabiltzaileDatuak = Map.of("name", name, "email", email, "surname", surname1, "surname2",
				surname2, "birthdate", birthdate, "isTrainer", isTrainer);

		uidDoc.set(erabiltzaileDatuak);
	}

	public Boolean handleLogin(JTextField textFieldUser, JPasswordField passwordField) {
		String email = textFieldUser.getText().trim();
		String password = new String(passwordField.getPassword());

		if (email.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Bete Erabiltzailea eta Pasahitza.", "Login",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}

		try {
			String uid = checkLogin(email, password);
			if (uid == null) {
				JOptionPane.showMessageDialog(null, "Erabiltzailea edo Pasahitza okerrak.", "Errorea",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			DocumentSnapshot erabiltzaileDoc = db.collection("users").document(uid).get().get();
			if (!erabiltzaileDoc.exists()) {
				JOptionPane.showMessageDialog(null, "Ez dira erabiltzailearen datuak aurkitu.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			Boolean entrenatzaileaDa = erabiltzaileDoc.getBoolean("isTrainer");
			if (entrenatzaileaDa == null) {
				entrenatzaileaDa = false;
			}

			Inter inter = new Inter(entrenatzaileaDa);
			inter.setVisible(true);
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Errorea Firebase Authentication-ekin konektatzean.", "Errorea",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private String checkLogin(String email, String password) throws Exception {
		if (API_KEY == null || API_KEY.isEmpty()) {
			throw new IllegalStateException("FIREBASE_API_KEY not set. Set env var or -DFIREBASE_API_KEY=<key>");
		}

		String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;

		JsonObject json = new JsonObject();
		json.addProperty("email", email);
		json.addProperty("password", password);
		json.addProperty("returnSecureToken", true);

		RequestBody body = RequestBody.create(JSON_MEDIA, json.toString());
		Request request = new Request.Builder().url(url).post(body).build();

		try (Response response = HTTP_BEZEROA.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String responseBody = response.body().string();
				JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
				return responseJson.get("localId").getAsString();
			}
			return null;
		}
	}

	public void saveBackupToXML() {
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
			StreamResult result = new StreamResult(new FileWriter("backup.xml"));

			transformer.transform(source, result);

			System.out.println("Backup saved to backup.xml");
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

			Iterable<CollectionReference> subCollections = document.getReference().listCollections();
			for (CollectionReference subCollection : subCollections) {
				Element subCollectionElement = doc.createElement("subcollection");
				subCollectionElement.setAttribute("name", subCollection.getId());
				documentElement.appendChild(subCollectionElement);

				addDocumentsToXML(subCollection, subCollectionElement, doc);
			}
		}
	}
}