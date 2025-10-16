package view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

public class Profile extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Profile frame = new Profile();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Profile() {
		setTitle("Zure Profila");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblIzenaP = new JLabel("Izena");
		lblIzenaP.setBounds(89, 33, 92, 14);
		contentPane.add(lblIzenaP);
		
		JLabel lblAbizena1P = new JLabel("Abizena:");
		lblAbizena1P.setBounds(89, 58, 92, 14);
		contentPane.add(lblAbizena1P);
		
		JLabel lblAbizena2P = new JLabel("2º Abizena:");
		lblAbizena2P.setBounds(89, 83, 92, 14);
		contentPane.add(lblAbizena2P);
		
		textField = new JTextField();
		textField.setBounds(226, 30, 142, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(226, 55, 142, 20);
		contentPane.add(textField_1);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(226, 80, 142, 20);
		contentPane.add(textField_2);
		
		JButton btnOnartuP = new JButton("Onartu");
		btnOnartuP.setBounds(127, 227, 89, 23);
		contentPane.add(btnOnartuP);
		
		JButton btnUtziP = new JButton("Utzi");
		btnUtziP.setBounds(226, 227, 89, 23);
		contentPane.add(btnUtziP);
		
		JButton btnChangePass = new JButton("Aldatu Pasahitza");
		btnChangePass.setBounds(160, 128, 125, 23);
		contentPane.add(btnChangePass);

	}
}
