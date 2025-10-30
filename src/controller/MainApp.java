package controller;

import javax.swing.SwingUtilities;

import model.CreateBackup;

public class MainApp {

	public static void main(String[] args) {
		controller.AppState.setAppStarted(true);

		Controller controller = new Controller(false);

		SwingUtilities.invokeLater(() -> {
			controller.getFirstView(false).setVisible(true);
		});

		new Thread(() -> {
			DBConnection dbConnection = new DBConnection();
			boolean connected = dbConnection.initialize(true);
			if (connected) {
				controller.onOnline();

				CreateBackup backup = new CreateBackup();
				new Thread(() -> {
					backup.saveBackup(true);
				}).start();
			}
		}).start();
	}
}