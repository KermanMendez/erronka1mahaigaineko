package controller;

import model.ConnectDB;

public class MainApp {

	public static void main(String[] args) {
		
		ConnectDB connectDB = new ConnectDB();

		controller.AppState.setAppStarted(true);
		Controller controller = new Controller();
		new Thread(() -> {
			connectDB.saveBackupToXML();
		}).start();
		controller.getFirstView().setVisible(true);
	}
}