package controller;

import com.google.cloud.firestore.Firestore;

import view.FirstView;

public class Controller {
	private static Controller instance;
	private static Firestore firestoreInstantzia;
	private DBConnection dbConnection;
	private Firestore db;
	private FirstView firstView;
	private boolean online;

	public static Controller getInstance() {
		if (instance == null) {
			instance = new Controller(false);
		}
		return instance;
	}

	public static void initialize(boolean online) {
		if (instance == null) {
			instance = new Controller(online);
		} else {
			instance.setOnline(online);
		}
	}

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
		} catch (Exception e) {
			e.printStackTrace();
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

	public void setOnline(boolean online) {
		this.online = online;
		if (online) {
			onOnline();
		}
	}

	public boolean isOnline() {
		return online && dbConnection != null && dbConnection.isInitialized();
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