package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;

import model.CreateUserBackup;
import model.EditProfile;
import model.Theme;
import model.UIStyle;

public class Profile extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	// form fields as instance members so other methods can access them
	private JTextField tfName;
	private JTextField tfSurname1;
	private JTextField tfSurname2;
	private JPasswordField pfPassword;
	private JPasswordField pfPassword2;
	private JFormattedTextField tfDob;

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

		// create a fresh GridBagConstraints for each component to avoid accidental
		// reuse bugs
		int row = 0;

		GridBagConstraints cLblName = new GridBagConstraints();
		cLblName.insets = new Insets(8, 8, 8, 8);
		cLblName.fill = GridBagConstraints.HORIZONTAL;
		cLblName.gridx = 0;
		cLblName.gridy = row;
		cLblName.weightx = 0.0;
		JLabel lblName = new JLabel("Nombre:");
		UIStyle.styleLabel(lblName, false);
		form.add(lblName, cLblName);

		GridBagConstraints cTfName = (GridBagConstraints) cLblName.clone();
		cTfName.gridx = 1;
		cTfName.weightx = 1.0;
		tfName = new JTextField();
		UIStyle.styleField(tfName);
		form.add(tfName, cTfName);

		row++;

		GridBagConstraints cLblS1 = new GridBagConstraints();
		cLblS1.insets = new Insets(8, 8, 8, 8);
		cLblS1.fill = GridBagConstraints.HORIZONTAL;
		cLblS1.gridx = 0;
		cLblS1.gridy = row;
		cLblS1.weightx = 0.0;
		JLabel lblSurname1 = new JLabel("Apellido 1:");
		UIStyle.styleLabel(lblSurname1, false);
		form.add(lblSurname1, cLblS1);

		GridBagConstraints cTfS1 = (GridBagConstraints) cLblS1.clone();
		cTfS1.gridx = 1;
		cTfS1.weightx = 1.0;
		tfSurname1 = new JTextField();
		UIStyle.styleField(tfSurname1);
		form.add(tfSurname1, cTfS1);

		row++;

		GridBagConstraints cLblS2 = new GridBagConstraints();
		cLblS2.insets = new Insets(8, 8, 8, 8);
		cLblS2.fill = GridBagConstraints.HORIZONTAL;
		cLblS2.gridx = 0;
		cLblS2.gridy = row;
		cLblS2.weightx = 0.0;
		JLabel lblSurname2 = new JLabel("Apellido 2:");
		UIStyle.styleLabel(lblSurname2, false);
		form.add(lblSurname2, cLblS2);

		GridBagConstraints cTfS2 = (GridBagConstraints) cLblS2.clone();
		cTfS2.gridx = 1;
		cTfS2.weightx = 1.0;
		tfSurname2 = new JTextField();
		UIStyle.styleField(tfSurname2);
		form.add(tfSurname2, cTfS2);

		row++;

		GridBagConstraints cLblPwd = new GridBagConstraints();
		cLblPwd.insets = new Insets(8, 8, 8, 8);
		cLblPwd.fill = GridBagConstraints.HORIZONTAL;
		cLblPwd.gridx = 0;
		cLblPwd.gridy = row;
		cLblPwd.weightx = 0.0;
		JLabel lblPassword = new JLabel("Contraseña nueva:");
		UIStyle.styleLabel(lblPassword, false);
		form.add(lblPassword, cLblPwd);

		GridBagConstraints cPfPwd = (GridBagConstraints) cLblPwd.clone();
		cPfPwd.gridx = 1;
		cPfPwd.weightx = 1.0;
		pfPassword = new JPasswordField();
		UIStyle.styleField(pfPassword);
		form.add(pfPassword, cPfPwd);

		row++;

		GridBagConstraints cLblPwd2 = new GridBagConstraints();
		cLblPwd2.insets = new Insets(8, 8, 8, 8);
		cLblPwd2.fill = GridBagConstraints.HORIZONTAL;
		cLblPwd2.gridx = 0;
		cLblPwd2.gridy = row;
		cLblPwd2.weightx = 0.0;
		JLabel lblPassword2 = new JLabel("Confirmar contraseña:");
		UIStyle.styleLabel(lblPassword2, false);
		form.add(lblPassword2, cLblPwd2);

		GridBagConstraints cPfPwd2 = (GridBagConstraints) cLblPwd2.clone();
		cPfPwd2.gridx = 1;
		cPfPwd2.weightx = 1.0;
		pfPassword2 = new JPasswordField();
		UIStyle.styleField(pfPassword2);
		form.add(pfPassword2, cPfPwd2);

		row++;

		GridBagConstraints cLblDob = new GridBagConstraints();
		cLblDob.insets = new Insets(8, 8, 8, 8);
		cLblDob.fill = GridBagConstraints.HORIZONTAL;
		cLblDob.gridx = 0;
		cLblDob.gridy = row;
		cLblDob.weightx = 0.0;
		JLabel lblDob = new JLabel("Fecha de nacimiento (dd/MM/yyyy):");
		UIStyle.styleLabel(lblDob, false);
		form.add(lblDob, cLblDob);

		GridBagConstraints cTfDob = (GridBagConstraints) cLblDob.clone();
		cTfDob.gridx = 1;
		cTfDob.weightx = 1.0;
		try {
			MaskFormatter mf = new MaskFormatter("##/##/####");
			mf.setPlaceholderCharacter('_');
			tfDob = new JFormattedTextField(mf);
		} catch (ParseException pe) {
			tfDob = new JFormattedTextField();
		}
		UIStyle.styleField(tfDob);
		form.add(tfDob, cTfDob);

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
			String surname1 = tfSurname1.getText().trim();
			String surname2 = tfSurname2.getText().trim();
			String surname = surname1 + (surname2.isEmpty() ? "" : " " + surname2);
			String pwd = new String(pfPassword.getPassword());
			String pwd2 = new String(pfPassword2.getPassword());
			String dob = finalTfDob.getText().trim();

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

			// email is not editable here; load it from local backup (saved at login)
			String localEmail = null;
			try {
				localEmail = new CreateUserBackup().loadEmail();
			} catch (Exception ex) {
				// ignore - will handle null below
			}
			if (localEmail == null || localEmail.trim().isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"No se encuentra el email local. Inicia sesión para poder actualizar el perfil.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// perform updates in background to avoid blocking EDT
			final String targetEmail = localEmail;
			new Thread(() -> {
				EditProfile editor = new EditProfile();
				boolean dbOk = editor.updateUserDocument(targetEmail, name, surname1, surname2, dob);
				boolean pwdOk = true;
				if (!pwd.isEmpty()) {
					pwdOk = editor.updatePasswordAuthAndSaveHash(targetEmail, pwd);
				}

				if (dbOk && pwdOk) {
					javax.swing.SwingUtilities.invokeLater(() -> {
						JOptionPane
								.showMessageDialog(this,
										"Perfil actualizado correctamente.\nNombre: " + name + "\nApellidos: " + surname
												+ "\nFecha nacimiento: " + dob,
										"Guardado", JOptionPane.INFORMATION_MESSAGE);
						dispose();
					});
				} else {
					javax.swing.SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(this,
								"Error al actualizar el perfil. Comprueba la conexión y vuelve a intentarlo.", "Error",
								JOptionPane.ERROR_MESSAGE);
					});
				}
			}).start();
		});

		buttons.add(btnCancel);
		buttons.add(btnSave);

		contentPane.add(buttons, BorderLayout.SOUTH);

		// load initial data from DB (if possible)
		loadProfileFromDb();

	}

	private void loadProfileFromDb() {
		// Run in background thread because Firestore calls are blocking
		new Thread(() -> {
			try {
				String email = new CreateUserBackup().loadEmail();
				if (email == null || email.trim().isEmpty()) {
					return;
				}

				// try to get DB connection
				controller.Controller controller = new controller.Controller(true);
				com.google.cloud.firestore.Firestore db = controller.getDb();
				if (db == null)
					return;

				com.google.cloud.firestore.QuerySnapshot query = db.collection("users").whereEqualTo("email", email)
						.get().get();
				if (query.isEmpty())
					return;

				com.google.cloud.firestore.DocumentSnapshot userDoc = query.getDocuments().get(0);
				String name = userDoc.getString("name");
				String surname = userDoc.getString("surname");
				// some users might have 'surname2' or 'surname' fields
				String surname2 = userDoc.getString("surname2");
				String birth = userDoc.getString("birthdate");
				if (birth == null)
					birth = userDoc.getString("birthday");

				final String fName = name != null ? name : "";
				String fSurname1 = "";
				String fSurname2 = "";
				if (surname != null) {
					if (surname2 != null && !surname2.isEmpty()) {
						fSurname1 = surname;
						fSurname2 = surname2;
					} else {
						String[] parts = surname.trim().split("\\s+", 2);
						fSurname1 = parts.length > 0 ? parts[0] : "";
						fSurname2 = parts.length > 1 ? parts[1] : "";
					}
				}
				final String ffSurname1 = fSurname1;
				final String ffSurname2 = fSurname2;
				final String fBirth = birth != null ? birth : "";

				javax.swing.SwingUtilities.invokeLater(() -> {
					tfName.setText(fName);
					tfSurname1.setText(ffSurname1);
					tfSurname2.setText(ffSurname2);
					tfDob.setText(fBirth);
				});

			} catch (Exception ex) {
				// ignore: leave fields blank when something fails
			}
		}).start();
	}

	// moved implementation to model.EditProfile
}