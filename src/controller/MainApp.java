package controller;

import model.CreateBackup;

public class MainApp {

	private static Boolean connect = true;

	public static void main(String[] args) {
		controller.AppState.setAppStarted(true);
		DBConnection dbConnection = new DBConnection();
		connect = dbConnection.initialize(connect);
		System.out.println(connect);
		Controller controller = new Controller(connect);

		if (connect) {
			CreateBackup backup = new CreateBackup();
			System.out.println(connect);
			new Thread(() -> {
				backup.saveBackupToXML(connect);
			}).start();
		}

		controller.getFirstView(connect).setVisible(true);
	}
}