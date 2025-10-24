package controller;

import model.CreateBackup;

public class MainApp {
	public static void main(String[] args) {
		Boolean connect = false;
		CreateBackup backup = new CreateBackup();
		controller.AppState.setAppStarted(true);
		Controller controller = new Controller();
		if (DBConnection.initialize()) {
			connect = true;
		}
		new Thread(() -> {
			backup.saveBackupToXML();
		}).start();
		controller.getFirstView(connect).setVisible(true);
	}
}