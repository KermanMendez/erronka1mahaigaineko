package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import controller.AppState;
import model.ConnectDB;
import model.DateFormater;

public class RegisterDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldEmail;
	private JPasswordField passwordField;
	private JTextField abizena1Field;
	private JTextField abizena2Field;
	private JDatePickerImpl datePicker;
	private JTextField textFieldIzena;
	private JCheckBox checkboxIsTrainer;
	private ConnectDB connectDB = new ConnectDB();

	public RegisterDialog(JFrame parent) {
		super(parent, "Erabiltzailearen Registroa", true);
		if (!AppState.isAppStarted()) {
			System.exit(0);
		}
		setSize(399, 388);
		setLocationRelativeTo(parent);
		getContentPane().setLayout(null);

		JLabel labelEmail = new JLabel("Email:");
		labelEmail.setBounds(10, 22, 150, 25);
		getContentPane().add(labelEmail);

		textFieldEmail = new JTextField();
		textFieldEmail.setBounds(170, 22, 200, 25);
		getContentPane().add(textFieldEmail);

		JLabel labelPass = new JLabel("Pasahitza:");
		labelPass.setBounds(10, 57, 150, 25);
		getContentPane().add(labelPass);

		passwordField = new JPasswordField();
		passwordField.setBounds(170, 57, 200, 25);
		getContentPane().add(passwordField);

		JButton btnRegistrar = new JButton("Eskaera Registratu");
		btnRegistrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Boolean registroa = connectDB.eskaeraRegistratu(textFieldIzena.getText().trim(),
						abizena1Field.getText().trim(), abizena2Field.getText().trim(), textFieldEmail.getText().trim(),
						new String(passwordField.getPassword()), (java.util.Date) datePicker.getModel().getValue(),
						checkboxIsTrainer.isSelected());
				if (registroa) {
					dispose();
				}
			}
		});
		btnRegistrar.setBounds(170, 255, 160, 25);
		getContentPane().add(btnRegistrar);

		JButton btnCancelar = new JButton("Utzi");
		btnCancelar.setBounds(170, 290, 160, 25);
		btnCancelar.addActionListener(e -> dispose());
		getContentPane().add(btnCancelar);

		JLabel lblAbizena1 = new JLabel("Abizena:");
		lblAbizena1.setBounds(10, 122, 150, 25);
		getContentPane().add(lblAbizena1);

		JLabel lblAbizena2 = new JLabel("2º Abizena:");
		lblAbizena2.setBounds(10, 158, 150, 25);
		getContentPane().add(lblAbizena2);

		abizena1Field = new JTextField();
		abizena1Field.setBounds(170, 122, 200, 25);
		getContentPane().add(abizena1Field);

		abizena2Field = new JTextField();
		abizena2Field.setBounds(170, 158, 200, 25);
		getContentPane().add(abizena2Field);

		JLabel labelData = new JLabel("Jaiotze Data:");
		labelData.setBounds(10, 204, 150, 25);
		getContentPane().add(labelData);
		try {
			UtilDateModel model = new UtilDateModel();
			Properties p = new Properties();
			p.put("text.today", "Today");
			p.put("text.month", "Month");
			p.put("text.year", "Year");
			JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
			datePicker = new JDatePickerImpl(datePanel, new DateFormater());
			datePicker.setBounds(170, 204, 200, 40);
			getContentPane().add(datePicker);

			JLabel lblIzena = new JLabel("Izena:");
			lblIzena.setBounds(10, 86, 150, 25);
			getContentPane().add(lblIzena);

			textFieldIzena = new JTextField();
			textFieldIzena.setBounds(170, 93, 200, 25);
			getContentPane().add(textFieldIzena);

			JLabel labelIsTrainer = new JLabel("Is Trainer:");
			labelIsTrainer.setBounds(10, 255, 150, 25);
			getContentPane().add(labelIsTrainer);

			checkboxIsTrainer = new JCheckBox("");
			checkboxIsTrainer.setBounds(83, 255, 21, 23);
			getContentPane().add(checkboxIsTrainer);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error initializing date picker: " + ex.getMessage(),
					"Date Picker Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}