package view;

import java.awt.EventQueue;
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
import javax.swing.border.EmptyBorder;

import com.google.cloud.firestore.Firestore;

import controller.Controller;
import controller.DBConnection;

public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField textFieldUser;
	private JPasswordField passwordField;
	private Controller controller = new Controller();
	private DBConnection dbConnection = controller.getDbConnection();
	Firestore db = controller.getDb();

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				LoginFrame frame = new LoginFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public LoginFrame() {
		setIconImage(Toolkit.getDefaultToolkit().getImage("img/logo.png"));
		setTitle("LOGIN");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel labelUser = new JLabel("Email:");
		labelUser.setBounds(40, 54, 90, 14);
		contentPane.add(labelUser);

		JLabel labelPassword = new JLabel("Password:");
		labelPassword.setBounds(40, 110, 87, 14);
		contentPane.add(labelPassword);

		textFieldUser = new JTextField();
		textFieldUser.setBounds(40, 79, 136, 20);
		textFieldUser.setColumns(10);
		contentPane.add(textFieldUser);

		passwordField = new JPasswordField();
		passwordField.setBounds(40, 135, 136, 20);
		contentPane.add(passwordField);

		JLabel lblLoginLogo = new JLabel("");
		ImageIcon originalIcon = new ImageIcon(getClass().getResource("/img/logo.png"));
		Image scaledImage = originalIcon.getImage().getScaledInstance(250, 180, Image.SCALE_SMOOTH);
		lblLoginLogo.setIcon(new ImageIcon(scaledImage));
		lblLoginLogo.setHorizontalAlignment(SwingConstants.CENTER);
		lblLoginLogo.setBounds(207, 22, 217, 180);
		contentPane.add(lblLoginLogo);

		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Boolean ok = dbConnection.handleLogin(textFieldUser, passwordField);
				if (ok) {
				dispose();
				}
			}
		});
		btnLogin.setBounds(59, 211, 89, 23);
		contentPane.add(btnLogin);

		JButton btnRegistro = new JButton("Registratu");
		btnRegistro.setBounds(255, 213, 105, 23);
		btnRegistro.addActionListener(e -> {
			RegisterDialog registerDialog = new RegisterDialog(this);
			registerDialog.setVisible(true);
		});
		contentPane.add(btnRegistro);
	}
}