package model;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
import model.ReadBackup.BackupData;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.DateUtils;
import view.Inter;

public class ConnectDB {
	private static final String DEFAULT_API_KEY = "AIzaSyBhHBYyK1vmvbrbP-tWUfFNxRqbeu2AOu4";
	private static final String API_KEY = System.getenv("FIREBASE_API_KEY") != null ? System.getenv("FIREBASE_API_KEY")
			: DEFAULT_API_KEY;
	private static final OkHttpClient HTTP_BEZEROA = new OkHttpClient();
	private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

	private ReadBackup reader = new ReadBackup();

	public Boolean eskaeraRegistratu(String izena, String abizena1, String abizena2, String email, String password,
			Date birthdate, Boolean isTrainer, Boolean connect) {
		if (email.isEmpty() || password.isEmpty() || izena.isEmpty() || abizena1.isEmpty() || abizena2.isEmpty()
				|| birthdate == null) {
			JOptionPane.showMessageDialog(null, "Datu Guztiak Bete.", "Errorea", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		String birthdateString = DateUtils.formatDate(birthdate);
		try {
			createUser(izena, abizena1, abizena2, email, password, birthdateString, isTrainer, connect);
			JOptionPane.showMessageDialog(null, "Registratu zara", "Login", JOptionPane.INFORMATION_MESSAGE);
			return true;
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null,
					"Erabiltzaile hau registratuta dago edo errore batekin aurkitu da. Saioa hasi edo beste email bat erabili",
					"Erregistroa Ezezta", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = 65536;
		int keyLength = 256;
		byte[] salt = new byte[16];
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(salt);

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		byte[] hash = skf.generateSecret(spec).getEncoded();

		return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
	}

	public void createUser(String name, String surname1, String surname2, String email, String password,
			String birthdate, Boolean isTrainer, Boolean connect) throws Exception {

		Controller controller = Controller.getInstance();
		Firestore db = controller.getDb();
		int level = 1;

		if (isTrainer) {
			level = 5;
		}

		String hashedPassword = hashPassword(password);

		CreateRequest request = new CreateRequest().setEmail(email).setEmailVerified(false).setPassword(password)
				.setDisabled(false);

		UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

		DocumentReference uidDoc = db.collection("users").document(userRecord.getUid());
		Map<String, Object> erabiltzaileDatuak = Map.of("name", name, "surname", surname1, "surname2", surname2,
				"email", email, "birthdate", birthdate, "isTrainer", isTrainer, "password", hashedPassword, "level",
				level);

		uidDoc.set(erabiltzaileDatuak);
	}

	public String handleLogin(JTextField textFieldUser, JPasswordField passwordField, Boolean connect) {
		String email = textFieldUser.getText().trim();
		String password = new String(passwordField.getPassword());

		// Usar el Controller singleton en lugar de crear uno nuevo
		Controller controller = Controller.getInstance();
		Firestore db = controller.getDb();

		if (email.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Bete Erabiltzailea eta Pasahitza.", "Login",
					JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		try {
			if (connect && db != null) {
				// Online (solo si DB está disponible)
				String uid = checkLogin(email, password);
				if (uid == null) {
					JOptionPane.showMessageDialog(null, "Erabiltzailea edo Pasahitza okerrak.", "Errorea",
							JOptionPane.ERROR_MESSAGE);
					return null;
				}

				DocumentSnapshot userDoc = db.collection("users").document(uid).get().get();
				if (!userDoc.exists()) {
					JOptionPane.showMessageDialog(null, "Ez dira erabiltzailearen datuak aurkitu.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return null;
				}

				Boolean entrenatzaileaDa = userDoc.getBoolean("isTrainer");
				if (entrenatzaileaDa == null)
					entrenatzaileaDa = false;

				Inter inter = new Inter(entrenatzaileaDa);
				inter.setVisible(true);

				CreateUserBackup createUserBackup = new CreateUserBackup();
				createUserBackup.saveEmail(email);

				return email;

			} else {
				// Offline
				BackupData backup = reader.loadBackupData();
				if (backup == null)
					return null;

				for (var entry : backup.collections.entrySet()) {
					for (ReadBackup.DocumentData doc : entry.getValue()) {
						String userEmail = doc.fields.get("email");
						if (userEmail == null || !userEmail.equals(email)) {
							continue;
						}

						String storedPassword = doc.fields.get("password");
						if (storedPassword == null || !storedPassword.contains("$")) {
							continue;
						}

						String[] parts = storedPassword.split("\\$");
						if (parts.length != 2)
							continue;

						byte[] salt = Base64.getDecoder().decode(parts[0]);
						byte[] hashStored = Base64.getDecoder().decode(parts[1]);

						PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
						SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
						byte[] hashAttempt = skf.generateSecret(spec).getEncoded();

						boolean valid = hashStored.length == hashAttempt.length;
						for (int i = 0; i < hashStored.length && valid; i++) {
							if (hashStored[i] != hashAttempt[i])
								valid = false;
						}

						if (!valid) {
							JOptionPane.showMessageDialog(null, "Erabiltzailea edo Pasahitza okerrak.", "Errorea",
									JOptionPane.ERROR_MESSAGE);
							return null;
						}

						Inter inter = new Inter(connect);
						inter.setVisible(true);

						CreateUserBackup createUserBackup = new CreateUserBackup();
						createUserBackup.saveEmail(email);

						return email;
					}
				}

				JOptionPane.showMessageDialog(null, "Erabiltzailea ez da aurkitu.", "Errorea",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Errorea login prozesuan.", "Errorea", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			return null;
		}
	}

	private String checkLogin(String email, String password) throws Exception {
		String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
		JsonObject json = new JsonObject();
		json.addProperty("email", email);
		json.addProperty("password", password);
		json.addProperty("returnSecureToken", true);
		RequestBody body = RequestBody.create(JSON_MEDIA, json.toString());
		Request request = new Request.Builder().url(url).post(body).build();
		try (Response response = HTTP_BEZEROA.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				return null;
			}
			String responseBody = response.body().string();
			JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

			OfflineHistoric offlineHandler = new OfflineHistoric();
			try {
				offlineHandler.sinkronizatuLineazKanpoDBra(true);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseJson.get("localId").getAsString();
		}
	}
}