package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
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
import model.ReadHistoric;
import model.Routines;
import model.UIStyle;

public class ViewHistoric extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel edukiontzia;
	private JComboBox<String> comboMaila;
	public static JList<String> listaWorkout;
	private LoginFrame login = new LoginFrame(Boolean.TRUE);

	public ViewHistoric(Boolean isTrainer, Boolean connect) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(Workouts.class.getResource("/img/logo.png")));
		Routines routines = new Routines(connect);
		ReadHistoric readHistoric = new ReadHistoric(connect);
		setTitle("Historic Workouts");
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
				Workouts workouts = new Workouts(isTrainer, connect);
				workouts.setVisible(true);
				dispose();
			}
		});
		btnAtzera.setBounds(10, 10, 40, 40);
		btnAtzera.setBorderPainted(false);
		btnAtzera.setContentAreaFilled(false);
		btnAtzera.setFocusPainted(false);
		btnAtzera.setOpaque(false);
		edukiontzia.add(btnAtzera);

		JLabel lblTitulua = new JLabel("Historic");
		lblTitulua.setFont(UIStyle.TITLE_FONT);
		lblTitulua.setForeground(UIStyle.PRIMARY);
		lblTitulua.setBounds(240, 10, 111, 30);
		edukiontzia.add(lblTitulua);

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

		comboMaila.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int aukeratutakoMaila = comboMaila.getSelectedIndex() + 1;
				new Thread(() -> {
					try {
						String[] ariketak = readHistoric.getHistoric(aukeratutakoMaila, connect);
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
			}
		});
		int aukeratutakoMaila = comboMaila.getSelectedIndex() + 1;
		new Thread(() -> {
			try {
				String[] ariketak = readHistoric.getHistoric(aukeratutakoMaila, connect);
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