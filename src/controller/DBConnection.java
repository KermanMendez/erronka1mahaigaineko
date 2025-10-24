package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class DBConnection {

	public boolean initialize(Boolean connect) {
		try {
			File keyFile = new File("serviceAccountKey.json");
			if (!keyFile.exists()) {
				System.out.println("[ERROR] No se encuentra el archivo serviceAccountKey.json.");
				return false;
			}

			// Solo carga el JSON, NO inicializa FirebaseApp todavía
			FileInputStream serviceAccount = new FileInputStream(keyFile);
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

			// Ping rápido a Google antes de inicializar
			try (Socket s = new Socket()) {
				s.connect(new InetSocketAddress("firestore.googleapis.com", 443), 3000); // 3s timeout
			} catch (IOException ex) {
				System.out.println("[INFO] Sin conexión a Firebase.");
				return false; // no internet, no inicializamos
			}

			// Si llegamos aquí, sí hay internet
			FirebaseApp.initializeApp(options);
			return true;

		} catch (Exception e) {
			return false;
		}
	}

}