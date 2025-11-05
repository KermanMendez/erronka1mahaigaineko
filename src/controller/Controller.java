package controller;

import com.google.cloud.firestore.Firestore;

import view.FirstView;

/**
 * Aplikazioaren egoera globalaren kudeatzailea (Singleton) Controlador del
 */
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
		this.online = false;
	}

	/**
	 * Online egoerara pasatzean deitzen da
	 */
	public void onOnline() {
		try {
			if (this.dbConnection != null && this.dbConnection.isInitialized()) {
				this.db = getFirestore();
				System.out.println("[INFO] Controller online moduan");
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Errorea online modura pasatzean: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public DBConnection getDbConnection() {
		return dbConnection;
	}

	public Firestore getDb() {
		// db null bada baina online gaude, Firestore lortu
		if (db == null && online && dbConnection != null && dbConnection.isInitialized()) {
			db = getFirestore();
		}
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

	/**
	 * Konexio egoera online den egiaztatu
	 * 
	 * @return true online badago
	 */
	public boolean isOnline() {
		return online && dbConnection != null && dbConnection.isInitialized();
	}

	/**
	 * Firestore instantzia lortu
	 * 
	 * @return Firestore instantzia edo null ez badago
	 */
	private static Firestore getFirestore() {
		try {
			if (firestoreInstantzia == null) {
				if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
					System.err.println("[ERROR] FirebaseApp ez dago hasieratuta");
					return null;
				}
				firestoreInstantzia = com.google.firebase.cloud.FirestoreClient.getFirestore();
				System.out.println("[INFO] Firestore instantzia sortuta");
			}
			return firestoreInstantzia;
		} catch (Exception e) {
			System.err.println("[ERROR] Errorea Firestore instantzia sortzean");
			return null;
		}
	}
}