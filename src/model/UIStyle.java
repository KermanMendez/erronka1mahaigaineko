package model;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Cursor;

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
	button.setBorderPainted(true);
	button.setContentAreaFilled(true);
	button.setOpaque(true);
	// Padding and rounded border
	button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
		javax.swing.BorderFactory.createLineBorder(PRIMARY, 1, true),
		javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14)));
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
		// Kasu bereziak: JList hautapena hobetu
		if (comp instanceof javax.swing.JList) {
			try {
				javax.swing.JList<?> list = (javax.swing.JList<?>) comp;
				list.setSelectionBackground(ACCENT);
				list.setSelectionForeground(BUTTON_FG);
			} catch (Throwable t) {
				// ez moztu exekuzioa
			}
		}
	}

	public static void styleScrollPane(javax.swing.JScrollPane sp) {
		if (sp == null)
			return;
		sp.setBorder(javax.swing.BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
		if (sp.getViewport() != null) {
			sp.getViewport().setBackground(FIELD_BG);
		}
	}

	/**
	 * Gehitu botoiari hover efektua eta kursorea zentralizatzeko helper-a.
	 * Erabilpena: UIStyle.addHoverEffect(myButton);
	 */
	public static void addHoverEffect(JButton button) {
		if (button == null)
			return;
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				try {
					// subtle brighten on hover and stronger border
					button.setBackground(BUTTON_BG.brighter());
					button.setForeground(BUTTON_FG);
					button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
							javax.swing.BorderFactory.createLineBorder(PRIMARY, 2, true),
							javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14)));
				} catch (Throwable t) {
					// fall back silently
				}
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				try {
					button.setBackground(BUTTON_BG);
					button.setForeground(BUTTON_FG);
					button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
							javax.swing.BorderFactory.createLineBorder(PRIMARY, 1, true),
							javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14)));
				} catch (Throwable t) {
				}
			}
		});
	}

	/**
	 * Estilo para botones icono (back/mini)
	 */
	public static void styleIconButton(JButton button) {
		if (button == null)
			return;
		button.setPreferredSize(new java.awt.Dimension(44, 44));
		button.setBackground(BACKGROUND);
		button.setForeground(PRIMARY);
		button.setFocusPainted(false);
		button.setBorderPainted(true);
		button.setContentAreaFilled(true);
		button.setOpaque(true);
		button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createLineBorder(PRIMARY, 2, true),
				javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6)));
		// hover effect: fill with primary
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				button.setBackground(PRIMARY);
				button.setForeground(BUTTON_FG);
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				button.setBackground(BACKGROUND);
				button.setForeground(PRIMARY);
			}
		});
	}
}
