package model;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Theme {

	private static boolean applied = false;

	public static void apply() {
		if (applied)
			return;
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}

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
			e.printStackTrace();
		}
	}
}
