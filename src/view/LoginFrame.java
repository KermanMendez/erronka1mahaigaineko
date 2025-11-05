package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import service.AuthenticationService;

public class LoginFrame extends JFrame {
	public static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFieldUser;
	private JPasswordField passwordField;
	private AuthenticationService authService = new AuthenticationService();

	public LoginFrame(Boolean connect) {

		setIconImage(Toolkit.getDefaultToolkit().getImage(LoginFrame.class.getResource("/img/logo.png")));
		setTitle("LOGIN");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane = new JPanel(new java.awt.GridBagLayout());
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);

		javax.swing.JPanel center = new javax.swing.JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		UIStyle.stylePanel(center);
		center.setBackground(Color.WHITE);
		center.setBorder(
				javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.EmptyBorder(24, 32, 24, 32),
						new UIStyle.RoundedBorder(UIStyle.BORDER_COLOR, 15)));

		JLabel lblLoginLogo = new JLabel("");
		ImageIcon originalIcon = new ImageIcon(getClass().getResource("/img/logo.png"));
		Image scaledImage = originalIcon.getImage().getScaledInstance(240, 180, Image.SCALE_SMOOTH);
		lblLoginLogo.setIcon(new ImageIcon(scaledImage));
		lblLoginLogo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		center.add(lblLoginLogo);
		center.add(Box.createVerticalStrut(24));

		JLabel labelUser = new JLabel("Email:");
		UIStyle.styleLabel(labelUser, false);
		labelUser.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		center.add(labelUser);
		center.add(Box.createVerticalStrut(8));
		textFieldUser = new JTextField();
		textFieldUser.setMaximumSize(new Dimension(360, 40));
		UIStyle.styleField(textFieldUser);
		textFieldUser.setToolTipText("Zure emaila sartu");
		textFieldUser.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		center.add(textFieldUser);
		center.add(Box.createVerticalStrut(16));

		JLabel labelPassword = new JLabel("Password:");
		UIStyle.styleLabel(labelPassword, false);
		labelPassword.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		center.add(labelPassword);
		center.add(Box.createVerticalStrut(8));
		passwordField = new JPasswordField();
		passwordField.setMaximumSize(new Dimension(360, 40));
		UIStyle.styleField(passwordField);
		passwordField.setToolTipText("Zure pasahitza sartu");
		passwordField.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		center.add(passwordField);
		center.add(Box.createVerticalStrut(12));

		javax.swing.JPanel actions = new javax.swing.JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
		actions.setOpaque(false);
		center.add(Box.createVerticalStrut(24));

		JButton btnLogin = new JButton("Login");
		UIStyle.styleButton(btnLogin);
		btnLogin.setPreferredSize(new Dimension(140, 45));
		btnLogin.setToolTipText("Saioa hasi");
		UIStyle.addHoverEffect(btnLogin);
		btnLogin.addActionListener(e -> {
			String user = authService.handleLogin(textFieldUser, passwordField, connect);
			if (user != null) {
				dispose();
			}
		});

		JButton btnRegister = new JButton("Register");
		UIStyle.styleButton(btnRegister);
		btnRegister.setPreferredSize(new Dimension(140, 45));
		btnRegister.setToolTipText("Kontu Berria sortu");
		UIStyle.addHoverEffect(btnRegister);
		btnRegister.addActionListener(e -> {
			RegisterDialog registerDialog = new RegisterDialog(connect);
			registerDialog.setVisible(true);
		});

		actions.add(btnLogin);
		actions.add(btnRegister);
		center.add(actions);

		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = java.awt.GridBagConstraints.CENTER;
		contentPane.add(center, gbc);

		setSize(480, 600);
		setLocationRelativeTo(null);
		getContentPane().setBackground(UIStyle.BACKGROUND);

		center.setBackground(Color.WHITE);
		center.setBorder(
				javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.EmptyBorder(20, 20, 20, 20),
						javax.swing.BorderFactory.createCompoundBorder(
								new UIStyle.RoundedBorder(UIStyle.BORDER_COLOR, 15),
								new javax.swing.border.EmptyBorder(20, 30, 20, 30))));
	}
}