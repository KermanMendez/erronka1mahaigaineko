package model;

import java.util.Date;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controller.Controller;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import view.Inter;

public class ConnectDB {

	private static final String API_KEY = "AIzaSyBhHBYyK1vmvbrbP-tWUfFNxRqbeu2AOu4";
	private static final OkHttpClient HTTP_BEZEROA = new OkHttpClient();
	private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");
	private static final java.text.SimpleDateFormat DATA_FORMATUA = new java.text.SimpleDateFormat("dd/MM/yyyy");
	private Controller controller = new Controller();
	Firestore db = controller.getDb();

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
			JOptionPane.showMessageDialog(null,
					"Erabiltzaile hau registratuta dago. Saioa hasi edo beste email bat erabili", "Erregistroa Ezezta",
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
}