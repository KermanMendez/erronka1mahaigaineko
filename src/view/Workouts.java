package view;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Workouts extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel edukiontzia;
	private JComboBox<String> comboMaila;
	private JList<String> listaWorkout;
	private JButton btnIkusiHistoria;
	private JButton btnHasiWorkout;
	private JLabel lblMailaAktuala;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Workouts frame = new Workouts();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Workouts() {
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

		lblMailaAktuala = new JLabel("Maila: 0");
		lblMailaAktuala.setBounds(30, 60, 150, 20);
		edukiontzia.add(lblMailaAktuala);

		JLabel lblIragazi = new JLabel("Zure maila aukeratu:");
		lblIragazi.setBounds(30, 100, 150, 20);
		edukiontzia.add(lblIragazi);

		comboMaila = new JComboBox<>();
		comboMaila.setModel(
				new DefaultComboBoxModel<String>(new String[] { "0. maila", "1. maila", "2. maila", "3. maila" }));
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
			String[] balioak = new String[] { "Workout 1 - 0. maila - 5 ariketa", "Workout 2 - 1. maila - 6 ariketa",
					"Workout 3 - 2. maila - 8 ariketa" };

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
		btnHasiWorkout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnHasiWorkout.setBounds(350, 210, 180, 30);
		edukiontzia.add(btnHasiWorkout);
	}
}