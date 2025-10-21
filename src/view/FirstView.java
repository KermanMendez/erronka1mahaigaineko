package view;

import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.AppState;

public class FirstView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static ImageIcon logo;

	public FirstView() {
		if (!AppState.isAppStarted()) {
			System.exit(0);
		}
		setTitle("LONG RING LONG LAND GYM");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 534, 343);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnEnter = new JButton("Enter");
		btnEnter.addActionListener(e -> {
			LoginFrame loginFrame = new LoginFrame();
			loginFrame.setVisible(true);
			dispose();
		});
		btnEnter.setBounds(207, 255, 89, 23);
		contentPane.add(btnEnter);

		JLabel labelLogo = new JLabel("");
		labelLogo.setIcon(getLogo());
		labelLogo.setHorizontalAlignment(SwingConstants.CENTER);
		labelLogo.setBounds(68, 11, 369, 218);
		contentPane.add(labelLogo);
	}

	private ImageIcon getLogo() {
		if (logo == null) {
			ImageIcon jatorrekoIkono = new ImageIcon(getClass().getResource("/img/logo.png"));
			Image irudiaEskalatua = jatorrekoIkono.getImage().getScaledInstance(300, 220, Image.SCALE_SMOOTH);
			logo = new ImageIcon(irudiaEskalatua);
		}
		return logo;
	}
}