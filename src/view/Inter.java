package view;

import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Inter extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Inter frame = new Inter();
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
	public Inter() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnNewButton = new JButton("Profila");
		btnNewButton.setBounds(38, 32, 117, 84);
		contentPane.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Workouts");
		btnNewButton_1.setBounds(38, 143, 117, 84);
		contentPane.add(btnNewButton_1);
		
		JLabel lblProfile = new JLabel("");
		lblProfile.setBounds(165, 32, 99, 84);

		ImageIcon originalIcon = new ImageIcon(getClass().getResource("/img/profile_icon.png"));
		Image scaledImage = originalIcon.getImage().getScaledInstance(117, 84, Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImage);
		lblProfile.setIcon(scaledIcon);
		lblProfile.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblProfile);
		
		JLabel lblWorkout = new JLabel("");
		lblWorkout.setBounds(165, 143, 94, 84);
		contentPane.add(lblWorkout);
		
		ImageIcon originalIcon2 = new ImageIcon(getClass().getResource("/img/workout_icon.png"));
		Image scaledImage2 = originalIcon2.getImage().getScaledInstance(117, 84, Image.SCALE_SMOOTH);
		ImageIcon scaledIcon2 = new ImageIcon(scaledImage2);
		lblWorkout.setIcon(scaledIcon2);
		lblWorkout.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblWorkout);

	}

}