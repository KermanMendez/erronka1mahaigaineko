package view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import model.Routines;
import model.Theme;
import model.UIStyle;

public class Workouts extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel edukiontzia;
	private JComboBox<String> comboMaila;
	public static JList<String> listaWorkout;
	private JButton btnIkusiHistoria;
	private JButton btnHasiWorkout;
	private JLabel lblMailaAktuala;
	private LoginFrame login = new LoginFrame(Boolean.TRUE);

	public Workouts(Boolean isTrainer, Boolean connect) {

		Theme.apply();
		setIconImage(Toolkit.getDefaultToolkit().getImage(Workouts.class.getResource("/img/logo.png")));
		Routines routines = new Routines(connect);
		setTitle("Workouts");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700, 500);
		setLocationRelativeTo(null);

		edukiontzia = new JPanel(new java.awt.BorderLayout(12, 12));
		UIStyle.stylePanel(edukiontzia);
		setContentPane(edukiontzia);

		JPanel topPanel = new JPanel(new java.awt.BorderLayout(0, 12));
		UIStyle.stylePanel(topPanel);
		edukiontzia.add(topPanel, java.awt.BorderLayout.NORTH);

		JPanel header = new JPanel(new java.awt.BorderLayout());
		UIStyle.stylePanel(header);
		JButton btnAtzera = new JButton(new ImageIcon(new ImageIcon(getClass().getResource("/img/atzera.png"))
				.getImage().getScaledInstance(36, 36, java.awt.Image.SCALE_SMOOTH)));
		UIStyle.styleIconButton(btnAtzera);
		btnAtzera.addActionListener(e -> {
			Inter inter = new Inter(isTrainer, connect);
			inter.setVisible(true);
			dispose();
		});
		header.add(btnAtzera, java.awt.BorderLayout.WEST);

		JLabel lblTitulua = new JLabel("Workouts");
		lblTitulua.setFont(UIStyle.TITLE_FONT);
		lblTitulua.setForeground(UIStyle.PRIMARY);
		lblTitulua.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		header.add(lblTitulua, java.awt.BorderLayout.CENTER);

		JButton btnLogout = new JButton("Logout");
		UIStyle.styleButton(btnLogout);
		btnLogout.setToolTipText("Saioa itxi");
		UIStyle.addHoverEffect(btnLogout);
		btnLogout.addActionListener(e -> {
			dispose();
			login.setVisible(true);
		});
		header.add(btnLogout, java.awt.BorderLayout.EAST);

		topPanel.add(header, java.awt.BorderLayout.NORTH);

		JPanel filters = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 8));
		UIStyle.stylePanel(filters);
		lblMailaAktuala = new JLabel("Maila: 1");
		UIStyle.styleLabel(lblMailaAktuala, false);
		filters.add(lblMailaAktuala);

		JLabel lblIragazi = new JLabel("Zure maila aukeratu:");
		UIStyle.styleLabel(lblIragazi, false);
		filters.add(lblIragazi);

		comboMaila = new javax.swing.JComboBox<>();
		comboMaila.setModel(new DefaultComboBoxModel<>(routines.levels()));
		UIStyle.styleField(comboMaila);
		comboMaila.setToolTipText("Zure maila aukeratu");
		filters.add(comboMaila);

		JComboBox<String> comboMailaRutinakLevel = new JComboBox<String>();
		try {
			comboMailaRutinakLevel.setModel(
					new DefaultComboBoxModel<>(routines.getRoutines(comboMaila.getSelectedIndex() + 1, connect)));
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		UIStyle.styleField(comboMailaRutinakLevel);
		comboMailaRutinakLevel.setToolTipText("Entrenamendu mota aukeratu");
		filters.add(comboMailaRutinakLevel);

		topPanel.add(filters, java.awt.BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		UIStyle.styleScrollPane(scrollPane);
		listaWorkout = new JList<>();
		UIStyle.styleField(listaWorkout);
		listaWorkout.setCellRenderer(new CardListRenderer());
		listaWorkout.setFixedCellHeight(-1);
		scrollPane.setViewportView(listaWorkout);
		edukiontzia.add(scrollPane, java.awt.BorderLayout.CENTER);

		JPanel bottom = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 40, 12));
		UIStyle.stylePanel(bottom);
		btnIkusiHistoria = new JButton("Ikusi historia");
		btnIkusiHistoria.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViewHistoric viewHistoric = new ViewHistoric(isTrainer, connect);
				viewHistoric.setVisible(true);
				dispose();
			}
		});
		UIStyle.styleButton(btnIkusiHistoria);
		btnIkusiHistoria.setToolTipText("Entrenamenduen historiala ikusi");
		UIStyle.addHoverEffect(btnIkusiHistoria);
		bottom.add(btnIkusiHistoria);

		btnHasiWorkout = new JButton("Hasi Workout-a");
		UIStyle.styleButton(btnHasiWorkout);
		btnHasiWorkout.setToolTipText("Entrenamendua hasi");
		UIStyle.addHoverEffect(btnHasiWorkout);
		btnHasiWorkout.addActionListener(e -> {
			ThreadFrame threadFrame = new ThreadFrame(comboMaila.getSelectedIndex() + 1,
					comboMailaRutinakLevel.getSelectedItem().toString(), isTrainer, connect);
			threadFrame.setVisible(true);
			dispose();
		});
		bottom.add(btnHasiWorkout);

		edukiontzia.add(bottom, java.awt.BorderLayout.SOUTH);

		getContentPane().setBackground(UIStyle.BACKGROUND);

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
			int aukeratutakoMaila = comboMaila.getSelectedIndex() + 1;
			lblMailaAktuala.setText("Maila: " + aukeratutakoMaila);

			new Thread(() -> {
				try {
					String[] workouts = routines.getRoutines(aukeratutakoMaila, connect);
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
	}
}