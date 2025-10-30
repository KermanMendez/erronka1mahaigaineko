package view;

import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.UIStyle;

public class Inter extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public Inter(Boolean isTrainer, Boolean connect) {

		setTitle("Ongi Etorri LRLL");
		setIconImage(Toolkit.getDefaultToolkit().getImage(Inter.class.getResource("/img/logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		contentPane = new JPanel();
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnProfile = new JButton("Profila");
		UIStyle.styleButton(btnProfile);
		btnProfile.setToolTipText("Zure profila ikusi");
		UIStyle.addHoverEffect(btnProfile);

		btnProfile.addActionListener(e -> {
			Profile profile = new Profile(isTrainer, connect);
			dispose();
			profile.setVisible(true);
		});

		btnProfile.setBounds(64, 76, 117, 84);
		contentPane.add(btnProfile);

		JButton btnWorkouts = new JButton("Workouts");
		UIStyle.styleButton(btnWorkouts);
		btnWorkouts.setToolTipText("zure ariketak ikusi");
		UIStyle.addHoverEffect(btnWorkouts);

		btnWorkouts.addActionListener(e -> {
			Workouts workouts = new Workouts(isTrainer, connect);
			dispose();
			workouts.setVisible(true);
		});

		btnWorkouts.setBounds(258, 76, 117, 84);
		contentPane.add(btnWorkouts);

		JButton btnAdmin = new JButton("ADMIN");
		UIStyle.styleButton(btnAdmin);
		btnAdmin.setToolTipText("Administrazio panela");
		UIStyle.addHoverEffect(btnAdmin);

		btnAdmin.setBounds(359, 203, 65, 47);
		contentPane.add(btnAdmin);
		if (isTrainer) {
			btnAdmin.setVisible(true);
		} else {
			btnAdmin.setVisible(false);
		}

		ImageIcon profileIcon = new ImageIcon(getClass().getResource("/img/profile_icon.png"));
		Image scaledProfileImage = profileIcon.getImage().getScaledInstance(97, 64, Image.SCALE_SMOOTH);
		btnProfile.setIcon(new ImageIcon(scaledProfileImage));
		btnProfile.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(btnProfile);

		ImageIcon workoutIcon = new ImageIcon(getClass().getResource("/img/workout_icon.png"));
		Image scaledWorkoutImage = workoutIcon.getImage().getScaledInstance(97, 64, Image.SCALE_SMOOTH);
		btnWorkouts.setIcon(new ImageIcon(scaledWorkoutImage));
		btnWorkouts.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(btnWorkouts);

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}
}