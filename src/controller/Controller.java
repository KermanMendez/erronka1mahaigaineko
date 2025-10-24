package controller;

import com.google.cloud.firestore.Firestore;

import view.FirstView;

public class Controller {
	private static Firestore firestoreInstantzia;
	private DBConnection dbConnection;
	private Firestore db;
	private FirstView firstView;

	public Controller() {
		DBConnection.initialize();
		db = getFirestore();
		setDbConnection(new DBConnection(this));
	}

	public DBConnection getDbConnection() {
		return dbConnection;
	}

	public Firestore getDb() {
		return db;
	}

	public FirstView getFirstView(Boolean connect) {
		if (firstView == null) {
			firstView = new FirstView(connect);
		}
		return firstView;
	}

	public void setDbConnection(DBConnection dbConnection) {
		this.dbConnection = dbConnection;
	}

	private static Firestore getFirestore() {
		try {
			if (firestoreInstantzia == null) {
				if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
					System.out.println("[ERROR] FirebaseApp no está inicializado. Firestore no disponible.");
					return null;
				}
				firestoreInstantzia = com.google.firebase.cloud.FirestoreClient.getFirestore();
			}
			return firestoreInstantzia;
		} catch (Exception e) {
			System.out.println("[ERROR] No se pudo obtener Firestore");
			return null;
		}
	}
}