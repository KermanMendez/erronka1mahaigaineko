package view;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.UIStyle;

public class FirstView extends JFrame {

	private static final long serialVersionUID = 1L;
	private static ImageIcon logo;
	private JPanel contentPane;

	public FirstView(Boolean connect) {
		// aplicar tema global
		Theme.apply();

		setTitle("LONG RING LONG LAND GYM");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane = new JPanel(new BorderLayout(12, 12));
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);

		// Logo centrado
		JLabel labelLogo = new JLabel("");
		labelLogo.setIcon(getLogo());
		labelLogo.setHorizontalAlignment(SwingConstants.CENTER);
		UIStyle.styleLabel(labelLogo, true);
		contentPane.add(labelLogo, BorderLayout.CENTER);

		// Panel inferior con botón
		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
		UIStyle.stylePanel(south);
		JButton btnEnter = new JButton("Sartu");
		UIStyle.styleButton(btnEnter);
		UIStyle.addHoverEffect(btnEnter);
		btnEnter.addActionListener(e -> {
			LoginFrame loginFrame = new LoginFrame(connect);
			loginFrame.setVisible(true);
			dispose();
		});
		south.add(btnEnter);
		contentPane.add(south, BorderLayout.SOUTH);

		setSize(560, 380);
		setLocationRelativeTo(null);
		getContentPane().setBackground(UIStyle.BACKGROUND);
	}

	private ImageIcon getLogo() {
		if (logo == null) {
			ImageIcon jatorrekoIkono = new ImageIcon(getClass().getResource("/img/logo.png"));
			Image irudiaEskalatua = jatorrekoIkono.getImage().getScaledInstance(360, 260, Image.SCALE_SMOOTH);
			logo = new ImageIcon(irudiaEskalatua);
		}
		return logo;
	}
}