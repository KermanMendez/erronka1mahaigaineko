package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import controller.AppState;
import controller.MainApp;
import model.ConnectDB;
import model.DateFormater;
import model.UIStyle;
import java.awt.Toolkit;

public class RegisterDialog extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldEmail;
	private JPasswordField passwordField;
	private JTextField abizena1Field;
	private JTextField abizena2Field;
	private JDatePickerImpl datePicker;
	private JTextField textFieldIzena;
	private JCheckBox checkboxIsTrainer;
	private ConnectDB connectDB = new ConnectDB();

	public RegisterDialog(Boolean connect) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(RegisterDialog.class.getResource("/img/logo.png")));
		setTitle("Erabiltzailearen Registroa");
		setSize(399, 388);
		getContentPane().setLayout(null);
		if (getContentPane() instanceof JPanel) {
			UIStyle.stylePanel((JPanel) getContentPane());
		}

		JLabel labelEmail = new JLabel("Email:");
		labelEmail.setBounds(10, 22, 150, 25);
		UIStyle.styleLabel(labelEmail, false);
		getContentPane().add(labelEmail);

		textFieldEmail = new JTextField();
		textFieldEmail.setBounds(170, 22, 200, 25);
		UIStyle.styleField(textFieldEmail);
		textFieldEmail.setToolTipText("Zure email-a sartu");
		getContentPane().add(textFieldEmail);

		JLabel labelPass = new JLabel("Pasahitza:");
		labelPass.setBounds(10, 57, 150, 25);
		UIStyle.styleLabel(labelPass, false);
		getContentPane().add(labelPass);

		passwordField = new JPasswordField();
		passwordField.setBounds(170, 57, 200, 25);
		UIStyle.styleField(passwordField);
		passwordField.setToolTipText("Zure pasahitza sartu");
		getContentPane().add(passwordField);

		JLabel lblIzena = new JLabel("Izena:");
		lblIzena.setBounds(10, 87, 150, 25);
		UIStyle.styleLabel(lblIzena, false);
		getContentPane().add(lblIzena);

		textFieldIzena = new JTextField();
		textFieldIzena.setBounds(170, 87, 200, 25);
		UIStyle.styleField(textFieldIzena);
		textFieldIzena.setToolTipText("Izena");
		getContentPane().add(textFieldIzena);

		JLabel lblAbizena1 = new JLabel("Abizena:");
		lblAbizena1.setBounds(10, 122, 150, 25);
		UIStyle.styleLabel(lblAbizena1, false);
		getContentPane().add(lblAbizena1);

		abizena1Field = new JTextField();
		abizena1Field.setBounds(170, 122, 200, 25);
		UIStyle.styleField(abizena1Field);
		abizena1Field.setToolTipText("Lehenengo Abizena");
		getContentPane().add(abizena1Field);

		JLabel lblAbizena2 = new JLabel("Bigarren Abizena:");
		lblAbizena2.setBounds(10, 158, 150, 25);
		UIStyle.styleLabel(lblAbizena2, false);
		getContentPane().add(lblAbizena2);

		abizena2Field = new JTextField();
		abizena2Field.setBounds(170, 158, 200, 25);
		UIStyle.styleField(abizena2Field);
		abizena2Field.setToolTipText("Bigarren Abizena");
		getContentPane().add(abizena2Field);

		JLabel labelData = new JLabel("Jaiotze Data:");
		labelData.setBounds(10, 204, 150, 25);
		UIStyle.styleLabel(labelData, false);
		getContentPane().add(labelData);
		UtilDateModel model = new UtilDateModel();
		Properties p = new Properties();
		p.put("text.today", "Gaur");
		p.put("text.month", "Hilabetea");
		p.put("text.year", "Urtea");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		datePicker = new JDatePickerImpl(datePanel, new DateFormater());
		datePicker.setBounds(170, 204, 200, 25);
		UIStyle.styleField(datePicker);
		getContentPane().add(datePicker);

		JLabel lblTrainer = new JLabel("Entrenatzailea da?");
		lblTrainer.setBounds(10, 230, 150, 25);
		UIStyle.styleLabel(lblTrainer, false);
		getContentPane().add(lblTrainer);

		checkboxIsTrainer = new JCheckBox();
		checkboxIsTrainer.setBounds(170, 230, 200, 25);
		getContentPane().add(checkboxIsTrainer);

		JButton btnRegistrar = new JButton("Registratu");
		UIStyle.styleButton(btnRegistrar);
		btnRegistrar.setToolTipText("Erabiltzailea registratu");
		btnRegistrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnRegistrar.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnRegistrar.setBackground(UIStyle.ACCENT);
				btnRegistrar.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnRegistrar.setBackground(UIStyle.BUTTON_BG);
				btnRegistrar.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnRegistrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Boolean registroa = connectDB.eskaeraRegistratu(textFieldIzena.getText().trim(),
						abizena1Field.getText().trim(), abizena2Field.getText().trim(), textFieldEmail.getText().trim(),
						new String(passwordField.getPassword()), (java.util.Date) datePicker.getModel().getValue(),
						checkboxIsTrainer.isSelected(), connect);
				if (registroa) {
					dispose();
				}
			}
		});
		btnRegistrar.setBounds(170, 255, 160, 30);
		getContentPane().add(btnRegistrar);

		JButton btnCancelar = new JButton("Utzi");
		UIStyle.styleButton(btnCancelar);
		btnCancelar.setToolTipText("Saiakera utzi");
		btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCancelar.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnCancelar.setBackground(UIStyle.ACCENT);
				btnCancelar.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnCancelar.setBackground(UIStyle.BUTTON_BG);
				btnCancelar.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnCancelar.setBounds(170, 290, 160, 30);
		btnCancelar.addActionListener(e -> dispose());
		getContentPane().add(btnCancelar);

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}

	public static void main(String[] args) {
		if (!AppState.isAppStarted()) {
			MainApp.main(args);
			return;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RegisterDialog frame = new RegisterDialog(Boolean.TRUE);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
