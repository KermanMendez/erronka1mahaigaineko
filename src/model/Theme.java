package model;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Gestor del tema visual de la aplicación (Nimbus Look and Feel)
 * Thread-safe con double-checked locking
 */
public class Theme {

	private static volatile boolean applied = false;

	/**
	 * Aplica el tema Nimbus a toda la aplicación
	 * Solo se ejecuta una vez (thread-safe)
	 */
	public static void apply() {
		// Double-checked locking para mejor rendimiento
		if (applied) {
			return;
		}
		
		synchronized (Theme.class) {
			if (applied) {
				return;
			}
			
			try {
				// Buscar y aplicar Nimbus Look and Feel
				for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}

				// Aplicar colores personalizados de UIStyle
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
	
	/**
	 * Verifica si el tema ya ha sido aplicado
	 */
	public static boolean isApplied() {
		return applied;
	}
}
