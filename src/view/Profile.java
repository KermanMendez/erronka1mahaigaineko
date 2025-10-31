package view;

import model.CreateUserBackup;
import model.Theme;
import model.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.ParseException;

public class Profile extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	public Profile() {
		Theme.apply();

		setTitle("Perfil de usuario");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(520, 420);
		setLocationRelativeTo(null);

		contentPane = new JPanel(new BorderLayout(12, 12));
		contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);

		JPanel header = new JPanel(new BorderLayout());
		UIStyle.stylePanel(header);
		JLabel title = new JLabel("Mi perfil");
		UIStyle.styleLabel(title, true);
		header.add(title, BorderLayout.WEST);
		contentPane.add(header, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		UIStyle.stylePanel(form);
		form.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		int row = 0;

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		JLabel lblName = new JLabel("Nombre:");
		UIStyle.styleLabel(lblName, false);
		form.add(lblName, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		JTextField tfName = new JTextField();
		UIStyle.styleField(tfName);
		form.add(tfName, gbc);

		row++;

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		JLabel lblSurname = new JLabel("Apellidos:");
		UIStyle.styleLabel(lblSurname, false);
		form.add(lblSurname, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		JTextField tfSurname = new JTextField();
		UIStyle.styleField(tfSurname);
		form.add(tfSurname, gbc);

		row++;

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		JLabel lblEmail = new JLabel("Gmail:");
		UIStyle.styleLabel(lblEmail, false);
		form.add(lblEmail, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		JTextField tfEmail = new JTextField();
		UIStyle.styleField(tfEmail);
		form.add(tfEmail, gbc);

		row++;

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		JLabel lblPassword = new JLabel("Contraseña nueva:");
		UIStyle.styleLabel(lblPassword, false);
		form.add(lblPassword, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		JPasswordField pfPassword = new JPasswordField();
		UIStyle.styleField(pfPassword);
		form.add(pfPassword, gbc);

		row++;

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		JLabel lblPassword2 = new JLabel("Confirmar contraseña:");
		UIStyle.styleLabel(lblPassword2, false);
		form.add(lblPassword2, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		JPasswordField pfPassword2 = new JPasswordField();
		UIStyle.styleField(pfPassword2);
		form.add(pfPassword2, gbc);

		row++;

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0.0;
		JLabel lblDob = new JLabel("Fecha de nacimiento (dd/MM/yyyy):");
		UIStyle.styleLabel(lblDob, false);
		form.add(lblDob, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		JFormattedTextField tfDob;
		try {
			MaskFormatter mf = new MaskFormatter("##/##/####");
			mf.setPlaceholderCharacter('_');
			tfDob = new JFormattedTextField(mf);
		} catch (ParseException pe) {
			tfDob = new JFormattedTextField();
		}
		UIStyle.styleField(tfDob);
		form.add(tfDob, gbc);

		row++;

		contentPane.add(form, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		UIStyle.stylePanel(buttons);

		JButton btnCancel = new JButton("Cancelar");
		UIStyle.styleButton(btnCancel);
		UIStyle.addHoverEffect(btnCancel);
		btnCancel.addActionListener((ActionEvent e) -> dispose());

		JButton btnSave = new JButton("Guardar");
		UIStyle.styleButton(btnSave);
		UIStyle.addHoverEffect(btnSave);
		final JFormattedTextField finalTfDob = tfDob;
		btnSave.addActionListener((ActionEvent e) -> {
			String name = tfName.getText().trim();
			String surname = tfSurname.getText().trim();
			String email = tfEmail.getText().trim();
			String pwd = new String(pfPassword.getPassword());
			String pwd2 = new String(pfPassword2.getPassword());
			String dob = finalTfDob.getText().trim();

			if (email.isEmpty() || !email.contains("@")) {
				JOptionPane.showMessageDialog(this, "Introduce un email válido.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!pwd.isEmpty() || !pwd2.isEmpty()) {
				if (!pwd.equals(pwd2)) {
					JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (pwd.length() < 6) {
					JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 6 caracteres.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			try {
				new CreateUserBackup().saveEmail(email);
			} catch (IOException io) {
				JOptionPane.showMessageDialog(this, "No se pudo guardar el email localmente: " + io.getMessage(),
						"Aviso", JOptionPane.WARNING_MESSAGE);
			}

			JOptionPane.showMessageDialog(this, "Perfil guardado.\nNombre: " + name + "\nApellidos: " + surname
					+ "\nEmail: " + email + "\nFecha nacimiento: " + dob, "Guardado", JOptionPane.INFORMATION_MESSAGE);
			dispose();
		});

		buttons.add(btnCancel);
		buttons.add(btnSave);

		contentPane.add(buttons, BorderLayout.SOUTH);

	}

}