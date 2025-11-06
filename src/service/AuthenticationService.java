package service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Map;

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
import service.BackupReaderService.BackupData;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.DateUtils;
import util.ExceptionHandler;
import util.PasswordUtils;
import util.ValidationUtils;
import view.Inter;

public class AuthenticationService {
	private static final String DEFAULT_API_KEY = "AIzaSyBhHBYyK1vmvbrbP-tWUfFNxRqbeu2AOu4";
	private static final String API_KEY = System.getenv("FIREBASE_API_KEY") != null ? System.getenv("FIREBASE_API_KEY")
			: DEFAULT_API_KEY;
	private static final OkHttpClient HTTP_BEZEROA = new OkHttpClient();
	private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");
	private DateUtils dateUtils = new DateUtils();

	public Boolean eskaeraRegistratu(String izena, String abizena1, String abizena2, String email, String password,
			Date birthdate, Boolean trainer, Boolean connect) {

		// Balidatu datuak ValidationUtils erabiliz
		String erroreMezua = ValidationUtils.balidatuErregistroa(izena, abizena1, abizena2, email, password, password,
				birthdate);

		if (erroreMezua != null) {
			ExceptionHandler.erakutsiErrorea("Balidazio errorea", erroreMezua);
			return false;
		}

		String birthdateString = dateUtils.formatDate(birthdate);

		try {
			createUser(izena, abizena1, abizena2, email, password, birthdateString, trainer, connect);
			ExceptionHandler.erakutsiInfo("Erregistroa", "Ondo erregistratu zara. Orain saioa hasi dezakezu.");
			return true;
		} catch (Exception ex) {
			System.err.println("[ERROR] Errorea erregistroa sortzean");

			if (ex.getMessage() != null && ex.getMessage().contains("EMAIL_EXISTS")) {
				ExceptionHandler.erakutsiErrorea("Erregistroa ezin izan da egin",
						"Email helbide hau jadanik erregistratuta dago. Saioa hasi edo beste bat erabili.");
			} else {
				ExceptionHandler.kudeatu(ExceptionHandler.ErrorMota.SISTEMA_ERROREA, ex);
			}
			return false;
		}
	}

	public String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return PasswordUtils.hashPasahitza(password);
	}

	public void createUser(String name, String surname1, String surname2, String email, String password,
			String birthdate, Boolean trainer, Boolean connect) throws Exception {

		Controller controller = Controller.getInstance();
		Firestore db = controller.getDb();
		int level = 1;

		if (trainer) {
			level = 5;
		}

		String hashedPassword = hashPassword(password);

		CreateRequest request = new CreateRequest().setEmail(email).setEmailVerified(false).setPassword(password)
				.setDisabled(false);

		UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

		DocumentReference uidDoc = db.collection("users").document(userRecord.getUid());
		Map<String, Object> erabiltzaileDatuak = Map.of("name", name, "surname", surname1, "surname2", surname2,
				"email", email, "birthdate", birthdate, "trainer", trainer, "password", hashedPassword, "level", level);

		uidDoc.set(erabiltzaileDatuak);
	}

	public String handleLogin(JTextField textFieldUser, JPasswordField passwordField, Boolean connect) {
		String email = textFieldUser.getText().trim();
		String password = new String(passwordField.getPassword());

		// Controller singleton erabili
		Controller controller = Controller.getInstance();
		Firestore db = controller.getDb();

		// Balidatu sarrera datuak
		if (ValidationUtils.testuaHutsik(email) || ValidationUtils.testuaHutsik(password)) {
			ExceptionHandler.erakutsiAbisua("Datuak falta dira", "Erabiltzailea eta pasahitza bete behar dituzu.");
			return null;
		}

		try {
			if (connect && db != null) {
				// Online (DB eskuragarri badago soilik)
				String uid = checkLogin(email, password);
				if (uid == null) {
					ExceptionHandler.erakutsiErrorea("Autentifikazio errorea",
							"Erabiltzailea edo pasahitza ez dira zuzenak. Egiaztatu eta saiatu berriro.");
					return null;
				}

				DocumentSnapshot userDoc = db.collection("users").document(uid).get().get();
				if (!userDoc.exists()) {
					ExceptionHandler.erakutsiErrorea("Errorea",
							"Ezin izan dira erabiltzailearen datuak aurkitu datu-basean.");
					return null;
				}

				Boolean entrenatzaileaDa = userDoc.getBoolean("trainer");
				if (entrenatzaileaDa == null)
					entrenatzaileaDa = false;

				// Konexio egoera erreala pasatu, ez entrenatzaile eremua
				Inter inter = new Inter(connect);
				inter.setVisible(true);

				UserBackupService createUserBackup = new UserBackupService();
				createUserBackup.saveEmail(email);

				return email;

			} else {
				// Offline
				BackupData backup = BackupReaderService.loadBackupSafe();
				if (backup == null)
					return null;

				for (var entry : backup.collections.entrySet()) {
					for (BackupReaderService.DocumentData doc : entry.getValue()) {
						String userEmail = doc.fields.get("email");
						if (userEmail == null || !userEmail.equals(email)) {
							continue;
						}

						String storedPassword = doc.fields.get("password");
						if (storedPassword == null || !storedPassword.contains("$")) {
							continue;
						}

						if (!PasswordUtils.egiaztaturPasahitza(password, storedPassword)) {
							ExceptionHandler.erakutsiErrorea("Autentifikazio errorea",
									"Erabiltzailea edo pasahitza ez dira zuzenak.");
							return null;
						}

						Inter inter = new Inter(connect);
						inter.setVisible(true);

						UserBackupService createUserBackup = new UserBackupService();
						createUserBackup.saveEmail(email);

						return email;
					}
				}

				ExceptionHandler.erakutsiErrorea("Erabiltzailea ez da aurkitu",
						"Ez dago erabiltzailerik email horrekin. Lehenik erregistratu behar duzu.");
				return null;
			}

		} catch (Exception ex) {
			System.err.println("[ERROR] Errorea login prozesuan");
			ExceptionHandler.kudeatu(ExceptionHandler.ErrorMota.AUTENTIFIKAZIO_ERROREA, ex);
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

			OfflineHistoricService offlineHandler = new OfflineHistoricService();
			try {
				offlineHandler.sinkronizatuLineazKanpoDBra(true);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseJson.get("localId").getAsString();
		}
	}
}