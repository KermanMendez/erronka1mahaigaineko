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
				return false;
			}

			FileInputStream serviceAccount = new FileInputStream(keyFile);
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

			try (Socket s = new Socket()) {
				s.connect(new InetSocketAddress("firestore.googleapis.com", 443), 3000);
			} catch (IOException ex) {
				return false;
			}

			FirebaseApp.initializeApp(options);
			return true;

		} catch (Exception e) {
			return false;
		}
	}

}