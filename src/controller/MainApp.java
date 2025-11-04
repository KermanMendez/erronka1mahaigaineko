package controller;

import javax.swing.SwingUtilities;

import model.CreateBackup;
import model.Theme;

public class MainApp {

	public static void main(String[] args) {
		Theme.apply();

		Controller.initialize(false);
		Controller controller = Controller.getInstance();

		try {
			DBConnection dbConnection = new DBConnection();
			boolean connected = dbConnection.initialize(true);

			if (connected) {
				controller.setDbConnection(dbConnection);
				controller.setOnline(true);
				System.out.println("[MainApp] Firebase-ra konektatuta");
			} else {
				System.err.println("[MainApp] Ezin da posible Firebase-ra konektatzea");
			}
		} catch (Exception e) {
			System.err.println("[MainApp] Error en conexión: " + e.getMessage());
		}

		SwingUtilities.invokeLater(() -> {
			controller.getFirstView(false).setVisible(true);
		});

		Thread backupThread = new Thread(() -> {
			try {
				if (controller.getDb() != null && controller.isOnline()) {
					CreateBackup backup = new CreateBackup();
					backup.saveBackup(true);
				}
			} catch (Exception e) {
				System.err.println("[MainApp] Errorea backup-a sortzean");
			}
		}, "BackupThread");
		backupThread.setPriority(Thread.MIN_PRIORITY);
		backupThread.setDaemon(true);
		backupThread.start();
	}
}