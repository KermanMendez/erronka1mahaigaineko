package controller;

import model.CreateBackup;

public class MainApp {
	public static void main(String[] args) {
		CreateBackup backup = new CreateBackup();
		controller.AppState.setAppStarted(true);
		Controller controller = new Controller();
		new Thread(() -> {
			backup.saveBackupToXML();
		}).start();
		controller.getFirstView().setVisible(true);
	}
}