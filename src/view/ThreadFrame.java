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
	private boolean pausatua = false;
	private boolean atsedenSaltatuEskaera = false;
	private boolean geldituEskaera = false;
	private Workouts workouts;

	private DefaultListModel<String> modelTotal = new DefaultListModel<>();
	private DefaultListModel<String> modelSeries = new DefaultListModel<>();
	private DefaultListModel<String> modelatsedens = new DefaultListModel<>();

	public ThreadFrame(int maila, String rutinaIzena, Boolean entrenatzaileaDa) {
		setTitle("Ariketa Kontrol Mahaia - " + rutinaIzena);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(900, 600);
		setLayout(new BorderLayout(10, 10));

		JPanel goikoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
		JButton btnPausa = new JButton("Pausatu / Berreskuratu");
		btnPausa.addActionListener(e -> {
			pausatua = !pausatua;
			if (!pausatua) {
				synchronized (pauseLock) {
					pauseLock.notifyAll();
				}
			}
		});

		JButton btnSaltatuatseden = new JButton("atseden Saltatu");
		btnSaltatuatseden.addActionListener(e -> atsedenSaltatuEskaera = true);

		goikoPanel.add(btnPausa);
		goikoPanel.add(btnSaltatuatseden);
		add(goikoPanel, BorderLayout.NORTH);

		JPanel erdikoPanel = new JPanel();
		erdikoPanel.setLayout(new BoxLayout(erdikoPanel, BoxLayout.X_AXIS));

		erdikoPanel.add(createListPanel("⏱ Denbora Totala", modelTotal));
		erdikoPanel.add(createListPanel("💪 Serieak", modelSeries));
		erdikoPanel.add(createListPanel("😴 atsedenak", modelatsedens));

		add(erdikoPanel, BorderLayout.CENTER);

		JButton btnAmaitu = new JButton("Rutina Amaitu");
		btnAmaitu.addActionListener(e -> {
			geldituEskaera = true;
			hariak.historyLog(rutinaIzena);
			JOptionPane.showMessageDialog(this, "Rutina amaitu eta historialean gordeta");
			workouts = new Workouts(entrenatzaileaDa);
			workouts.setVisible(true);
			dispose();
		});
		JPanel behekoPanel = new JPanel();
		behekoPanel.add(btnAmaitu);
		add(behekoPanel, BorderLayout.SOUTH);

		hariak = new Hariak();

		new Thread(() -> {
			try {
				List<Exercise> ariketak = hariak.start(maila, rutinaIzena);
				hariak.startExerciseThreads(ariketak, modelTotal, modelSeries, modelatsedens, () -> geldituEskaera,
						() -> {
							if (atsedenSaltatuEskaera) {
								atsedenSaltatuEskaera = false;
								return true;
							}
							return false;
						}, () -> pausatua, pauseLock, false, true, true);

			} catch (InterruptedException | ExecutionException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	private JPanel createListPanel(String title, DefaultListModel<String> model) {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		panel.add(label, BorderLayout.NORTH);

		JList<String> list = new JList<>(model);
		JScrollPane scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(280, 400));
		panel.add(scroll, BorderLayout.CENTER);
		return panel;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			ThreadFrame frame = new ThreadFrame(1, "Nire Rutina", Boolean.FALSE);
			frame.setVisible(true);
		});
	}
}
