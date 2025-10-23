package view;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controller.AppState;
import controller.MainApp;
import model.Exercise;
import model.Hariak;

public class ThreadFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private Hariak hariak;
	private boolean paused = false;
	private boolean skipRestRequested = false;
	private final Object pauseLock = new Object();
	private boolean stopRequested = false;

	private JLabel labelTotala = new JLabel("");
	private JLabel labelSerieak = new JLabel("");
	private JLabel labelAtsedenak = new JLabel("");
	private JLabel labelHasiera = new JLabel("");

	public ThreadFrame(int level, String routineName, Boolean isTrainer) {
		setTitle(" Workout - " + routineName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(693, 490);
		setLocationRelativeTo(null);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel infoPanel = new JPanel(new GridLayout(1, 3, 15, 15));
		infoPanel.setBounds(54, 181, 554, 35);

		labelTotala.setBorder(BorderFactory.createTitledBorder("⏱️ Total"));
		labelTotala.setFont(new Font("SansSerif", Font.BOLD, 12));
		labelTotala.setVisible(false);

		labelSerieak.setBorder(BorderFactory.createTitledBorder("🏋️ Serieak"));
		labelSerieak.setFont(new Font("SansSerif", Font.BOLD, 12));
		labelSerieak.setVisible(false);

		labelAtsedenak.setBorder(BorderFactory.createTitledBorder("💤 Atsedenak"));
		labelAtsedenak.setFont(new Font("SansSerif", Font.BOLD, 12));
		labelAtsedenak.setVisible(false);

		labelHasiera.setFont(new Font("SansSerif", Font.BOLD, 12));
		labelHasiera.setBounds(238, 128, 159, 42);

		infoPanel.add(labelTotala);
		infoPanel.add(labelSerieak);
		infoPanel.add(labelAtsedenak);

		contentPane.add(labelHasiera);

		contentPane.add(infoPanel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		buttonPanel.setBounds(54, 380, 554, 42);

		JButton btnPause = new JButton("Pausatu / Jarraitu");
		btnPause.setFocusPainted(false);
		btnPause.addActionListener(e -> {
			paused = !paused;
			if (!paused) {
				synchronized (pauseLock) {
					pauseLock.notifyAll();
				}
			}
		});
		buttonPanel.add(btnPause);

		JButton btnSkip = new JButton("Atsedena saltatu");
		btnSkip.setFocusPainted(false);
		btnSkip.addActionListener(e -> skipRestRequested = true);
		buttonPanel.add(btnSkip);

		JButton btnAmaitu = new JButton("Amaitu rutina");
		btnAmaitu.setFocusPainted(false);
		btnAmaitu.addActionListener(e -> {
			stopRequested = true;
			hariak.historyLog(routineName);
			Workouts workoutsView = new Workouts(isTrainer);
			workoutsView.setVisible(true);
			dispose();
		});
		buttonPanel.add(btnAmaitu);

		contentPane.add(buttonPanel);

		hariak = new Hariak();
		new Thread(() -> {
			try {
				List<Exercise> exercises = hariak.start(level, routineName);
				hariak.startExerciseThreads(exercises, labelTotala, labelSerieak, labelAtsedenak, labelHasiera,
						() -> stopRequested, () -> {
							if (skipRestRequested) {
								skipRestRequested = false;
								return true;
							}
							return false;
						}, () -> paused, pauseLock, true, true, true);
			} catch (InterruptedException | ExecutionException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			if (!AppState.isAppStarted()) {
				MainApp.main(args);
				return;
			}
			try {
				ThreadFrame frame = new ThreadFrame(1, "Nire rutina", Boolean.FALSE);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
