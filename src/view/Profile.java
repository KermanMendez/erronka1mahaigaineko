package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.AppState;
import controller.MainApp;
import model.UIStyle;

public class Profile extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtSurname1;
	private JTextField txtSurname2;

	public Profile(Boolean isTrainer, Boolean connect) {
		setTitle("Zure Profila");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		ImageIcon icon = new ImageIcon(getClass().getResource("/img/atzera.png"));

		Image scaledImage = icon.getImage().getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImage);

		JButton btnAtzera = new JButton(scaledIcon);
		btnAtzera.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Inter inter = new Inter(isTrainer, connect);
				inter.setVisible(true);
				dispose();
			}
		});
		btnAtzera.setBounds(10, 10, 40, 40);
		btnAtzera.setBorderPainted(false);
		btnAtzera.setContentAreaFilled(false);
		btnAtzera.setFocusPainted(false);
		btnAtzera.setOpaque(false);
		contentPane.add(btnAtzera);

		JLabel lblIzenaP = new JLabel("Izena");
		lblIzenaP.setBounds(89, 33, 92, 14);
		UIStyle.styleLabel(lblIzenaP, false);
		contentPane.add(lblIzenaP);

		JLabel lblAbizena1P = new JLabel("Abizena:");
		lblAbizena1P.setBounds(89, 58, 92, 14);
		UIStyle.styleLabel(lblAbizena1P, false);
		contentPane.add(lblAbizena1P);

		JLabel lblAbizena2P = new JLabel("2º Abizena:");
		lblAbizena2P.setBounds(89, 83, 92, 14);
		UIStyle.styleLabel(lblAbizena2P, false);
		contentPane.add(lblAbizena2P);

		txtName = new JTextField();
		txtName.setBounds(226, 30, 142, 20);
		UIStyle.styleField(txtName);
		txtName.setColumns(10);
		txtName.setToolTipText("Izena");
		contentPane.add(txtName);

		txtSurname1 = new JTextField();
		txtSurname1.setBounds(226, 55, 142, 20);
		UIStyle.styleField(txtSurname1);
		txtSurname1.setColumns(10);
		txtSurname1.setToolTipText("Lehenengo Abizena");
		contentPane.add(txtSurname1);

		txtSurname2 = new JTextField();
		txtSurname2.setBounds(226, 80, 142, 20);
		UIStyle.styleField(txtSurname2);
		txtSurname2.setColumns(10);
		txtSurname2.setToolTipText("Bigarren");
		contentPane.add(txtSurname2);

		JButton btnOnartuP = new JButton("Onartu");
		UIStyle.styleButton(btnOnartuP);
		btnOnartuP.setToolTipText("Aldaketak Gorde");
		btnOnartuP.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnOnartuP.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnOnartuP.setBackground(UIStyle.ACCENT);
				btnOnartuP.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnOnartuP.setBackground(UIStyle.BUTTON_BG);
				btnOnartuP.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnOnartuP.setBounds(127, 227, 89, 30);
		contentPane.add(btnOnartuP);

		JButton btnUtziP = new JButton("Utzi");
		UIStyle.styleButton(btnUtziP);
		btnUtziP.setToolTipText("Aldaketak Utzi");
		btnUtziP.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnUtziP.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnUtziP.setBackground(UIStyle.ACCENT);
				btnUtziP.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnUtziP.setBackground(UIStyle.BUTTON_BG);
				btnUtziP.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnUtziP.setBounds(226, 227, 89, 30);
		contentPane.add(btnUtziP);

		JButton btnChangePass = new JButton("Aldatu Pasahitza");
		UIStyle.styleButton(btnChangePass);
		btnChangePass.setToolTipText("Pasahitza Aldatu");
		btnChangePass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnChangePass.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnChangePass.setBackground(UIStyle.ACCENT);
				btnChangePass.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnChangePass.setBackground(UIStyle.BUTTON_BG);
				btnChangePass.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnChangePass.setBounds(160, 128, 125, 30);
		contentPane.add(btnChangePass);

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}

	public static void main(String[] args) {
		if (!AppState.isAppStarted()) {
			MainApp.main(args);
			return;
		}
		EventQueue.invokeLater(() -> {
			try {
				Profile frame = new Profile(Boolean.FALSE, Boolean.TRUE);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}