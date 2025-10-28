package controller;

import model.CreateBackup;

public class MainApp {

	private static Boolean connect = true;

	public static void main(String[] args) {
		controller.AppState.setAppStarted(true);
		DBConnection dbConnection = new DBConnection();
		connect = dbConnection.initialize(connect);
		Controller controller = new Controller(connect);

		if (connect) {
			CreateBackup backup = new CreateBackup();
			new Thread(() -> {
				backup.saveBackupToXML(connect);
			}).start();
		}

		controller.getFirstView(connect).setVisible(true);
	}
}