package controller;

import java.io.File;
import java.io.FileInputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class DBConnection {
	public DBConnection(Controller controller) {
	}

	public static Boolean initialize() {
		try {
			File keyFile = new File("serviceAccountKey.json");
			if (!keyFile.exists()) {
				System.out.println(
						"[ERROR] No se encuentra el archivo serviceAccountKey.json. Firebase no se inicializará.");
				return false;
			}
			FileInputStream serviceAccount = new FileInputStream(keyFile);

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

			if (options == null) {
				System.out.println("Error al cargar las credenciales de Firebase");
				return false;
			}

			FirebaseApp.initializeApp(options);
			return true;
		} catch (Exception e) {
		}
		return true;
	}
}