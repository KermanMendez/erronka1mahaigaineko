package view;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Profile extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtSurname1;
	private JTextField txtSurname2;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				Profile frame = new Profile();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

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

		txtName = new JTextField();
		txtName.setBounds(226, 30, 142, 20);
		txtName.setColumns(10);
		contentPane.add(txtName);

		txtSurname1 = new JTextField();
		txtSurname1.setBounds(226, 55, 142, 20);
		txtSurname1.setColumns(10);
		contentPane.add(txtSurname1);

		txtSurname2 = new JTextField();
		txtSurname2.setBounds(226, 80, 142, 20);
		txtSurname2.setColumns(10);
		contentPane.add(txtSurname2);

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