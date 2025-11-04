package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/**
 * Firebase konexioa kudeatzeko klasea
 */
public class DBConnection {

	private boolean initialized = false;
	private static final int CONNECTION_TIMEOUT_MS = 1000;
	private static final String FIRESTORE_HOST = "firestore.googleapis.com";
	private static final int FIRESTORE_PORT = 443;

	/**
	 * Konexioa hasieratuta dagoen ala ez itzuli
	 * 
	 * @return true hasieratuta badago
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Firebase konexioa hasieratu
	 * 
	 * @param connect konektatu nahi bada
	 * @return true arrakastaz konektatu bada
	 */
	public boolean initialize(Boolean connect) {
		if (!connect) {
			System.out.println("[INFO] Lineaz kanpoko modua - Firebase konexiorik ez");
			return false;
		}

		try {
			// Egiaztatu serviceAccountKey.json fitxategia existitzen dela
			File keyFile = new File("serviceAccountKey.json");
			if (!keyFile.exists()) {
				System.err.println("[ERROR] serviceAccountKey.json fitxategia ez da aurkitu");
				System.err.println("[INFO] Mesedez, ziurtatu fitxategia proiektoaren erro direktorioan dagoela");
				return false;
			}

			// Kargatu kredentzialak
			try (FileInputStream serviceAccount = new FileInputStream(keyFile)) {
				FirebaseOptions options = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

				// Egiaztatu Firestore konexioa proba batekin
				try (Socket socket = new Socket()) {
					socket.connect(new InetSocketAddress(FIRESTORE_HOST, FIRESTORE_PORT), CONNECTION_TIMEOUT_MS);
					System.out.println("[INFO] Firestore konexio proba: arrakasta");
				} catch (IOException ex) {
					System.err.println("[ERROR] Ezin izan da Firestore-era konektatu: " + ex.getMessage());
					System.err.println("[INFO] Egiaztatu internet konexioa eta saiatu berriro");
					return false;
				}

				// Hasieratu FirebaseApp
				if (FirebaseApp.getApps().isEmpty()) {
					FirebaseApp.initializeApp(options);
					System.out.println("[INFO] FirebaseApp hasieratuta");
				} else {
					System.out.println("[INFO] FirebaseApp jadanik hasieratuta dago");
				}

				initialized = true;
				return true;
			}

		} catch (Exception e) {
			System.err.println("[ERROR] Errorea Firebase hasieratzean");
			return false;
		}
	}
}