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

	private boolean initialized = false;

	public boolean isInitialized() {
		return initialized;
	}

	public boolean initialize(Boolean connect) {
		try {
			File keyFile = new File("serviceAccountKeya.json");
			if (!keyFile.exists()) {
				System.err.println("[DBConnection] serviceAccountKey.json aurkitu ezina");
				return false;
			}

			try (FileInputStream serviceAccount = new FileInputStream(keyFile)) {
				FirebaseOptions options = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

				try (Socket s = new Socket()) {
					s.connect(new InetSocketAddress("firestore.googleapis.com", 443), 3000);
				} catch (IOException ex) {
					System.err.println("[DBConnection] Ezin izan da Firestore-era konektatu: " + ex.getMessage());
					return false;
				}

				if (FirebaseApp.getApps().isEmpty()) {
					FirebaseApp.initializeApp(options);
				}
				initialized = true;
				return true;
			}

		} catch (Exception e) {
			System.err.println("[DBConnection] Hasieratzean errorea: " + e.getMessage());
			return false;
		}
	}

}