package view;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Aplikazioaren itxura tema kudeatzailea (Nimbus Look and Feel) Gestor del tema
 * visual de la aplicación (Nimbus Look and Feel) Thread-safe double-checked
 * locking erabiliz
 */
public class Theme {

	private static volatile boolean applied = false;

	/**
	 * Nimbus tema aplikatu aplikazio osoari Behin bakarrik exekutatzen da
	 * (thread-safe)
	 */
	public static void apply() {
		// Double-checked locking errendimendu hoberako
		if (applied) {
			return;
		}

		synchronized (Theme.class) {
			if (applied) {
				return;
			}

			try {
				// Nimbus Look and Feel bilatu eta aplikatu
				for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}

				// UIStyle-ko kolore pertsonalizatuak aplikatu
				UIManager.put("control", UIStyle.BACKGROUND);
				UIManager.put("info", UIStyle.BACKGROUND);
				UIManager.put("nimbusBase", UIStyle.PRIMARY);
				UIManager.put("nimbusBlueGrey", UIStyle.SECONDARY);
				UIManager.put("text", UIStyle.FIELD_FG);

				UIManager.put("Button.background", UIStyle.BUTTON_BG);
				UIManager.put("Button.foreground", UIStyle.BUTTON_FG);
				UIManager.put("Button.font", UIStyle.BUTTON_FONT);

				UIManager.put("Label.font", UIStyle.LABEL_FONT);
				UIManager.put("TextField.font", UIStyle.FIELD_FONT);
				UIManager.put("ComboBox.font", UIStyle.FIELD_FONT);
				UIManager.put("List.font", UIStyle.FIELD_FONT);

				applied = true;

			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				System.err.println("[ERROR] Errorea tema aplikatzerakoan");
			}
		}
	}

	/**
	 * Egiaztatu tema jadanik aplikatuta dagoen
	 * 
	 * @return true aplikatuta badago
	 */
	public static boolean isApplied() {
		return applied;
	}
}
