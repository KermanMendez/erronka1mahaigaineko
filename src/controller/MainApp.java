package controller;

import javax.swing.SwingUtilities;

import model.CreateBackup;

public class MainApp {

	public static void main(String[] args) {

		Controller.initialize(false);
		Controller controller = Controller.getInstance();

		SwingUtilities.invokeLater(() -> {
			controller.getFirstView(false).setVisible(true);
		});

		new Thread(() -> {
			DBConnection dbConnection = new DBConnection();
			boolean connected = dbConnection.initialize(true);

			if (connected) {
				controller.setOnline(true);

				SwingUtilities.invokeLater(() -> {
					controller.getFirstView(true);
				});

				CreateBackup backup = new CreateBackup();
				new Thread(() -> {
					backup.saveBackup(true);
				}).start();
			}
		}).start();
	}
}