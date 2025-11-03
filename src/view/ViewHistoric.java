package view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.Routines;
import model.Theme;
import model.UIStyle;

public class ViewHistoric extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel edukiontzia;
	private JComboBox<String> comboMaila;
	public static JList<String> listaWorkout;
	private LoginFrame login = new LoginFrame(Boolean.TRUE);

	public ViewHistoric(Boolean connect) {

		Theme.apply();
		setIconImage(Toolkit.getDefaultToolkit().getImage(Workouts.class.getResource("/img/logo.png")));
		Routines routines = new Routines(connect);
		setTitle("Historic Workouts");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700, 480);
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
			Workouts workouts = new Workouts(connect);
			workouts.setVisible(true);
			dispose();
		});
		header.add(btnAtzera, java.awt.BorderLayout.WEST);

		JLabel lblTitulua = new JLabel("Historic");
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

		JLabel lblIragazi = new JLabel("Zure maila aukeratu:");
		UIStyle.styleLabel(lblIragazi, false);
		filters.add(lblIragazi);

		comboMaila = new JComboBox<>();
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

		getContentPane().setBackground(UIStyle.BACKGROUND);

		comboMaila.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Routines.updateRoutinesComboBox(comboMaila, comboMailaRutinakLevel, routines, connect, listaWorkout,
						true);
			}
		});

		comboMailaRutinakLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Routines.updateWorkoutList(comboMaila, comboMailaRutinakLevel, connect, listaWorkout, true);
			}
		});

		Routines.updateWorkoutList(comboMaila, comboMailaRutinakLevel, connect, listaWorkout, true);
	}
}