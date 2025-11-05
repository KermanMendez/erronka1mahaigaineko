package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import service.AuthenticationService;
import util.DateFormater;

public class RegisterDialog extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldEmail;
	private JPasswordField passwordField;
	private JTextField abizena1Field;
	private JTextField abizena2Field;
	private JDatePickerImpl datePicker;
	private JTextField textFieldIzena;
	private JCheckBox checkboxtrainer;
	private AuthenticationService authService = new AuthenticationService();

	public RegisterDialog(Boolean connect) {

		setIconImage(Toolkit.getDefaultToolkit().getImage(RegisterDialog.class.getResource("/img/logo.png")));
		setTitle("Erabiltzailearen Registroa");
		setSize(520, 420);
		setLocationRelativeTo(null);

		JPanel root = new JPanel(new BorderLayout(12, 12));
		UIStyle.stylePanel(root);
		setContentPane(root);

		JPanel form = new JPanel(new java.awt.GridBagLayout());
		UIStyle.stylePanel(form);
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.insets = new java.awt.Insets(8, 8, 8, 8);
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

		int row = 0;

		JLabel labelEmail = new JLabel("Email:");
		UIStyle.styleLabel(labelEmail, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(labelEmail, gbc);

		textFieldEmail = new JTextField();
		UIStyle.styleField(textFieldEmail);
		textFieldEmail.setToolTipText("Zure email-a sartu");
		gbc.gridx = 1;
		gbc.gridy = row++;
		gbc.weightx = 1.0;
		form.add(textFieldEmail, gbc);

		JLabel labelPass = new JLabel("Pasahitza:");
		UIStyle.styleLabel(labelPass, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;
		form.add(labelPass, gbc);

		passwordField = new JPasswordField();
		UIStyle.styleField(passwordField);
		passwordField.setToolTipText("Zure pasahitza sartu");
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(passwordField, gbc);

		JLabel lblIzena = new JLabel("Izena:");
		UIStyle.styleLabel(lblIzena, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(lblIzena, gbc);

		textFieldIzena = new JTextField();
		UIStyle.styleField(textFieldIzena);
		textFieldIzena.setToolTipText("Izena");
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(textFieldIzena, gbc);

		JLabel lblAbizena1 = new JLabel("Abizena:");
		UIStyle.styleLabel(lblAbizena1, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(lblAbizena1, gbc);

		abizena1Field = new JTextField();
		UIStyle.styleField(abizena1Field);
		abizena1Field.setToolTipText("Lehenengo Abizena");
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(abizena1Field, gbc);

		JLabel lblAbizena2 = new JLabel("Bigarren Abizena:");
		UIStyle.styleLabel(lblAbizena2, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(lblAbizena2, gbc);

		abizena2Field = new JTextField();
		UIStyle.styleField(abizena2Field);
		abizena2Field.setToolTipText("Bigarren Abizena");
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(abizena2Field, gbc);

		JLabel labelData = new JLabel("Jaiotze Data:");
		UIStyle.styleLabel(labelData, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(labelData, gbc);

		UtilDateModel model = new UtilDateModel();
		Properties p = new Properties();
		p.put("text.today", "Gaur");
		p.put("text.month", "Hilabetea");
		p.put("text.year", "Urtea");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		datePicker = new JDatePickerImpl(datePanel, new DateFormater());
		UIStyle.styleField(datePicker);
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(datePicker, gbc);

		JLabel lblTrainer = new JLabel("Entrenatzailea da?");
		UIStyle.styleLabel(lblTrainer, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(lblTrainer, gbc);

		checkboxtrainer = new JCheckBox();
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(checkboxtrainer, gbc);

		root.add(form, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
		UIStyle.stylePanel(actions);
		JButton btnRegistrar = new JButton("Registratu");
		UIStyle.styleButton(btnRegistrar);
		btnRegistrar.setToolTipText("Erabiltzailea registratu");
		UIStyle.addHoverEffect(btnRegistrar);
		btnRegistrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Boolean registroa = authService.eskaeraRegistratu(textFieldIzena.getText().trim(),
						abizena1Field.getText().trim(), abizena2Field.getText().trim(), textFieldEmail.getText().trim(),
						new String(passwordField.getPassword()), (java.util.Date) datePicker.getModel().getValue(),
						checkboxtrainer.isSelected(), connect);
				if (registroa) {
					dispose();
				}
			}
		});

		JButton btnCancelar = new JButton("Utzi");
		UIStyle.styleButton(btnCancelar);
		btnCancelar.setToolTipText("Saiakera utzi");
		UIStyle.addHoverEffect(btnCancelar);
		btnCancelar.addActionListener(e -> dispose());

		actions.add(btnRegistrar);
		actions.add(btnCancelar);
		root.add(actions, BorderLayout.SOUTH);

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}
}
