package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.AppState;
import model.UIStyle;

public class Inter extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public Inter(Boolean isTrainer) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		contentPane = new JPanel();
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnProfile = new JButton("Profila");
		UIStyle.styleButton(btnProfile);
		btnProfile.setToolTipText("Zure profila ikusi");
		btnProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnProfile.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnProfile.setBackground(UIStyle.ACCENT);
				btnProfile.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnProfile.setBackground(UIStyle.BUTTON_BG);
				btnProfile.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnProfile.addActionListener(e -> {
			Profile profile = new Profile(isTrainer);
			dispose();
			profile.setVisible(true);
		});
		btnProfile.setBounds(38, 32, 117, 84);
		contentPane.add(btnProfile);

		JButton btnWorkouts = new JButton("Workouts");
		UIStyle.styleButton(btnWorkouts);
		btnWorkouts.setToolTipText("zure ariketak ikusi");
		btnWorkouts.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnWorkouts.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnWorkouts.setBackground(UIStyle.ACCENT);
				btnWorkouts.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnWorkouts.setBackground(UIStyle.BUTTON_BG);
				btnWorkouts.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnWorkouts.addActionListener(e -> {
			Workouts workouts = new Workouts(isTrainer);
			dispose();
			workouts.setVisible(true);
		});
		btnWorkouts.setBounds(38, 143, 117, 84);
		contentPane.add(btnWorkouts);

		JButton btnAdmin = new JButton("ADMIN");
		UIStyle.styleButton(btnAdmin);
		btnAdmin.setToolTipText("Administrazio panela");
		btnAdmin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnAdmin.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnAdmin.setBackground(UIStyle.ACCENT);
				btnAdmin.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnAdmin.setBackground(UIStyle.BUTTON_BG);
				btnAdmin.setForeground(UIStyle.BUTTON_FG);
			}
		});
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

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}

	public static void main(String[] args) {
		if (!AppState.isAppStarted()) {
			System.exit(0);
		}
		EventQueue.invokeLater(() -> {
			try {
				Inter frame = new Inter(Boolean.FALSE);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}