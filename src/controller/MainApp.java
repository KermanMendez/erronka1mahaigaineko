package controller;

public class MainApp {

	public static void main(String[] args) {
		controller.AppState.setAppStarted(true);
		Controller controller = new Controller();
		new Thread(() -> {
			controller.getDbConnection().saveBackupToXML();
		}).start();
		controller.getFirstView().setVisible(true);
	}
}