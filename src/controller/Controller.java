package controller;

import com.google.cloud.firestore.Firestore;

import view.FirstView;

public class Controller {

	private FirstView firstView;
	private DBConnection dbConnection;
	private Firestore db;
	private static Firestore firestoreInstantzia;

	public Controller() {
		DBConnection.initialize();
		db = getFirestore();
		setDbConnection(new DBConnection(this));
	}

	private static Firestore getFirestore() {
		try {
			if (firestoreInstantzia == null) {
				// Solo intentar obtener Firestore si FirebaseApp está inicializado
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

	public Firestore getDb() {
		return db;
	}

	public FirstView getFirstView() {
		if (firstView == null) {
			firstView = new FirstView();
		}
		return firstView;
	}

	public DBConnection getDbConnection() {
		return dbConnection;
	}

	public void setDbConnection(DBConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
}