package view;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.Exercise;
import model.Hariak;
import model.RoutineData;
import model.UIStyle;

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

	private JLabel lblRutinaIzena;
	private JLabel lblRutinaDeskribapena;
	private JLabel lblRutinaSets;

	public ThreadFrame(int level, String routineName, Boolean isTrainer, Boolean connect) {

		setIconImage(Toolkit.getDefaultToolkit().getImage(ThreadFrame.class.getResource("/img/logo.png")));
		setTitle(" Workout - " + routineName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(693, 490);
		setLocationRelativeTo(null);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Botón atrás
		JButton btnAtzera = new JButton(new ImageIcon(new ImageIcon(getClass().getResource("/img/atzera.png"))
				.getImage().getScaledInstance(36, 36, java.awt.Image.SCALE_SMOOTH)));
		btnAtzera.setBounds(10, 10, 36, 36);
		UIStyle.styleIconButton(btnAtzera);
		btnAtzera.addActionListener(e -> {
			stopRequested = true;
			Workouts workouts = new Workouts(isTrainer, connect);
			workouts.setVisible(true);
			dispose();
		});
		contentPane.add(btnAtzera);

		lblRutinaIzena = new JLabel(routineName);
		lblRutinaIzena.setBounds(54, 40, 300, 24);
		UIStyle.styleLabel(lblRutinaIzena, true);
		lblRutinaDeskribapena = new JLabel(" ");
		lblRutinaDeskribapena.setBounds(54, 70, 150, 20);
		UIStyle.styleLabel(lblRutinaDeskribapena, false);

		lblRutinaSets = new JLabel("Serieak");
		lblRutinaSets.setBounds(54, 100, 150, 20);
		UIStyle.styleLabel(lblRutinaSets, false);

		JPanel infoPanel = new JPanel(new GridLayout(1, 3, 15, 15));
		infoPanel.setBounds(54, 181, 554, 68);
		UIStyle.stylePanel(infoPanel);

		labelTotala.setBorder(BorderFactory.createTitledBorder("⏱️ Total"));
		labelTotala.setFont(new Font("SansSerif", Font.BOLD, 12));
		labelTotala.setVisible(false);
		UIStyle.styleField(labelTotala);

		labelSerieak.setBorder(BorderFactory.createTitledBorder("🏋️ Serieak"));
		labelSerieak.setFont(new Font("SansSerif", Font.BOLD, 12));
		labelSerieak.setVisible(false);
		UIStyle.styleField(labelSerieak);

		labelAtsedenak.setBorder(BorderFactory.createTitledBorder("💤 Atsedenak"));
		labelAtsedenak.setFont(new Font("SansSerif", Font.BOLD, 12));
		labelAtsedenak.setVisible(false);
		UIStyle.styleField(labelAtsedenak);

		labelHasiera.setFont(new Font("SansSerif", Font.BOLD, 12));
		labelHasiera.setBounds(260, 100, 320, 42);
		UIStyle.styleLabel(labelHasiera, false);

		infoPanel.add(labelTotala);
		infoPanel.add(labelSerieak);
		infoPanel.add(labelAtsedenak);

		contentPane.add(labelHasiera);

		contentPane.add(infoPanel);

		contentPane.add(lblRutinaIzena);
		contentPane.add(lblRutinaDeskribapena);
		contentPane.add(lblRutinaSets);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		buttonPanel.setBounds(54, 380, 554, 60);
		UIStyle.stylePanel(buttonPanel);

		JButton btnPause = new JButton("Pausatu / Jarraitu");
		UIStyle.styleButton(btnPause);
		UIStyle.addHoverEffect(btnPause);
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
		UIStyle.styleButton(btnSkip);
		UIStyle.addHoverEffect(btnSkip);
		btnSkip.addActionListener(e -> skipRestRequested = true);
		buttonPanel.add(btnSkip);

		JButton btnAmaitu = new JButton("Amaitu rutina");
		UIStyle.styleButton(btnAmaitu);
		UIStyle.addHoverEffect(btnAmaitu);
		btnAmaitu.addActionListener(e -> {
			stopRequested = true;
			Workouts workoutsView = new Workouts(isTrainer, connect);
			workoutsView.setVisible(true);
			dispose();
		});

		buttonPanel.add(btnAmaitu);
		contentPane.add(buttonPanel);

		hariak = new Hariak();
		new Thread(() -> {
			try {
				RoutineData result = hariak.loadRoutine(level, routineName, connect);
				List<Exercise> exercises = result.getExercises();
				String desc = result.getDescription();
				int totalSets = result.getTotalSets();

				final String description = (desc == null || desc.trim().isEmpty()) ? "Ez da deskripziorik aurkitu"
						: desc;
				final int finalTotalSets = totalSets;
				javax.swing.SwingUtilities.invokeLater(() -> {
					lblRutinaDeskribapena.setText(description);
					lblRutinaSets.setText("Serieak: " + finalTotalSets);
				});
				hariak.startExerciseThreads(exercises, labelTotala, labelSerieak, labelAtsedenak, labelHasiera,
						() -> stopRequested, () -> {
							if (skipRestRequested) {
								skipRestRequested = false;
								return true;
							}
							return false;
						}, () -> paused, pauseLock, routineName, true, true, true);
			} catch (InterruptedException | ExecutionException ex) {
				ex.printStackTrace();
			}
		}).start();
	}
}
