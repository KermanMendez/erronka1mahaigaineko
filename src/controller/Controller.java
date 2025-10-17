package controller;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

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
		if (firestoreInstantzia == null) {
			firestoreInstantzia = FirestoreClient.getFirestore();
		}
		return firestoreInstantzia;
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