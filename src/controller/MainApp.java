package controller;

import javax.swing.SwingUtilities;

import model.CreateBackup;
import model.Theme;

public class MainApp {

	public static void main(String[] args) {
		// Aplicar tema ANTES de crear cualquier componente
		Theme.apply();
		
		// Inicializar controller
		Controller.initialize(false);
		Controller controller = Controller.getInstance();

		// Intentar conexión a Firebase PRIMERO (bloquea hasta conectar o fallar)
		try {
			DBConnection dbConnection = new DBConnection();
			boolean connected = dbConnection.initialize(true);

			if (connected) {
				// Asignar la conexión al controller
				controller.setDbConnection(dbConnection);
				controller.setOnline(true);
				System.out.println("[MainApp] Firebase inicializado correctamente");
			} else {
				System.err.println("[MainApp] No se pudo conectar a Firebase");
			}
		} catch (Exception e) {
			System.err.println("[MainApp] Error en conexión: " + e.getMessage());
		}

		// Mostrar UI después de inicializar Firebase
		SwingUtilities.invokeLater(() -> {
			controller.getFirstView(false).setVisible(true);
		});

		// Backup en segundo plano después de mostrar UI
		Thread backupThread = new Thread(() -> {
			try {
				// Esperar 2 segundos antes de hacer backup
				Thread.sleep(2000);
				
				// Verificar que el controller tiene DB antes de hacer backup
				if (controller.getDb() != null && controller.isOnline()) {
					CreateBackup backup = new CreateBackup();
					backup.saveBackup(true);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				System.err.println("[MainApp] Error en backup: " + e.getMessage());
			}
		}, "BackupThread");
		backupThread.setPriority(Thread.MIN_PRIORITY);
		backupThread.setDaemon(true);
		backupThread.start();
	}
}