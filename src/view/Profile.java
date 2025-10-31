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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;

import model.CreateUserBackup;
import model.EditProfile;
import model.Theme;
import model.UIStyle;
import model.User;

public class Profile extends JFrame {

	private static final long serialVersionUID = 1L;

	private User userProfile;

	private JPanel contentPane;
	private JTextField tfName;
	private JTextField tfSurname1;
	private JTextField tfSurname2;
	private JPasswordField pfPassword;
	private JPasswordField pfPassword2;
	private JFormattedTextField tfDob;

	private EditProfile editProfile = new EditProfile();

	public Profile() {
		Theme.apply();

		setTitle("Erabiltzailearen Profila");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(520, 420);
		setLocationRelativeTo(null);

		contentPane = new JPanel(new BorderLayout(12, 12));
		contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);

		JPanel header = new JPanel(new BorderLayout());
		UIStyle.stylePanel(header);
		JLabel title = new JLabel("Nire Profila");
		UIStyle.styleLabel(title, true);
		header.add(title, BorderLayout.WEST);
		contentPane.add(header, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		UIStyle.stylePanel(form);
		form.setOpaque(false);

		int row = 0;

		GridBagConstraints cLblName = new GridBagConstraints();
		cLblName.insets = new Insets(8, 8, 8, 8);
		cLblName.fill = GridBagConstraints.HORIZONTAL;
		cLblName.gridx = 0;
		cLblName.gridy = row;
		cLblName.weightx = 0.0;
		JLabel lblName = new JLabel("Izena:");
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
		JLabel lblSurname1 = new JLabel("Abizena 1:");
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
		JLabel lblSurname2 = new JLabel("Abizena 2:");
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
		JLabel lblPassword = new JLabel("Pasahitza Berria:");
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
		JLabel lblPassword2 = new JLabel("Pasahitza Berria Errepikatu:");
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
		JLabel lblDob = new JLabel("Jaiotze Data::");
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

		JButton btnCancel = new JButton("Ezeztatu");
		UIStyle.styleButton(btnCancel);
		UIStyle.addHoverEffect(btnCancel);
		btnCancel.addActionListener((ActionEvent e) -> dispose());

		JButton btnSave = new JButton("Gorde");
		UIStyle.styleButton(btnSave);
		UIStyle.addHoverEffect(btnSave);
		final JFormattedTextField finalTfDob = tfDob;
		btnSave.addActionListener((ActionEvent e) -> {

			userProfile = editProfile.validateChanges(tfName, tfSurname1, tfSurname2, pfPassword, pfPassword2,
					finalTfDob);

			// validateChanges returns null on validation error (e.g. passwords mismatch)
			if (userProfile == null) {
				return; // user alerted inside validateChanges
			}

			String localEmail = null;
			try {
				localEmail = new CreateUserBackup().loadEmail();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			editProfile.setLocalEmail(localEmail);

			final String targetEmail = localEmail;
			new Thread(() -> {
				boolean dbOk = editProfile.updateUserDocument(targetEmail, userProfile.getName(),
						userProfile.getSurname(), userProfile.getSurname2(), userProfile.getDobString());
				boolean pwdOk = true;
				if (userProfile.getPassword() != null && !userProfile.getPassword().isEmpty()) {
					pwdOk = editProfile.updatePasswordAuthAndSaveHash(targetEmail, userProfile.getPassword());
				}

				editProfile.showMessage(dbOk, pwdOk, userProfile.getName(), userProfile.getFullSurname(),
						userProfile.getDobString());

			}).start();
		});

		buttons.add(btnCancel);
		buttons.add(btnSave);

		contentPane.add(buttons, BorderLayout.SOUTH);

	editProfile.loadProfileFromDb(tfName, tfSurname1, tfSurname2, tfDob);

	}
}