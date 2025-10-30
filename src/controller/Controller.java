package controller;

import com.google.cloud.firestore.Firestore;

import view.FirstView;

public class Controller {
	private static Firestore firestoreInstantzia;
	private DBConnection dbConnection;
	private Firestore db;
	private FirstView firstView;

	public Controller(Boolean connect) {

		this.dbConnection = new DBConnection();
		db = getFirestore();
		setDbConnection(this.dbConnection);
	}

	public void onOnline() {
		try {
			if (this.dbConnection == null) {
				this.dbConnection = new DBConnection();
			}
			boolean ok = this.dbConnection.initialize(true);
			if (ok) {
				this.db = getFirestore();
			}
			setDbConnection(this.dbConnection);
		} catch (Exception ignored) {
		}
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
					return null;
				}
				firestoreInstantzia = com.google.firebase.cloud.FirestoreClient.getFirestore();
			}
			return firestoreInstantzia;
		} catch (Exception e) {
			return null;
		}
	}
}