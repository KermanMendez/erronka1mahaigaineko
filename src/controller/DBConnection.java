package controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

public class DBConnection {

	public static void initialize() {
		try {
			List<FirebaseApp> apps = FirebaseApp.getApps();
			if (!apps.isEmpty()) {
				return;
			}

			FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();

			FirebaseApp.initializeApp(options);
			System.out.println("Firebase conectado con éxito");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createUser(String email, String password) throws Exception {
		String name = "Test User";
		String surname = "Test Surname";
		String surname2 = "Test Surname2";
		String birthdate = "01/01/2000";

		CreateRequest request = new CreateRequest()
				.setEmail(email)
				.setEmailVerified(false)
				.setPassword(password)
				.setDisabled(false);

		UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
		System.out.println("Usuario creado: " + userRecord.getUid());

		Controller controller = new Controller();
		Firestore db = controller.getDb();

		DocumentReference uidDoc = db.collection("users").document(userRecord.getUid());
		Map<String, Object> routineData = new HashMap<>();
		routineData.put("name", name);
		routineData.put("surname", surname);
		routineData.put("surname2", surname2);
		routineData.put("birthdate", birthdate);

		ApiFuture<WriteResult> writeRoutine = uidDoc.set(routineData);
		System.out.println(uidDoc + " guardada en: " + writeRoutine.get().getUpdateTime());
	}
}