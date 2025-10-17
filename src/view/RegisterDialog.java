package view;

import java.util.Date;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jdatepicker.impl.DateComponentFormatter;

import controller.DBConnection;
import javax.swing.JCheckBox;

public class RegisterDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldEmail;
	private JPasswordField passwordField;
	private DBConnection dbConnection = new DBConnection();
	private JTextField abizena1Field;
	private JTextField abizena2Field;
	private JDatePickerImpl datePicker;
	private JTextField textFieldIzena;
	private JCheckBox checkboxIsTrainer;

	public RegisterDialog(JFrame parent) {
		super(parent, "Erabiltzailearen Registroa", true);
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
		btnRegistrar.setBounds(170, 255, 160, 25);
		btnRegistrar.addActionListener(e -> registrarSolicitud());
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
			// Use custom formatter for Date.toString() format
			datePicker = new JDatePickerImpl(datePanel, new DateToStringFormatter());
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

	private void registrarSolicitud() {
		String izena = textFieldIzena.getText().trim();
		String abizena1 = abizena1Field.getText().trim();
		String abizena2 = abizena2Field.getText().trim();
		String email = textFieldEmail.getText().trim();
		String password = new String(passwordField.getPassword());
		Boolean isTrainer = checkboxIsTrainer.isSelected();
		Date selectedDate = (java.util.Date) datePicker.getModel().getValue();

		if (email.isEmpty() || password.isEmpty() || izena.isEmpty() || abizena1.isEmpty() || abizena2.isEmpty()
				|| selectedDate == null) {
			JOptionPane.showMessageDialog(this, "Datu Guztiak Bete.");
			return;
		}

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
		String birthdateString = sdf.format(selectedDate);

		try {
			dbConnection.createUser(izena, abizena1, abizena2, email, password, birthdateString, isTrainer);
			JOptionPane.showMessageDialog(this, "Registratu zara");
			dispose();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errorea registratzen.", "Errorea", JOptionPane.ERROR_MESSAGE);
		}
	}

	@SuppressWarnings("serial")
	class DateToStringFormatter extends DateComponentFormatter {
		private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");

		@Override
		public String valueToString(Object value) throws java.text.ParseException {
			if (value instanceof java.util.Date) {
				return sdf.format((java.util.Date) value);
			} else if (value instanceof java.util.Calendar) {
				return sdf.format(((java.util.Calendar) value).getTime());
			}
			return super.valueToString(value);
		}
	}
}