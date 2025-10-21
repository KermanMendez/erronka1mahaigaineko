package view;

import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import controller.AppState;
import model.Exercise;
import model.Hariak;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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

	public ThreadFrame(int level, String routineName) {
		setTitle("Workout Thread - " + routineName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 370);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 30, 380, 200);
		contentPane.add(scrollPane);

		DefaultListModel<String> listModel = new DefaultListModel<>();
		hariakList = new JList<>(listModel);
		scrollPane.setViewportView(hariakList);

		JButton btnStopTime = new JButton("Denbora Gelditu");
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
		btnStopTime.setBounds(30, 263, 115, 23);
		contentPane.add(btnStopTime);

		JButton btnNextExercise = new JButton("Hurrengo Ariketa");
		btnNextExercise.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				skipRestRequested = true;
			}
		});
		btnNextExercise.setBounds(155, 263, 126, 23);
		contentPane.add(btnNextExercise);

		JButton btnFinish = new JButton("Amaitu");
		btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hariakInstance.stopWorkout();
				Workouts workoutsFrame = new Workouts();
				workoutsFrame.setVisible(true);
				dispose();
			}
		});
		btnFinish.setBounds(291, 263, 119, 23);
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
	}

	public Thread getHilo1() {
		return hilo1;
	}

	public void setHilo1(Thread hilo1) {
		this.hilo1 = hilo1;
	}
}