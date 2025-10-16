package view;

import java.util.Properties;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jdatepicker.impl.DateComponentFormatter;

import controller.DBConnection;


public class RegisterDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldEmail;
	private JPasswordField passwordField;
	private DBConnection dbConnection = new DBConnection();
	private JTextField abizena1Field;
	private JTextField abizena2Field;
	private JDatePickerImpl datePicker;


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
		lblAbizena1.setBounds(10, 92, 150, 25);
		getContentPane().add(lblAbizena1);
		
		JLabel lblAbizena2 = new JLabel("2º Abizena:");
		lblAbizena2.setBounds(10, 127, 150, 25);
		getContentPane().add(lblAbizena2);
		
		abizena1Field = new JTextField();
		abizena1Field.setBounds(170, 92, 200, 25);
		getContentPane().add(abizena1Field);
		
		abizena2Field = new JTextField();
		abizena2Field.setBounds(170, 127, 200, 25);
		getContentPane().add(abizena2Field);
		
		JLabel labelData = new JLabel("Jaiotze Data:");
		labelData.setBounds(10, 162, 150, 25);
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
			datePicker.setBounds(170, 162, 200, 40);
			getContentPane().add(datePicker);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error initializing date picker: " + ex.getMessage(), "Date Picker Error", JOptionPane.ERROR_MESSAGE);
		}
		

	}

	private void registrarSolicitud() {
		String email = textFieldEmail.getText().trim();
		String password = new String(passwordField.getPassword());
		// Example: Retrieve selected date
		java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
		// You can use selectedDate as needed

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
	
	// Custom formatter for Date.toString() format but without time
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