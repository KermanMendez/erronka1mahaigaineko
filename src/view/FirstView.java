package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.Controller;
import model.LoadLogo;
import model.UIStyle;

public class FirstView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private LoadLogo loadLogo = new LoadLogo();

	private Controller controller;

	public FirstView(Boolean connect) {
		this.controller = Controller.getInstance();

		setTitle("LONG RING LONG LAND GYM");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane = new JPanel(new BorderLayout(12, 12));
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);

		JLabel labelLogo = new JLabel("");
		labelLogo.setIcon(loadLogo.getLogo());
		labelLogo.setHorizontalAlignment(SwingConstants.CENTER);
		UIStyle.styleLabel(labelLogo, true);
		contentPane.add(labelLogo, BorderLayout.CENTER);

		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
		UIStyle.stylePanel(south);
		JButton btnEnter = new JButton("Sartu");
		UIStyle.styleButton(btnEnter);
		UIStyle.addHoverEffect(btnEnter);
		btnEnter.addActionListener(e -> {
			LoginFrame loginFrame = new LoginFrame(controller.isOnline());
			loginFrame.setVisible(true);
			dispose();
		});
		south.add(btnEnter);
		contentPane.add(south, BorderLayout.SOUTH);

		setSize(560, 380);
		setLocationRelativeTo(null);
		getContentPane().setBackground(UIStyle.BACKGROUND);
	}
}