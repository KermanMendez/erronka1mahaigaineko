package view;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;



import controller.DBConnection;

public class RegisterDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldEmail;
	private JPasswordField passwordField;
	private DBConnection dbConnection = new DBConnection();


	public RegisterDialog(JFrame parent) {
		super(parent, "Erabiltzailearen Registroa", true);
		setSize(399, 239);
		setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JLabel labelEmail = new JLabel("Email:");
		labelEmail.setBounds(10, 45, 150, 25);
		getContentPane().add(labelEmail);
		textFieldEmail = new JTextField();
		textFieldEmail.setBounds(170, 45, 200, 25);
		getContentPane().add(textFieldEmail);

		JLabel labelPass = new JLabel("Pasahitza:");
		labelPass.setBounds(10, 80, 150, 25);
		getContentPane().add(labelPass);
		passwordField = new JPasswordField();
		passwordField.setBounds(170, 80, 200, 25);
		getContentPane().add(passwordField);

		JButton btnRegistrar = new JButton("Eskaera Registratu");
		btnRegistrar.setBounds(170, 120, 160, 25);
		btnRegistrar.addActionListener(e -> registrarSolicitud());
		getContentPane().add(btnRegistrar);

		JButton btnCancelar = new JButton("Utzi");
		btnCancelar.setBounds(170, 155, 160, 25);
		btnCancelar.addActionListener(e -> dispose());
		getContentPane().add(btnCancelar);
		

	}

	private void registrarSolicitud() {
		String email = textFieldEmail.getText().trim();
		String password = new String(passwordField.getPassword());

		if (email.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Datu Guztiak Bete.");
			return;
		}

		try {
			dbConnection.createUser(email, password);
			JOptionPane.showMessageDialog(this, "Registratu zara");
			dispose();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errorea registratzen.", "Errorea", JOptionPane.ERROR_MESSAGE);
		}
	}
}
