package controller;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import view.FirstView;

public class Controller {

	FirstView firstView;
	DBConnection dbConnection;
	Firestore db;

	public Controller() {
		firstView = new FirstView();
		dbConnection = new DBConnection();
		DBConnection.initialize();
		db = FirestoreClient.getFirestore();
	}

	public Firestore getDb() {
		return db;
	}

	public FirstView getFirstView() {
		return firstView;
	}
}