package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import controller.AppState;
import controller.MainApp;
import model.ReadBackup;
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
	private LoginFrame login = new LoginFrame(Boolean.TRUE);

	public Workouts(Boolean isTrainer, Boolean connect) {
		Routines routines = new Routines(connect);
		setTitle("Workouts");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		setLocationRelativeTo(null);

		edukiontzia = new JPanel();
		UIStyle.stylePanel(edukiontzia);
		setContentPane(edukiontzia);
		edukiontzia.setLayout(null);

		ImageIcon icon = new ImageIcon(getClass().getResource("/img/atzera.png"));

		Image scaledImage = icon.getImage().getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImage);

		JButton btnAtzera = new JButton(scaledIcon);
		btnAtzera.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Inter inter = new Inter(isTrainer, connect);
				inter.setVisible(true);
				dispose();
			}
		});
		btnAtzera.setBounds(10, 10, 40, 40);
		btnAtzera.setBorderPainted(false);
		btnAtzera.setContentAreaFilled(false);
		btnAtzera.setFocusPainted(false);
		btnAtzera.setOpaque(false);
		edukiontzia.add(btnAtzera);

		JLabel lblTitulua = new JLabel("Workouts");
		lblTitulua.setFont(UIStyle.TITLE_FONT);
		lblTitulua.setForeground(UIStyle.PRIMARY);
		lblTitulua.setBounds(240, 10, 111, 30);
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
		comboMaila.setModel(new DefaultComboBoxModel<>(routines.levels()));
		comboMaila.setBounds(180, 100, 120, 22);
		UIStyle.styleField(comboMaila);
		comboMaila.setToolTipText("Zure maila aukeratu");
		edukiontzia.add(comboMaila);

		JComboBox<String> comboMailaRutinakLevel = new JComboBox<String>();
		try {
			comboMailaRutinakLevel.setModel(
					new DefaultComboBoxModel<>(routines.getRoutines(comboMaila.getSelectedIndex() + 1, connect)));
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		comboMailaRutinakLevel.setBounds(345, 99, 210, 22);
		UIStyle.styleField(comboMailaRutinakLevel);
		comboMailaRutinakLevel.setToolTipText("Entrenamendu mota aukeratu");
		edukiontzia.add(comboMailaRutinakLevel);

		JLabel lblZerrenda = new JLabel("Workouts zerrenda:");
		lblZerrenda.setBounds(30, 140, 150, 20);
		UIStyle.styleLabel(lblZerrenda, false);
		edukiontzia.add(lblZerrenda);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 160, 525, 150);
		edukiontzia.add(scrollPane);

		listaWorkout = new JList<>();
		UIStyle.styleField(listaWorkout);
		scrollPane.setViewportView(listaWorkout);

		Runnable listaEguneratu = () -> {
			int aukeratutakoMaila = comboMaila.getSelectedIndex() + 1;
			Object selectedItem = comboMailaRutinakLevel.getSelectedItem();

			if (selectedItem == null) {
				return;
			}

			String nivelText = selectedItem.toString();

			new Thread(() -> {
				try {
					String[] ariketak = routines.getLevels(aukeratutakoMaila, nivelText, connect);
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
					String[] workouts = routines.getRoutines(nivelSeleccionado, connect);
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
		btnIkusiHistoria.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ReadBackup backup = new ReadBackup();
				backup.loadBackupData();
				backup.readBackup();
			}
		});
		UIStyle.styleButton(btnIkusiHistoria);
		btnIkusiHistoria.setToolTipText("Entrenamenduen historiala ikusi");
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
		btnIkusiHistoria.setBounds(30, 321, 160, 30);
		edukiontzia.add(btnIkusiHistoria);

		btnHasiWorkout = new JButton("Hasi Workout-a");
		UIStyle.styleButton(btnHasiWorkout);
		btnHasiWorkout.setToolTipText("Entrenamendua hasi");
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
					comboMailaRutinakLevel.getSelectedItem().toString(), isTrainer, connect);
			threadFrame.setVisible(true);
			dispose();
		});
		btnHasiWorkout.setBounds(212, 321, 160, 30);
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
		btnIkusiAriketak.setBounds(395, 321, 160, 30);
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
			MainApp.main(args);
			return;
		}
		EventQueue.invokeLater(() -> {
			try {
				Workouts frame = new Workouts(Boolean.FALSE, Boolean.TRUE);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}