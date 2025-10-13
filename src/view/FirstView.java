package view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FirstView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FirstView frame = new FirstView();
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
	public FirstView() {
		setTitle("LONG RING LONG LAND GYM");
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\in2dm3-d.ELORRIETA\\Desktop\\KermanMendez\\Erronka1\\src\\img\\logo.png"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 534, 343);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnEnter = new JButton("Enter");
		btnEnter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginFrame loginFrame = new LoginFrame();
				loginFrame.setVisible(true);
				dispose();
			}
		});
		btnEnter.setBounds(207, 255, 89, 23);
		contentPane.add(btnEnter);
		
		JLabel labelLogo = new JLabel("");

		ImageIcon originalIcon = new ImageIcon("C:\\Users\\in2dm3-d.ELORRIETA\\Desktop\\KermanMendez\\Erronka1\\src\\img\\logo.png");

		java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(300, 220, java.awt.Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImage);
		labelLogo.setIcon(scaledIcon);
		labelLogo.setHorizontalAlignment(SwingConstants.CENTER);
		labelLogo.setBounds(68, 11, 369, 218);
		contentPane.add(labelLogo);

	}
}