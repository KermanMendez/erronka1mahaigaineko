package model;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UIStyle {
	public static final Color PRIMARY = new Color(33, 150, 243);
	public static final Color SECONDARY = new Color(30, 30, 30);
	public static final Color ACCENT = new Color(255, 193, 7);
	public static final Color BACKGROUND = new Color(245, 245, 245);
	public static final Color BUTTON_BG = new Color(25, 118, 210);
	public static final Color BUTTON_FG = Color.WHITE;
	public static final Color FIELD_BG = Color.WHITE;
	public static final Color FIELD_FG = Color.BLACK;
	public static final Color BORDER_COLOR = new Color(200, 200, 200);

	public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
	public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 16);
	public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
	public static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 15);

	public static void styleButton(JButton button) {
		button.setBackground(BUTTON_BG);
		button.setForeground(BUTTON_FG);
		button.setFont(BUTTON_FONT);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setOpaque(true);
		button.setBorder(javax.swing.BorderFactory.createLineBorder(PRIMARY, 2, true));
	}

	public static void styleLabel(JLabel label, boolean isTitle) {
		label.setFont(isTitle ? TITLE_FONT : LABEL_FONT);
		label.setForeground(SECONDARY);
	}

	public static void stylePanel(JPanel panel) {
		panel.setBackground(BACKGROUND);
		panel.setBorder(javax.swing.BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
	}

	public static void styleField(JComponent comp) {
		comp.setBackground(FIELD_BG);
		comp.setForeground(FIELD_FG);
		comp.setFont(FIELD_FONT);
	}
}
