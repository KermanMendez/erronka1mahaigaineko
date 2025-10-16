package view;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mindrot.jbcrypt.BCrypt;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import controller.Controller;

public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFieldUser;
	private JPasswordField passwordField;

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

		JLabel labelUser = new JLabel("User:");
		labelUser.setBounds(40, 54, 90, 14);
		contentPane.add(labelUser);

		JLabel labelPassword = new JLabel("Password:");
		labelPassword.setBounds(40, 110, 87, 14);
		contentPane.add(labelPassword);

		textFieldUser = new JTextField();
		textFieldUser.setBounds(40, 79, 136, 20);
		contentPane.add(textFieldUser);
		textFieldUser.setColumns(10);

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
		btnLogin.setBounds(59, 211, 89, 23);
		contentPane.add(btnLogin);

		btnLogin.addActionListener(e -> {
			String username = textFieldUser.getText().trim();
			String password = new String(passwordField.getPassword());

			if (username.isEmpty() || password.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Bete Erabiltzailea eta Pasahitza.");
				return;
			}

			try {
				User user = checkLogin(username, password);
				if (user != null) {
					Workouts workouts = new Workouts(user.isAdmin);
					workouts.setVisible(true);
					dispose();
				} else {
					JOptionPane.showMessageDialog(null, "Erabiltzailea edo Pasahitza okerrak.");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Errorea datu basearekin konektatzean.");
			}
		});

		JButton btnRegistro = new JButton("Registratu");
		btnRegistro.setBounds(255, 213, 105, 23);
		contentPane.add(btnRegistro);

		btnRegistro.addActionListener(e -> {
			RegisterDialog registerDialog = new RegisterDialog(this);
			registerDialog.setVisible(true);
		});
	}

	private User checkLogin(String username, String plainPassword) throws Exception {
	    Firestore db;
	    Controller controller = new Controller();
	    db = controller.getDb();
	    DocumentReference docRef = db.collection("users").document(username);
	    ApiFuture<DocumentSnapshot> future = docRef.get();
	    DocumentSnapshot doc = future.get();
	    if (!doc.exists())
	        return null;

	    String storedHash = doc.getString("password");
	    if (storedHash == null || storedHash.isEmpty())
	        return null;

	    if (BCrypt.checkpw(plainPassword, storedHash)) {
	        Boolean isAdminFromDB = doc.getBoolean("isAdmin");
	        boolean isAdmin = isAdminFromDB != null && isAdminFromDB;
	        return new User(isAdmin);
	    } else {
	        return null;
	    }
	}


	private static class User {
		boolean isAdmin;

		public User(boolean isAdmin) {
			this.isAdmin = isAdmin;
		}
	}
}
