package view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Inter extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public Inter(Boolean connect) {

		setTitle("Ongi Etorri LRLL");
		setIconImage(Toolkit.getDefaultToolkit().getImage(Inter.class.getResource("/img/logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 450);
		setLocationRelativeTo(null);

		contentPane = new JPanel(new BorderLayout(12, 12));
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		UIStyle.stylePanel(topPanel);

		JLabel lblTitle = new JLabel("LONG RING LONG LAND GYM");
		lblTitle.setFont(UIStyle.TITLE_FONT);
		lblTitle.setForeground(UIStyle.PRIMARY);
		lblTitle.setAlignmentX(CENTER_ALIGNMENT);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		topPanel.add(Box.createVerticalStrut(20));
		topPanel.add(lblTitle);
		topPanel.add(Box.createVerticalStrut(20));

		contentPane.add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new GridBagLayout());
		UIStyle.stylePanel(centerPanel);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(20, 20, 20, 20);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		JButton btnProfile = new JButton("Profila");
		UIStyle.styleButton(btnProfile);
		btnProfile.setToolTipText("Zure profila ikusi eta editatu");
		UIStyle.addHoverEffect(btnProfile);
		btnProfile.addActionListener(e -> {
			Profile profile = new Profile(connect);
			profile.setVisible(true);
			dispose();
		});

		ImageIcon profileIcon = new ImageIcon(getClass().getResource("/img/profile_icon.png"));
		Image scaledProfileImage = profileIcon.getImage().getScaledInstance(80, 54, Image.SCALE_SMOOTH);
		btnProfile.setIcon(new ImageIcon(scaledProfileImage));
		btnProfile.setVerticalTextPosition(SwingConstants.BOTTOM);
		btnProfile.setHorizontalTextPosition(SwingConstants.CENTER);

		gbc.gridx = 0;
		gbc.gridy = 0;
		centerPanel.add(btnProfile, gbc);

		JButton btnWorkouts = new JButton("Workouts");
		UIStyle.styleButton(btnWorkouts);
		btnWorkouts.setToolTipText("Zure entrenamenduen errutina ikusi");
		UIStyle.addHoverEffect(btnWorkouts);
		btnWorkouts.addActionListener(e -> {
			Workouts workouts = new Workouts(connect);
			workouts.setVisible(true);
			dispose();
		});

		ImageIcon workoutIcon = new ImageIcon(getClass().getResource("/img/workout_icon.png"));
		Image scaledWorkoutImage = workoutIcon.getImage().getScaledInstance(80, 54, Image.SCALE_SMOOTH);
		btnWorkouts.setIcon(new ImageIcon(scaledWorkoutImage));
		btnWorkouts.setVerticalTextPosition(SwingConstants.BOTTOM);
		btnWorkouts.setHorizontalTextPosition(SwingConstants.CENTER);

		gbc.gridx = 1;
		gbc.gridy = 0;
		centerPanel.add(btnWorkouts, gbc);

		contentPane.add(centerPanel, BorderLayout.CENTER);

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}
}
