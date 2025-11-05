package view;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.Hariak;
import model.UIStyle;

public class ThreadFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private Hariak hariak = new Hariak();
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

	public ThreadFrame(int level, String routineName, Boolean connect) {

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
		btnPause.setEnabled(false);
		buttonPanel.add(btnPause);

		JButton btnSkip = new JButton("Atsedena saltatu");
		UIStyle.styleButton(btnSkip);
		UIStyle.addHoverEffect(btnSkip);
		btnSkip.addActionListener(e -> skipRestRequested = true);
		btnSkip.setEnabled(false);
		buttonPanel.add(btnSkip);

		JButton btnAmaitu = new JButton("Amaitu rutina");
		UIStyle.styleButton(btnAmaitu);
		UIStyle.addHoverEffect(btnAmaitu);
		btnAmaitu.addActionListener(e -> {
			stopRequested = true;
		});
		btnAmaitu.setEnabled(false);
		buttonPanel.add(btnAmaitu);
		contentPane.add(buttonPanel);

		hariak.executeWorkout(level, routineName, connect, labelTotala, labelSerieak, labelAtsedenak, labelHasiera,
				lblRutinaDeskribapena, lblRutinaSets, () -> stopRequested, () -> {
					if (skipRestRequested) {
						skipRestRequested = false;
						return true;
					}
					return false;
				}, () -> paused, pauseLock,
				() -> {
					btnPause.setVisible(true);
					btnPause.setEnabled(true);
					btnSkip.setVisible(true);
					btnSkip.setEnabled(true);
					btnAmaitu.setVisible(true);
					btnAmaitu.setEnabled(true);
				},
				() -> {
					Workouts workouts = new Workouts(connect);
					workouts.setVisible(true);
					dispose();
				});
	}
}
