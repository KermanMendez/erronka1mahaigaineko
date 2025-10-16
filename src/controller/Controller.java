package controller;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import view.FirstView;

public class Controller {

	private FirstView firstView;
	private DBConnection dbConnection;
	private Firestore db;

	public Controller() {
		firstView = new FirstView();
		setDbConnection(new DBConnection());
		DBConnection.initialize();
		db = FirestoreClient.getFirestore();
	}

	public Firestore getDb() {
		return db;
	}

	public FirstView getFirstView() {
		return firstView;
	}

	public DBConnection getDbConnection() {
		return dbConnection;
	}

	public void setDbConnection(DBConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
}