package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

import model.Exercise;
import model.Hariak;

public class ThreadFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private Hariak hariak;
	private final Object pauseLock = new Object();
	private boolean paused = false;
	private boolean skipRestRequested = false;
	private boolean stopRequested = false;

	private JLabel labelTotal = new JLabel("");
	private JLabel labelSeries = new JLabel("");
	private JLabel labelDescansos = new JLabel("");

	public ThreadFrame(int level, String routineName, Boolean isTrainer) {
		setTitle("Workout Dashboard - " + routineName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(900, 600);
		setLayout(new BorderLayout(10, 10));

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
		JButton btnPause = new JButton("Pausa / Reanudar");
		btnPause.addActionListener(e -> {
			paused = !paused;
			if (!paused) {
				synchronized (pauseLock) {
					pauseLock.notifyAll();
				}
			}
		});

		JButton btnSkip = new JButton("Saltar atseden");
		btnSkip.addActionListener(e -> skipRestRequested = true);

		topPanel.add(btnPause);
		topPanel.add(btnSkip);
		add(topPanel, BorderLayout.NORTH);

	JPanel centerPanel = new JPanel();
	centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
	centerPanel.add(createLabelPanel("⏱ Denbora totala", labelTotal));
	centerPanel.add(createLabelPanel("💪 Serieak", labelSeries));
	centerPanel.add(createLabelPanel("😴 Atsedenak", labelDescansos));
		add(centerPanel, BorderLayout.CENTER);

		JButton btnAmaitu = new JButton("Amaitu rutina");
		btnAmaitu.addActionListener(e -> {
			stopRequested = true;
			hariak.historyLog(routineName);
			dispose();
		});

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(btnAmaitu);
		add(bottomPanel, BorderLayout.SOUTH);

		hariak = new Hariak();

		new Thread(() -> {
			try {
				List<Exercise> exercises = hariak.start(level, routineName);
				hariak.startExerciseThreads(exercises, labelTotal, labelSeries, labelDescansos, () -> stopRequested,
						() -> {
							if (skipRestRequested) {
								skipRestRequested = false;
								return true;
							}
							return false;
						}, () -> paused, pauseLock, false, true, true);
			} catch (InterruptedException | ExecutionException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	private JPanel createLabelPanel(String title, JLabel content) {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		panel.add(label, BorderLayout.NORTH);

		content.setHorizontalAlignment(SwingConstants.CENTER);
		content.setPreferredSize(new Dimension(280, 400));
		panel.add(content, BorderLayout.CENTER);
		return panel;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			ThreadFrame frame = new ThreadFrame(1, "Nire rutina", Boolean.FALSE);
			frame.setVisible(true);
		});
	}
}
