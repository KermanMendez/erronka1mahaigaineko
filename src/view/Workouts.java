package view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import model.Hariak;
import model.Routines;

public class Workouts extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel edukiontzia;
	private JComboBox<String> comboMaila;
	public static JList<String> listaWorkout;
	private JButton btnIkusiHistoria;
	private JButton btnHasiWorkout;
	private JButton btnIkusiAriketak;
	private JLabel lblMailaAktuala;
	private Hariak workoutThread = new Hariak("WorkoutThread");
	private Routines routines = new Routines();
	private LoginFrame login = new LoginFrame();

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				Workouts frame = new Workouts(false);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Workouts() {
		this(false);
	}

	public Workouts(boolean isAdmin) {
		setTitle("Workouts");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		setLocationRelativeTo(null);

		edukiontzia = new JPanel();
		edukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(edukiontzia);
		edukiontzia.setLayout(null);

		JLabel lblTitulua = new JLabel("Workouts");
		lblTitulua.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 20));
		lblTitulua.setBounds(240, 10, 200, 30);
		edukiontzia.add(lblTitulua);

		lblMailaAktuala = new JLabel("Maila: 1");
		lblMailaAktuala.setBounds(30, 60, 150, 20);
		edukiontzia.add(lblMailaAktuala);

		JLabel lblIragazi = new JLabel("Zure maila aukeratu:");
		lblIragazi.setBounds(30, 100, 150, 20);
		edukiontzia.add(lblIragazi);

		comboMaila = new JComboBox<>();
		comboMaila.setModel(new DefaultComboBoxModel<>(
				new String[] { "1. maila", "2. maila", "3. maila", "4. maila", "5. maila" }));
		comboMaila.setBounds(180, 100, 120, 22);
		edukiontzia.add(comboMaila);

		JLabel lblZerrenda = new JLabel("Workouts zerrenda:");
		lblZerrenda.setBounds(30, 140, 150, 20);
		edukiontzia.add(lblZerrenda);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 160, 250, 150);
		edukiontzia.add(scrollPane);

		listaWorkout = new JList<>();
		listaWorkout.setModel(new AbstractListModel<String>() {
			private static final long serialVersionUID = 1L;
			String[] balioak = new String[] { "Workout 1 - 1. maila - 6 ariketa", "Workout 2 - 2. maila - 6 ariketa",
					"Workout 3 - 3. maila - 6 ariketa", "Workout 4 - 4. maila - 6 ariketa",
					"Workout 5 - 5. maila - 6 ariketa" };

			public int getSize() {
				return balioak.length;
			}

			public String getElementAt(int index) {
				return balioak[index];
			}
		});
		scrollPane.setViewportView(listaWorkout);

		btnIkusiHistoria = new JButton("Ikusi historia");
		btnIkusiHistoria.setBounds(350, 160, 180, 30);
		edukiontzia.add(btnIkusiHistoria);

		btnHasiWorkout = new JButton("Hasi Workout-a");
		btnHasiWorkout.addActionListener(e -> workoutThread.start());
		btnHasiWorkout.setBounds(350, 210, 180, 30);
		edukiontzia.add(btnHasiWorkout);

		btnIkusiAriketak = new JButton("Ariketak Ikusi");
		btnIkusiAriketak.addActionListener(e -> {
			int aukeratuMaila = comboMaila.getSelectedIndex() + 1;

			listaWorkout.setModel(new AbstractListModel<String>() {
				private static final long serialVersionUID = 1L;
				String[] balioak = new String[] { "Cargando..." };

				public int getSize() {
					return balioak.length;
				}

				public String getElementAt(int index) {
					return balioak[index];
				}
			});

			routines.ariketak(aukeratuMaila);
		});
		btnIkusiAriketak.setBounds(350, 267, 180, 30);
		edukiontzia.add(btnIkusiAriketak);

		JButton btnLogout = new JButton("Logout");
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				login.setVisible(true);
			}
		});
		btnLogout.setBounds(485, 18, 89, 23);
		edukiontzia.add(btnLogout);
	}
}