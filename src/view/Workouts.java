package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import controller.AppState;
import model.Routines;
import model.UIStyle;

public class Workouts extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel edukiontzia;
	private JComboBox<String> comboMaila;
	public static JList<String> listaWorkout;
	private JButton btnIkusiHistoria;
	private JButton btnHasiWorkout;
	private JButton btnIkusiAriketak;
	private JLabel lblMailaAktuala;
	private Routines routines = new Routines();
	private LoginFrame login = new LoginFrame();

	public Workouts() {
		setTitle("Workouts");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		setLocationRelativeTo(null);

		edukiontzia = new JPanel();
		UIStyle.stylePanel(edukiontzia);
		setContentPane(edukiontzia);
		edukiontzia.setLayout(null);

		JLabel lblTitulua = new JLabel("Workouts");
		lblTitulua.setFont(UIStyle.TITLE_FONT);
		lblTitulua.setForeground(UIStyle.PRIMARY);
		lblTitulua.setBounds(240, 10, 200, 30);
		edukiontzia.add(lblTitulua);

		lblMailaAktuala = new JLabel("Maila: 1");
		lblMailaAktuala.setBounds(30, 60, 150, 20);
		UIStyle.styleLabel(lblMailaAktuala, false);
		edukiontzia.add(lblMailaAktuala);

		JLabel lblIragazi = new JLabel("Zure maila aukeratu:");
		lblIragazi.setBounds(30, 100, 150, 20);
		UIStyle.styleLabel(lblIragazi, false);
		edukiontzia.add(lblIragazi);

		comboMaila = new JComboBox<>();
		comboMaila.setModel(new DefaultComboBoxModel<>(
				new String[] { "1. maila", "2. maila", "3. maila", "4. maila", "5. maila" }));
		comboMaila.setBounds(180, 100, 120, 22);
		UIStyle.styleField(comboMaila);
		comboMaila.setToolTipText("Selecciona tu nivel");
		edukiontzia.add(comboMaila);

		JComboBox<String> comboMailaRutinakLevel = new JComboBox<String>();
		try {
			comboMailaRutinakLevel
					.setModel(new DefaultComboBoxModel<>(routines.getRoutines(comboMaila.getSelectedIndex() + 1)));
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		comboMailaRutinakLevel.setBounds(337, 99, 120, 22);
		UIStyle.styleField(comboMailaRutinakLevel);
		comboMailaRutinakLevel.setToolTipText("Selecciona rutina");
		edukiontzia.add(comboMailaRutinakLevel);

		JLabel lblZerrenda = new JLabel("Workouts zerrenda:");
		lblZerrenda.setBounds(30, 140, 150, 20);
		UIStyle.styleLabel(lblZerrenda, false);
		edukiontzia.add(lblZerrenda);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 160, 250, 150);
		edukiontzia.add(scrollPane);

		listaWorkout = new JList<>();
		UIStyle.styleField(listaWorkout);
		scrollPane.setViewportView(listaWorkout);

		Runnable listaEguneratu = () -> {
			int nivelSeleccionado = comboMaila.getSelectedIndex() + 1;
			Object selectedItem = comboMailaRutinakLevel.getSelectedItem();

			if (selectedItem == null) {
				return;
			}

			String nivelText = selectedItem.toString();

			new Thread(() -> {
				try {
					String[] ariketak = routines.getLevels(nivelSeleccionado, nivelText);
					SwingUtilities.invokeLater(() -> {
						listaWorkout.setModel(new AbstractListModel<String>() {
							private static final long serialVersionUID = 1L;
							String[] balioak = ariketak;

							public int getSize() {
								return balioak.length;
							}

							public String getElementAt(int index) {
								return balioak[index];
							}
						});
					});
				} catch (InterruptedException | ExecutionException ex) {
					ex.printStackTrace();
				}
			}).start();
		};

		listaEguneratu.run();

		comboMaila.addActionListener(e -> {
			int nivelSeleccionado = comboMaila.getSelectedIndex() + 1;
			lblMailaAktuala.setText("Maila: " + nivelSeleccionado);

			new Thread(() -> {
				try {
					String[] workouts = routines.getRoutines(nivelSeleccionado);
					SwingUtilities.invokeLater(() -> {
						comboMailaRutinakLevel.setModel(new DefaultComboBoxModel<>(workouts));
						listaEguneratu.run();
					});
				} catch (InterruptedException | ExecutionException ex) {
					ex.printStackTrace();
				}
			}).start();
		});

		comboMailaRutinakLevel.addActionListener(e -> {
			listaEguneratu.run();
		});

		btnIkusiHistoria = new JButton("Ikusi historia");
		UIStyle.styleButton(btnIkusiHistoria);
		btnIkusiHistoria.setToolTipText("Ver historial de entrenamientos");
		btnIkusiHistoria.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnIkusiHistoria.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnIkusiHistoria.setBackground(UIStyle.ACCENT);
				btnIkusiHistoria.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnIkusiHistoria.setBackground(UIStyle.BUTTON_BG);
				btnIkusiHistoria.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnIkusiHistoria.setBounds(350, 160, 180, 30);
		edukiontzia.add(btnIkusiHistoria);

		btnHasiWorkout = new JButton("Hasi Workout-a");
		UIStyle.styleButton(btnHasiWorkout);
		btnHasiWorkout.setToolTipText("Comenzar entrenamiento");
		btnHasiWorkout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnHasiWorkout.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnHasiWorkout.setBackground(UIStyle.ACCENT);
				btnHasiWorkout.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnHasiWorkout.setBackground(UIStyle.BUTTON_BG);
				btnHasiWorkout.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnHasiWorkout.addActionListener(e -> {
			ThreadFrame threadFrame = new ThreadFrame(comboMaila.getSelectedIndex() + 1,
					comboMailaRutinakLevel.getSelectedItem().toString());
			threadFrame.setVisible(true);
			dispose();
		});
		btnHasiWorkout.setBounds(350, 200, 180, 30);
		edukiontzia.add(btnHasiWorkout);

		btnIkusiAriketak = new JButton("Ikusi ariketak");
		UIStyle.styleButton(btnIkusiAriketak);
		btnIkusiAriketak.setToolTipText("Aukeratutako workout-aren ariketak ikusi");
		btnIkusiAriketak.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnIkusiAriketak.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnIkusiAriketak.setBackground(UIStyle.ACCENT);
				btnIkusiAriketak.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnIkusiAriketak.setBackground(UIStyle.BUTTON_BG);
				btnIkusiAriketak.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnIkusiAriketak.setBounds(350, 240, 180, 30);
		edukiontzia.add(btnIkusiAriketak);

		JButton btnLogout = new JButton("Logout");
		UIStyle.styleButton(btnLogout);
		btnLogout.setToolTipText("Saioa itxi");
		btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLogout.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnLogout.setBackground(UIStyle.ACCENT);
				btnLogout.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnLogout.setBackground(UIStyle.BUTTON_BG);
				btnLogout.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnLogout.addActionListener(e -> {
			dispose();
			login.setVisible(true);
		});
		btnLogout.setBounds(485, 18, 89, 23);
		edukiontzia.add(btnLogout);

		getContentPane().setBackground(UIStyle.BACKGROUND);
	}

	public static void main(String[] args) {
		if (!AppState.isAppStarted()) {
			System.exit(0);
		}
		EventQueue.invokeLater(() -> {
			try {
				Workouts frame = new Workouts();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}