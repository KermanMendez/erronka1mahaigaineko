package controller;

public class AppState {
    private static boolean appStarted = false;

    public static boolean isAppStarted() {
        return appStarted;
    }

    public static void setAppStarted(boolean started) {
        appStarted = started;
    }
}
