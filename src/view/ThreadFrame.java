package view;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import controller.AppState;
import model.Exercise;
import model.Hariak;
import model.UIStyle;

public class ThreadFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<String> hariakList;
	private Hariak hariakInstance;
	private Thread hilo1;
	private volatile boolean stopRequested = false;
	private volatile boolean skipRestRequested = false;
	private volatile boolean paused = false;
	private final Object hilo1Lock = new Object();

	public ThreadFrame(int level, String routineName) {
		setTitle("Workout Thread - " + routineName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 370);

		contentPane = new JPanel();
		UIStyle.stylePanel(contentPane);
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 30, 380, 200);
		contentPane.add(scrollPane);

		DefaultListModel<String> listModel = new DefaultListModel<>();
		hariakList = new JList<>(listModel);
		UIStyle.styleField(hariakList);
		scrollPane.setViewportView(hariakList);

		JButton btnStopTime = new JButton("Denbora Gelditu");
		UIStyle.styleButton(btnStopTime);
		btnStopTime.setToolTipText("Denbora gelditu/jarraitu");
		btnStopTime.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnStopTime.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnStopTime.setBackground(UIStyle.ACCENT);
				btnStopTime.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnStopTime.setBackground(UIStyle.BUTTON_BG);
				btnStopTime.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnStopTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!paused) {
					paused = true;
					btnStopTime.setText("Denbora Jarraitu");
				} else {
					paused = false;
					btnStopTime.setText("Denbora Gelditu");
					synchronized (hilo1Lock) {
						hilo1Lock.notifyAll();
					}
				}
			}
		});
		btnStopTime.setBounds(30, 263, 115, 30);
		contentPane.add(btnStopTime);

		JButton btnNextExercise = new JButton("Hurrengo Ariketa");
		UIStyle.styleButton(btnNextExercise);
		btnNextExercise.setToolTipText("Hurrengo ariketarekin jarraitu");
		btnNextExercise.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnNextExercise.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnNextExercise.setBackground(UIStyle.ACCENT);
				btnNextExercise.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnNextExercise.setBackground(UIStyle.BUTTON_BG);
				btnNextExercise.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnNextExercise.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				skipRestRequested = true;
			}
		});
		btnNextExercise.setBounds(155, 263, 126, 30);
		contentPane.add(btnNextExercise);

		JButton btnFinish = new JButton("Amaitu");
		UIStyle.styleButton(btnFinish);
		btnFinish.setToolTipText("Entrenamendua amaitu");
		btnFinish.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFinish.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnFinish.setBackground(UIStyle.ACCENT);
				btnFinish.setForeground(UIStyle.SECONDARY);
			}

			public void mouseExited(MouseEvent e) {
				btnFinish.setBackground(UIStyle.BUTTON_BG);
				btnFinish.setForeground(UIStyle.BUTTON_FG);
			}
		});
		btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hariakInstance.stopWorkout();
				Workouts workoutsFrame = new Workouts(Boolean.FALSE);
				workoutsFrame.setVisible(true);
				dispose();
			}
		});
		btnFinish.setBounds(291, 263, 119, 30);
		contentPane.add(btnFinish);

		hariakInstance = new Hariak("WorkoutThread");

		new Thread(() -> {
			try {
				List<Exercise> exercises = hariakInstance.start(level, routineName);
				setHilo1(hariakInstance.calculosWithStop(exercises, listModel, () -> stopRequested, () -> {
					if (skipRestRequested) {
						skipRestRequested = false;
						return true;
					}
					return false;
				}, () -> paused, hilo1Lock));
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				listModel.addElement("Error loading exercises");
			}
		}).start();
		getContentPane().setBackground(UIStyle.BACKGROUND);
	}

	public static void main(String[] args) {
		if (!AppState.isAppStarted()) {
			System.exit(0);
		}
		EventQueue.invokeLater(() -> {
			try {
				ThreadFrame frame = new ThreadFrame(1, "My Routine");
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public Thread getHilo1() {
		return hilo1;
	}

	public void setHilo1(Thread hilo1) {
		this.hilo1 = hilo1;
	}
}