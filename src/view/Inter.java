package view;

import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Inter extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				Inter frame = new Inter();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Inter() {
		this(false);
	}

	public Inter(Boolean isTrainer) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnProfile = new JButton("Profila");
		btnProfile.addActionListener(e -> {
			Profile profile = new Profile();
			dispose();
			profile.setVisible(true);
		});
		btnProfile.setBounds(38, 32, 117, 84);
		contentPane.add(btnProfile);

		JButton btnWorkouts = new JButton("Workouts");
		btnWorkouts.addActionListener(e -> {
			Workouts workouts = new Workouts();
			dispose();
			workouts.setVisible(true);
		});
		btnWorkouts.setBounds(38, 143, 117, 84);
		contentPane.add(btnWorkouts);

		JButton btnAdmin = new JButton("ADMIIIIIIIIIIIIN");
		btnAdmin.setBounds(307, 32, 117, 84);
		contentPane.add(btnAdmin);
		if (isTrainer) {
			btnAdmin.setVisible(true);
		} else {
			btnAdmin.setVisible(false);
		}

		JLabel lblProfile = new JLabel("");
		lblProfile.setBounds(165, 32, 99, 84);
		ImageIcon profileIcon = new ImageIcon(getClass().getResource("/img/profile_icon.png"));
		Image scaledProfileImage = profileIcon.getImage().getScaledInstance(117, 84, Image.SCALE_SMOOTH);
		lblProfile.setIcon(new ImageIcon(scaledProfileImage));
		lblProfile.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblProfile);

		JLabel lblWorkout = new JLabel("");
		lblWorkout.setBounds(165, 143, 94, 84);
		ImageIcon workoutIcon = new ImageIcon(getClass().getResource("/img/workout_icon.png"));
		Image scaledWorkoutImage = workoutIcon.getImage().getScaledInstance(117, 84, Image.SCALE_SMOOTH);
		lblWorkout.setIcon(new ImageIcon(scaledWorkoutImage));
		lblWorkout.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblWorkout);
	}
}