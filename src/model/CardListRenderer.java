package model;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

public class CardListRenderer implements ListCellRenderer<String> {

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
			boolean isSelected, boolean cellHasFocus) {

		JPanel panel = new JPanel(new java.awt.BorderLayout());
		panel.setOpaque(true);
		panel.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIStyle.BORDER_COLOR, 1, true),
						BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		panel.setBackground(isSelected ? UIStyle.ACCENT : UIStyle.FIELD_BG);
		JLabel label = new JLabel();
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setText(
				"<html><body style='width:520px; font-family:Segoe UI; font-size:12pt;'>" + value + "</body></html>");
		label.setOpaque(false);
		label.setForeground(isSelected ? UIStyle.BUTTON_FG : UIStyle.FIELD_FG);

		panel.add(label, java.awt.BorderLayout.CENTER);

		return panel;
	}
}
