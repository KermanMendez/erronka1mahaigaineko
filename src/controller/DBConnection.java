package controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
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

	Controller controller = new Controller();
	Firestore db = controller.getDb();

	private static final String API_KEY = "AIzaSyBhHBYyK1vmvbrbP-tWUfFNxRqbeu2AOu4";

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

	public void createUser(String name, String surname1, String surname2, String email, String password,
			String birthdate, Boolean isTrainer) throws Exception {

		CreateRequest request = new CreateRequest().setEmail(email).setEmailVerified(false).setPassword(password)
				.setDisabled(false);

		UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
		System.out.println("Usuario creado: " + userRecord.getUid());

		Controller controller = new Controller();
		Firestore db = controller.getDb();

		DocumentReference uidDoc = db.collection("users").document(userRecord.getUid());
		Map<String, Object> userData = new HashMap<>();
		userData.put("name", name);
		userData.put("email", email);
		userData.put("surname", surname1);
		userData.put("surname2", surname2);
		userData.put("birthdate", birthdate);
		userData.put("isTrainer", isTrainer);

		ApiFuture<WriteResult> writeUser = uidDoc.set(userData);
		System.out.println(uidDoc + " guardado en: " + writeUser.get().getUpdateTime());
	}

	public Boolean eskaeraRegistratu(String izena, String abizena1, String abizena2, String email, String password,
			Date birthdate, Boolean isTrainer) {
		if (email.isEmpty() || password.isEmpty() || izena.isEmpty() || abizena1.isEmpty() || abizena2.isEmpty()
				|| birthdate == null) {
			JOptionPane.showMessageDialog(null, "Datu Guztiak Bete.", "Errorea", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
		String birthdateString = sdf.format(birthdate);

		try {
			createUser(izena, abizena1, abizena2, email, password, birthdateString, isTrainer);
			JOptionPane.showMessageDialog(null, "Registratu zara", "Login", JOptionPane.INFORMATION_MESSAGE);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Errorea registratzen.", "Erregistroa Ezezta",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public void handleLogin(JTextField textFieldUser, JPasswordField passwordField) {
		String email = textFieldUser.getText().trim();
		String password = new String(passwordField.getPassword());

		if (email.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Bete Erabiltzailea eta Pasahitza.", "Login",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		try {
			String uid = checkLogin(email, password);

			if (uid != null) {
				DocumentSnapshot userDoc = db.collection("users").document(uid).get().get();

				if (userDoc.exists()) {

					Boolean isTrainer = userDoc.getBoolean("isTrainer");
					if (isTrainer == null) {
						isTrainer = false;
					}

					Inter inter = new Inter(isTrainer);
					// dispose();
					inter.setVisible(true);
				} else {
					JOptionPane.showMessageDialog(null, "Ez dira erabiltzailearen datuak aurkitu.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(null, "Erabiltzailea edo Pasahitza okerrak.", "Errorea",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Errorea Firebase Authentication-ekin konektatzean.", "Errorea",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private String checkLogin(String email, String password) throws Exception {
		if (API_KEY == null || API_KEY.isEmpty()) {
			throw new IllegalStateException("FIREBASE_API_KEY not set. Set env var or -DFIREBASE_API_KEY=<key>");
		}

		String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;

		OkHttpClient client = new OkHttpClient();

		JsonObject json = new JsonObject();
		json.addProperty("email", email);
		json.addProperty("password", password);
		json.addProperty("returnSecureToken", true);

		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(JSON, json.toString());

		Request request = new Request.Builder().url(url).post(body).build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String responseBody = response.body().string();
				JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

				String uid = responseJson.get("localId").getAsString();
				return uid;
			}
			return null;
		}
	}

}