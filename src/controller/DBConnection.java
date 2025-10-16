package controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.google.auth.oauth2.GoogleCredentials;
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
				// System.out.println("Firebase ya está inicializado");
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

	public void createUser(String email, String password) throws Exception {
		CreateRequest request = new CreateRequest().setEmail(email).setEmailVerified(false).setPassword(password)
				.setDisabled(false);

		UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
		System.out.println("Usuario creado: " + userRecord.getUid());
	}
}