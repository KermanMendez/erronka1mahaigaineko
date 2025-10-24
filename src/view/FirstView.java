package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.AppState;
import controller.MainApp;
import model.UIStyle;

public class FirstView extends JFrame {

	private static final long serialVersionUID = 1L;
	private static ImageIcon logo;
	private JPanel contentPane;

	public FirstView(Boolean connect) {
		if (!AppState.isAppStarted()) {
			controller.MainApp.main(null);
		}
		setTitle("LONG RING LONG LAND GYM");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/logo.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 534, 343);
		contentPane = new JPanel();
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		JButton btnEnter = new JButton("Sartu");
		UIStyle.styleButton(btnEnter);
		btnEnter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnEnter.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnEnter.setBackground(UIStyle.ACCENT);
				btnEnter.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnEnter.setBackground(UIStyle.BUTTON_BG);
				btnEnter.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnEnter.addActionListener(e -> {
			LoginFrame loginFrame = new LoginFrame(connect);
			loginFrame.setVisible(true);
			dispose();
		});
		btnEnter.setBounds(207, 255, 120, 35);
		contentPane.add(btnEnter);
		JLabel labelLogo = new JLabel("");
		labelLogo.setIcon(getLogo());
		labelLogo.setHorizontalAlignment(SwingConstants.CENTER);
		labelLogo.setBounds(68, 11, 369, 218);
		UIStyle.styleLabel(labelLogo, true);
		contentPane.add(labelLogo);
		getContentPane().setBackground(UIStyle.BACKGROUND);
	}

	private ImageIcon getLogo() {
		if (logo == null) {
			ImageIcon jatorrekoIkono = new ImageIcon(getClass().getResource("/img/logo.png"));
			Image irudiaEskalatua = jatorrekoIkono.getImage().getScaledInstance(300, 220, Image.SCALE_SMOOTH);
			logo = new ImageIcon(irudiaEskalatua);
		}
		return logo;
	}

	public static void main(String[] args) {
		if (!AppState.isAppStarted()) {
			MainApp.main(args);
			return;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FirstView frame = new FirstView(Boolean.TRUE);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}