package view;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.UIStyle;

public class Profile extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtSurname1;
	private JTextField txtSurname2;

	public Profile(Boolean isTrainer, Boolean connect) {
		Theme.apply();

		setIconImage(Toolkit.getDefaultToolkit().getImage(Profile.class.getResource("/img/logo.png")));
		setTitle("Zure Profila");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(520, 360);
		setLocationRelativeTo(null);

		contentPane = new JPanel(new java.awt.BorderLayout(12, 12));
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);

		JPanel top = new JPanel(new java.awt.BorderLayout());
		UIStyle.stylePanel(top);
		JButton btnAtzera = new JButton(new ImageIcon(
				new ImageIcon(getClass().getResource("/img/atzera.png")).getImage().getScaledInstance(36, 36,
						java.awt.Image.SCALE_SMOOTH)));
		UIStyle.styleIconButton(btnAtzera);
		btnAtzera.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Inter inter = new Inter(isTrainer, connect);
				inter.setVisible(true);
				dispose();
			}
		});
		top.add(btnAtzera, java.awt.BorderLayout.WEST);
		contentPane.add(top, java.awt.BorderLayout.NORTH);

		JPanel form = new JPanel(new java.awt.GridBagLayout());
		UIStyle.stylePanel(form);
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.insets = new java.awt.Insets(8, 8, 8, 8);
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

		int row = 0;

		JLabel lblIzenaP = new JLabel("Izena");
		UIStyle.styleLabel(lblIzenaP, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(lblIzenaP, gbc);

		txtName = new JTextField();
		UIStyle.styleField(txtName);
		txtName.setToolTipText("Izena");
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(txtName, gbc);

		JLabel lblAbizena1P = new JLabel("Abizena:");
		UIStyle.styleLabel(lblAbizena1P, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(lblAbizena1P, gbc);

		txtSurname1 = new JTextField();
		UIStyle.styleField(txtSurname1);
		txtSurname1.setToolTipText("Lehenengo Abizena");
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(txtSurname1, gbc);

		JLabel lblAbizena2P = new JLabel("2º Abizena:");
		UIStyle.styleLabel(lblAbizena2P, false);
		gbc.gridx = 0;
		gbc.gridy = row;
		form.add(lblAbizena2P, gbc);

		txtSurname2 = new JTextField();
		UIStyle.styleField(txtSurname2);
		txtSurname2.setToolTipText("Bigarren");
		gbc.gridx = 1;
		gbc.gridy = row++;
		form.add(txtSurname2, gbc);

		JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 8));
		UIStyle.stylePanel(actions);
		JButton btnOnartuP = new JButton("Onartu");
		UIStyle.styleButton(btnOnartuP);
		btnOnartuP.setToolTipText("Aldaketak Gorde");
		UIStyle.addHoverEffect(btnOnartuP);

		JButton btnUtziP = new JButton("Utzi");
		UIStyle.styleButton(btnUtziP);
		btnUtziP.setToolTipText("Aldaketak Utzi");
		UIStyle.addHoverEffect(btnUtziP);

		JButton btnChangePass = new JButton("Aldatu Pasahitza");
		UIStyle.styleButton(btnChangePass);
		btnChangePass.setToolTipText("Pasahitza Aldatu");
		UIStyle.addHoverEffect(btnChangePass);

		actions.add(btnOnartuP);
		actions.add(btnUtziP);
		actions.add(btnChangePass);

		contentPane.add(form, java.awt.BorderLayout.CENTER);
		contentPane.add(actions, java.awt.BorderLayout.SOUTH);

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}
}