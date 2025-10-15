package view;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import model.PojoRegistratu;
import model.Registroak;

public class RegisterDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldUser;
	private JTextField textFieldEmail;
	private JPasswordField passwordField;

	public RegisterDialog(JFrame parent) {
		super(parent, "Erabiltzailearen Registroa", true);
		setSize(400, 220);
		setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JLabel labelUser = new JLabel("Erabiltzailea:");
		labelUser.setBounds(10, 10, 150, 25);
		getContentPane().add(labelUser);
		textFieldUser = new JTextField();
		textFieldUser.setBounds(170, 10, 200, 25);
		getContentPane().add(textFieldUser);

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
		String username = textFieldUser.getText().trim();
		String email = textFieldEmail.getText().trim();
		String password = new String(passwordField.getPassword());

		if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Datu Guztiak Bete.");
			return;
		}

		try {
			Registroak eskaerak = new Registroak();
			eskaerak.eskaeraGorde(new PojoRegistratu(username, email, password));
			JOptionPane.showMessageDialog(this, "Eskaera Bidalita.\n Administratzaile batek kudeatuko du.");
			dispose();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errorea eskaera goredetzean.");
		}
	}
}
