package view;

import javax.swing.*;
import java.awt.*;

import model.UIStyle;

/**
 * UI osagaiak sortzeko fabrika klasea
 * Clase factoría para crear componentes de UI reutilizables
 */
public class ComponentFactory {

	/**
	 * Botoi bat sortu estilo aplikatuz
	 * @param testua Botoiaren testua
	 * @param tooltipTestua Tooltip testua (aukerakoa)
	 * @return JButton konfiguratua
	 */
	public static JButton sortuBotoia(String testua, String tooltipTestua) {
		JButton botoia = new JButton(testua);
		UIStyle.styleButton(botoia);
		UIStyle.addHoverEffect(botoia);
		if (tooltipTestua != null && !tooltipTestua.isEmpty()) {
			botoia.setToolTipText(tooltipTestua);
		}
		return botoia;
	}

	/**
	 * Botoi bat sortu tamaina zehaztuz
	 * @param testua Botoiaren testua
	 * @param tooltipTestua Tooltip testua
	 * @param zabalera Zabalera
	 * @param altuera Altuera
	 * @return JButton konfiguratua
	 */
	public static JButton sortuBotoia(String testua, String tooltipTestua, int zabalera, int altuera) {
		JButton botoia = sortuBotoia(testua, tooltipTestua);
		botoia.setPreferredSize(new Dimension(zabalera, altuera));
		return botoia;
	}

	/**
	 * Ikono botoi bat sortu
	 * @param ikonoPath Ikono bidea
	 * @param tooltipTestua Tooltip testua
	 * @return JButton ikonoarekin
	 */
	public static JButton sortuIkonoBotoia(String ikonoPath, String tooltipTestua) {
		ImageIcon originalIcon = new ImageIcon(ComponentFactory.class.getResource(ikonoPath));
		Image scaledImage = originalIcon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
		JButton botoia = new JButton(new ImageIcon(scaledImage));
		UIStyle.styleIconButton(botoia);
		if (tooltipTestua != null) {
			botoia.setToolTipText(tooltipTestua);
		}
		return botoia;
	}

	/**
	 * Label bat sortu
	 * @param testua Label testua
	 * @param titulua True titulu gisa formateatu nahi bada
	 * @return JLabel konfiguratua
	 */
	public static JLabel sortuLabela(String testua, boolean titulua) {
		JLabel label = new JLabel(testua);
		UIStyle.styleLabel(label, titulua);
		return label;
	}

	/**
	 * Testu eremua sortu
	 * @return JTextField konfiguratua
	 */
	public static JTextField sortuTestuEremua() {
		JTextField field = new JTextField();
		UIStyle.styleField(field);
		return field;
	}

	/**
	 * Testu eremua sortu tooltiparekin
	 * @param tooltipTestua Tooltip testua
	 * @return JTextField konfiguratua
	 */
	public static JTextField sortuTestuEremua(String tooltipTestua) {
		JTextField field = sortuTestuEremua();
		if (tooltipTestua != null) {
			field.setToolTipText(tooltipTestua);
		}
		return field;
	}

	/**
	 * Pasahitza eremua sortu
	 * @param tooltipTestua Tooltip testua
	 * @return JPasswordField konfiguratua
	 */
	public static JPasswordField sortuPasahitzaEremua(String tooltipTestua) {
		JPasswordField field = new JPasswordField();
		UIStyle.styleField(field);
		if (tooltipTestua != null) {
			field.setToolTipText(tooltipTestua);
		}
		return field;
	}

	/**
	 * Panel bat sortu
	 * @param layout Layout kudeatzailea
	 * @return JPanel konfiguratua
	 */
	public static JPanel sortuPanela(LayoutManager layout) {
		JPanel panel = new JPanel(layout);
		UIStyle.stylePanel(panel);
		return panel;
	}

	/**
	 * Panel zentratua sortu GridBagLayout-ekin
	 * @return JPanel zentratua
	 */
	public static JPanel sortuPanelaZentratua() {
		return sortuPanela(new GridBagLayout());
	}

	/**
	 * Formulario panela sortu eremu vertikalak dituzten
	 * @return JPanel formularioarentzat
	 */
	public static JPanel sortuFormularioPanela() {
		JPanel panel = new JPanel(new GridBagLayout());
		UIStyle.stylePanel(panel);
		panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		return panel;
	}

	/**
	 * Scroll panel bat sortu
	 * @param edukia Barneko edukia
	 * @return JScrollPane konfiguratua
	 */
	public static JScrollPane sortuScrollPanela(Component edukia) {
		JScrollPane scrollPane = new JScrollPane(edukia);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		UIStyle.styleScrollPane(scrollPane);
		return scrollPane;
	}

	/**
	 * ComboBox bat sortu
	 * @param items Aukerak
	 * @param tooltipTestua Tooltip testua
	 * @return JComboBox konfiguratua
	 */
	public static <T> JComboBox<T> sortuComboBox(T[] items, String tooltipTestua) {
		JComboBox<T> comboBox = new JComboBox<>(items);
		UIStyle.styleField(comboBox);
		if (tooltipTestua != null) {
			comboBox.setToolTipText(tooltipTestua);
		}
		return comboBox;
	}

	/**
	 * GridBagConstraints sortu formularioen eremuetarako
	 * @param x X posizioa (zutabea)
	 * @param y Y posizioa (errenkada)
	 * @param weightx Pisu horizontala
	 * @return GridBagConstraints konfiguratua
	 */
	public static GridBagConstraints sortuFormularioConstraints(int x, int y, double weightx) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.weightx = weightx;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 8, 8, 8);
		return gbc;
	}

	/**
	 * Goiburua sortu atzera botoiarekin, titulua eta eskuin botoiarekin
	 * @param atzeraBotoiaAction Atzera botoiaren akzioa
	 * @param titulua Goiburuaren titulua
	 * @param eskuinBotoia Eskuineko botoia (aukerakoa)
	 * @return JPanel goiburuarekin
	 */
	public static JPanel sortuGoiburua(Runnable atzeraBotoiaAction, String titulua, JButton eskuinBotoia) {
		JPanel header = sortuPanela(new BorderLayout());

		if (atzeraBotoiaAction != null) {
			JButton btnAtzera = sortuIkonoBotoia("/img/atzera.png", "Atzera joan");
			btnAtzera.addActionListener(e -> atzeraBotoiaAction.run());
			header.add(btnAtzera, BorderLayout.WEST);
		}

		JLabel lblTitulua = sortuLabela(titulua, true);
		lblTitulua.setHorizontalAlignment(SwingConstants.CENTER);
		header.add(lblTitulua, BorderLayout.CENTER);

		if (eskuinBotoia != null) {
			header.add(eskuinBotoia, BorderLayout.EAST);
		}

		return header;
	}
}
