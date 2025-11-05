package controller;

import javax.swing.SwingUtilities;

import service.BackupService;
import view.Theme;

//Aplikazioaren sarrera puntua
public class MainApp {

	public static void main(String[] args) {
		// Aplikatu itxura tema
		Theme.apply();

		// Hasieratu kontrolatzailea
		Controller.initialize(false); // Hasiera azkarragoa
		Controller controller = Controller.getInstance();

		// Konektatu Firebase-ra
		try {
			DBConnection dbConnection = new DBConnection();
			boolean connected = dbConnection.initialize(true);

			if (connected) {
				controller.setDbConnection(dbConnection);
				controller.setOnline(true);
				System.out.println("[INFO] Firebase-ra konektatuta");
			} else {
				System.err.println("[ABISUA] Ezin izan da Firebase-ra konektatu. Lineaz kanpoko modua erabiliko da.");
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Errorea konexioan");
		}

		// Erakutsi lehenengo leihoa
		SwingUtilities.invokeLater(() -> {
			// Usar el estado real de conexión del controller
			controller.getFirstView(controller.isOnline()).setVisible(true);
		});

		// Sortu backup-a atzeko planoan (daemon thread)
		Thread backupThread = new Thread(() -> {
			try {
				if (controller.getDb() != null && controller.isOnline()) {
					System.out.println("[INFO] Backup automatikoa hasten...");
					BackupService backup = new BackupService();
					backup.saveBackup(true);
					System.out.println("[INFO] Backup-a arrakastaz gordeta");
				} else {
					System.out.println("[INFO] Lineaz kanpo - backup-ik ez da sortuko");
				}
			} catch (Exception e) {
				System.err.println("[ERROR] Errorea backup-a sortzean");
			}
		}, "BackupThread");
		backupThread.setPriority(Thread.MIN_PRIORITY);
		backupThread.setDaemon(true);
		backupThread.start();
	}
}