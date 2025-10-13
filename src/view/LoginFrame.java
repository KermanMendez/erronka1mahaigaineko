package view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;

public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFieldUser;
	private JPasswordField passwordField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginFrame frame = new LoginFrame();
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
	public LoginFrame() {
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\in2dm3-d.ELORRIETA\\Desktop\\KermanMendez\\Erronka1\\src\\img\\logo.png"));
		setTitle("LOGIN");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel labelUser = new JLabel("User:");
		labelUser.setBounds(40, 54, 90, 14);
		contentPane.add(labelUser);
		
		JLabel labelPassword = new JLabel("Password:");
		labelPassword.setBounds(40, 110, 87, 14);
		contentPane.add(labelPassword);
		
		textFieldUser = new JTextField();
		textFieldUser.setBounds(40, 79, 136, 20);
		contentPane.add(textFieldUser);
		textFieldUser.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(40, 135, 136, 20);
		contentPane.add(passwordField);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(158, 213, 89, 23);
		contentPane.add(btnLogin);
		
		JLabel lblNewLabel = new JLabel("");
		ImageIcon originalIcon = new ImageIcon("C:\\Users\\in2dm3-d.ELORRIETA\\Desktop\\KermanMendez\\Erronka1\\src\\img\\logo.png");

		java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(250, 180, java.awt.Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImage);
		lblNewLabel.setIcon(scaledIcon);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(207, 22, 217, 180);
		contentPane.add(lblNewLabel);
	}
}
