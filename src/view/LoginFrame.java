package view;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import model.ConnectDB;
import model.UIStyle;

public class LoginFrame extends JFrame {
	public static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFieldUser;
	private JPasswordField passwordField;
	private ConnectDB connectDB = new ConnectDB();

	public LoginFrame(Boolean connect) {

		setIconImage(Toolkit.getDefaultToolkit().getImage(LoginFrame.class.getResource("/img/logo.png")));
		setTitle("LOGIN");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		JLabel labelUser = new JLabel("Email:");
		labelUser.setBounds(40, 54, 90, 14);
		UIStyle.styleLabel(labelUser, false);
		contentPane.add(labelUser);
		JLabel labelPassword = new JLabel("Password:");
		labelPassword.setBounds(40, 110, 87, 14);
		UIStyle.styleLabel(labelPassword, false);
		contentPane.add(labelPassword);
		textFieldUser = new JTextField();
		textFieldUser.setBounds(40, 79, 136, 20);
		UIStyle.styleField(textFieldUser);
		textFieldUser.setColumns(10);
		textFieldUser.setToolTipText("Zure emaila sartu");
		contentPane.add(textFieldUser);
		passwordField = new JPasswordField();
		passwordField.setBounds(40, 135, 136, 20);
		UIStyle.styleField(passwordField);
		passwordField.setToolTipText("Zure pasahitza sartu");
		contentPane.add(passwordField);
		JLabel lblLoginLogo = new JLabel("");
		ImageIcon originalIcon = new ImageIcon(getClass().getResource("/img/logo.png"));
		Image scaledImage = originalIcon.getImage().getScaledInstance(250, 180, Image.SCALE_SMOOTH);
		lblLoginLogo.setIcon(new ImageIcon(scaledImage));
		lblLoginLogo.setHorizontalAlignment(SwingConstants.CENTER);
		lblLoginLogo.setBounds(207, 22, 217, 180);
		contentPane.add(lblLoginLogo);
		JButton btnLogin = new JButton("Login");
		UIStyle.styleButton(btnLogin);
		btnLogin.setToolTipText("Saioa hasi");
		UIStyle.addHoverEffect(btnLogin);

		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = connectDB.handleLogin(textFieldUser, passwordField, connect);
				if (user != null) {
					dispose();
				}
			}
		});

		btnLogin.setBounds(40, 180, 136, 30);
		contentPane.add(btnLogin);
		JButton btnRegister = new JButton("Register");
		UIStyle.styleButton(btnRegister);
		btnRegister.setToolTipText("Kontu Berria sortu");
		UIStyle.addHoverEffect(btnRegister);

		btnRegister.addActionListener(e -> {
			RegisterDialog registerDialog = new RegisterDialog(connect);
			registerDialog.setVisible(true);
		});

		btnRegister.setBounds(40, 220, 136, 30);
		contentPane.add(btnRegister);
		getContentPane().setBackground(UIStyle.BACKGROUND);
	}
}