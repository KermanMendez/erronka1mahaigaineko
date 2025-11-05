package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;

public class UIStyle {

	public static class RoundedBorder extends AbstractBorder {
		private static final long serialVersionUID = 1L;
		private final Color color;
		private final int radius;

		public RoundedBorder(Color color, int radius) {
			this.color = color;
			this.radius = radius;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(color);
			g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
			g2d.dispose();
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(8, 16, 8, 16);
		}
	}

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
		button.setContentAreaFilled(false);
		button.setOpaque(false);

		button.setMinimumSize(new java.awt.Dimension(160, 40));
		button.setPreferredSize(new java.awt.Dimension(160, 40));

		button.setBorder(new RoundedBorder(BUTTON_BG, 10));

		button.addPropertyChangeListener("border", evt -> button.repaint());
		button.addPropertyChangeListener("background", evt -> button.repaint());

		button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				if (button.getModel().isPressed()) {
					g2d.setColor(button.getBackground().darker());
				} else {
					g2d.setColor(button.getBackground());
				}

				g2d.fill(new RoundRectangle2D.Float(1, 1, c.getWidth() - 2, c.getHeight() - 2, 10, 10));
				g2d.dispose();

				super.paint(g, c);
			}
		});
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
		if (comp instanceof javax.swing.JList) {
			try {
				javax.swing.JList<?> list = (javax.swing.JList<?>) comp;
				list.setSelectionBackground(ACCENT);
				list.setSelectionForeground(BUTTON_FG);
			} catch (Throwable t) {
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

	public static void addHoverEffect(JButton button) {
		if (button == null)
			return;
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				try {
					button.setBackground(ACCENT);
					button.setForeground(Color.BLACK);
					button.setBorder(new RoundedBorder(ACCENT, 10));
				} catch (Throwable t) {
				}
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				try {
					button.setBackground(BUTTON_BG);
					button.setForeground(BUTTON_FG);
					button.setBorder(new RoundedBorder(BUTTON_BG, 10));
				} catch (Throwable t) {
				}
			}
		});
	}

	public static void styleIconButton(JButton button) {
		if (button == null)
			return;
		button.setMinimumSize(new java.awt.Dimension(48, 48));
		button.setPreferredSize(new java.awt.Dimension(48, 48));
		button.setMaximumSize(new java.awt.Dimension(48, 48));

		button.setBackground(BACKGROUND);
		button.setForeground(PRIMARY);
		button.setFocusPainted(false);
		button.setBorderPainted(true);
		button.setContentAreaFilled(false);
		button.setOpaque(false);

		button.setBorder(new RoundedBorder(PRIMARY, 8));

		button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2d.setColor(button.getBackground());
				g2d.fill(new RoundRectangle2D.Float(1, 1, c.getWidth() - 2, c.getHeight() - 2, 8, 8));
				g2d.dispose();

				super.paint(g, c);
			}
		});

		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				button.setBackground(ACCENT.brighter());
				button.setForeground(Color.BLACK);
				button.setBorder(new RoundedBorder(ACCENT.brighter(), 8));
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				button.setBackground(BACKGROUND);
				button.setForeground(PRIMARY);
				button.setBorder(new RoundedBorder(PRIMARY, 8));
			}
		});
	}
}
