package view;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String API_KEY = "AIzaSyBhHBYyK1vmvbrbP-tWUfFNxRqbeu2AOu4";

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
		btnLogin.setBounds(59, 211, 89, 23);
		btnLogin.addActionListener(e -> handleLogin());
		contentPane.add(btnLogin);

		JButton btnRegistro = new JButton("Registratu");
		btnRegistro.setBounds(255, 213, 105, 23);
		btnRegistro.addActionListener(e -> {
			RegisterDialog registerDialog = new RegisterDialog(this);
			registerDialog.setVisible(true);
		});
		contentPane.add(btnRegistro);
	}

	private void handleLogin() {
		String email = textFieldUser.getText().trim();
		String password = new String(passwordField.getPassword());

		if (email.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Bete Erabiltzailea eta Pasahitza.");
			return;
		}

		try {
			boolean loginSuccess = checkLogin(email, password);
			if (loginSuccess) {
				Inter inter = new Inter();
				dispose();
				inter.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(this, 
						"Erabiltzailea edo Pasahitza okerrak.", 
						"Errorea",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errorea Firebase Authentication-ekin konektatzean.");
		}
	}

	private boolean checkLogin(String email, String password) throws Exception {
		if (API_KEY == null || API_KEY.isEmpty()) {
			throw new IllegalStateException("FIREBASE_API_KEY not set. Set env var or -DFIREBASE_API_KEY=<key>");
		}

		String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;

		OkHttpClient client = new OkHttpClient();

		JsonObject json = new JsonObject();
		json.addProperty("email", email);
		json.addProperty("password", password);
		json.addProperty("returnSecureToken", true);

		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(JSON, json.toString());

		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();

		try (Response response = client.newCall(request).execute()) {
			return response.isSuccessful();
		}
	}
}