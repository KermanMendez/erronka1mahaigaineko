package controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

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
	
	public boolean createUser(String username, String email, String plainPassword) throws Exception {
		Firestore db = FirestoreClient.getFirestore();
		CollectionReference usersCol = db.collection("users");

		if (db.collection("users").document(username).get().get().exists()) {
			return false;
		}

		String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

		ApiFuture<WriteResult> writeUser = usersCol.document(username).set(new java.util.HashMap<>() {
			private static final long serialVersionUID = 1L;

			{
				put("username", username);
				put("email", email);
				put("password", hashedPassword);
			}
		});
		return writeUser.get() != null;
	}
}