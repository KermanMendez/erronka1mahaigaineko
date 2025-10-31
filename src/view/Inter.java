package view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.Theme;
import model.UIStyle;

public class Inter extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public Inter(Boolean isTrainer, Boolean connect) {
		Theme.apply();

		setTitle("Ongi Etorri LRLL");
		setIconImage(Toolkit.getDefaultToolkit().getImage(Inter.class.getResource("/img/logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(520, 360);
		setLocationRelativeTo(null);

		contentPane = new JPanel(new GridBagLayout());
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(12, 12, 12, 12);
		gbc.fill = GridBagConstraints.BOTH;

		JButton btnProfile = new JButton("Profila");
		UIStyle.styleButton(btnProfile);
		btnProfile.setToolTipText("Zure profila ikusi");
		UIStyle.addHoverEffect(btnProfile);
		btnProfile.addActionListener(e -> {
			Profile profile = new Profile();
			profile.setVisible(true);
			dispose();
		});
		ImageIcon profileIcon = new ImageIcon(getClass().getResource("/img/profile_icon.png"));
		Image scaledProfileImage = profileIcon.getImage().getScaledInstance(80, 54, Image.SCALE_SMOOTH);
		btnProfile.setIcon(new ImageIcon(scaledProfileImage));
		btnProfile.setHorizontalAlignment(SwingConstants.CENTER);

		JButton btnWorkouts = new JButton("Workouts");
		UIStyle.styleButton(btnWorkouts);
		btnWorkouts.setToolTipText("zure ariketak ikusi");
		UIStyle.addHoverEffect(btnWorkouts);
		btnWorkouts.addActionListener(e -> {
			Workouts workouts = new Workouts(isTrainer, connect);
			workouts.setVisible(true);
			dispose();
		});
		ImageIcon workoutIcon = new ImageIcon(getClass().getResource("/img/workout_icon.png"));
		Image scaledWorkoutImage = workoutIcon.getImage().getScaledInstance(80, 54, Image.SCALE_SMOOTH);
		btnWorkouts.setIcon(new ImageIcon(scaledWorkoutImage));
		btnWorkouts.setHorizontalAlignment(SwingConstants.CENTER);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		gbc.weighty = 1.0;
		GridBagConstraints gbc_btnProfile = new GridBagConstraints();
		gbc_btnProfile.gridx = 0;
		contentPane.add(btnProfile, gbc_btnProfile);

		gbc.gridx = 1;
		contentPane.add(btnWorkouts, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}
}