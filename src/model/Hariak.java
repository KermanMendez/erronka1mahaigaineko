package model;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import controller.Controller;

public class Hariak {

	private final Controller controller = new Controller();
	private final Firestore db = controller.getDb();

	private boolean amaituta = false;
	private long totalSeconds;
	private int totalSets;
	private int sec;
	private int totalTime = 0;

	public Hariak() {
	}

	public List<Exercise> start(int level, String routineName) throws InterruptedException, ExecutionException {
		return getExercises(level, routineName);
	}

	private List<Exercise> getExercises(int level, String routineName) throws InterruptedException, ExecutionException {
		List<Exercise> exercises = new ArrayList<>();
		QuerySnapshot querySnapshot = db.collection("workouts").whereEqualTo("level", level)
				.whereEqualTo("name", routineName).get().get();
		if (querySnapshot.isEmpty())
			return exercises;

		DocumentSnapshot routineDoc = querySnapshot.getDocuments().get(0);
		List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("exercise").get().get()
				.getDocuments();
		for (QueryDocumentSnapshot doc : exerciseDocs) {
			Exercise ex = new Exercise();
			ex.setReps(doc.get("reps"));
			ex.setSets(doc.get("sets"));
			ex.setSerieTime(doc.get("timeSets"));
			ex.setRestTimeSec(doc.get("timePauseSec"));
			exercises.add(ex);
		}
		return exercises;
	}

	private volatile boolean skipRestNow = false;

	private void runExerciseThread(List<Exercise> exercises, DefaultListModel<String> listModel, String hiloTag,
			Supplier<Boolean> stopSupplier, Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier,
			Object pauseLock, int mode, boolean canPause) {

		int hiloTotalCounter = 0;

		for (Exercise ex : exercises) {
			int sets = ex.getSets();
			int serieTime = ex.getSerieTime();
			int restTime = ex.getRestTimeSec();

			for (int s = 1; s <= sets; s++) {

				for (int t = 1; t <= serieTime; t++) {
					if (stopSupplier != null && stopSupplier.get())
						return;
					if (canPause)
						waitIfPaused(pauseSupplier, pauseLock);

					if (mode == 0) {
						hiloTotalCounter++;
						totalTime = hiloTotalCounter;
					}

					final int currentSec = t;
					final int currentSet = s;
					SwingUtilities.invokeLater(() -> {
						if (mode == 0)
							listModel.addElement("Denbora totala: " + totalTime + " seg");
						else if (mode == 1)
							listModel.addElement("Sets " + currentSet + " - " + currentSec + "/" + serieTime + " seg");
					});

					sleep(1000);
				}

				if (s < sets) {
					skipRestNow = false;
					for (int t = 1; t <= restTime; t++) {
						if (stopSupplier != null && stopSupplier.get())
							return;

						if (skipRest != null && skipRest.get()) {
							skipRestNow = true;
						}

						if (skipRestNow)
							break;

						if (canPause)
							waitIfPaused(pauseSupplier, pauseLock);

						if (mode == 0) {
							hiloTotalCounter++;
							totalTime = hiloTotalCounter;
						}

						final int currentSec = t;
						SwingUtilities.invokeLater(() -> {
							if (mode == 0)
								listModel.addElement("Denbora totala: " + totalTime + " seg");
							else if (mode == 2)
								listModel.addElement("Atsedena " + currentSec + "/" + restTime + " seg");
						});

						sleep(1000);
					}
				}
			}
		}

		if (mode == 0) {
			totalSeconds = totalTime;
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
					"Rutina amaituta! Denbora totala: " + totalTime + " seg"));
			amaituta = true;
		}
	}

	private void waitIfPaused(Supplier<Boolean> pauseSupplier, Object pauseLock) {
		if (pauseSupplier != null && pauseSupplier.get()) {
			synchronized (pauseLock) {
				try {
					pauseLock.wait();
				} catch (InterruptedException ignored) {
				}
			}
		}
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}

	public void startExerciseThreads(List<Exercise> exercises, DefaultListModel<String> modelTotal,
			DefaultListModel<String> modelSeries, DefaultListModel<String> modelDescansos,
			Supplier<Boolean> stopSupplier, Supplier<Boolean> skipSupplier, Supplier<Boolean> pauseSupplier,
			Object lock, boolean thread1, boolean thread2, boolean thread3) {

		new Thread(() -> runExerciseThread(exercises, modelTotal, "⏱ TOTAL", stopSupplier, skipSupplier, pauseSupplier,
				lock, 0, thread1)).start();
		new Thread(() -> runExerciseThread(exercises, modelSeries, "💪 SERIEAK", stopSupplier, skipSupplier,
				pauseSupplier, lock, 1, thread2)).start();
		new Thread(() -> runExerciseThread(exercises, modelDescansos, "😴 ATSEDENAK", stopSupplier, skipSupplier,
				pauseSupplier, lock, 2, thread3)).start();
	}

	public void historyLog(String routineName) {
		CreateUserBackup backup = new CreateUserBackup();
		String email = backup.loadEmail();
		try {
			DocumentSnapshot routineDoc = db.collection("workouts").whereEqualTo("name", routineName).get().get()
					.getDocuments().get(0);
			List<QueryDocumentSnapshot> exercises = routineDoc.getReference().collection("exercise").get().get()
					.getDocuments();
			for (QueryDocumentSnapshot doc : exercises)
				totalSets += new Exercise().setSets(doc.get("sets"));

			QuerySnapshot userQuery = db.collection("users").whereEqualTo("email", email).get().get();
			if (userQuery.isEmpty())
				return;

			String userId = userQuery.getDocuments().get(0).getId();
			CollectionReference history = db.collection("users").document(userId).collection("historic");

			String today = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			Map<String, Object> data = new HashMap<>();
			data.put("completed", amaituta);
			data.put("date", today);
			data.put("totalReps", totalSets);
			data.put("totalTime", totalSeconds);
			data.put("workoutId", routineDoc.getId());

			history.add(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getSec() {
		return sec;
	}

	public void setSec(int sec) {
		this.sec = sec;
	}

	public void amaituta() {
		Thread.currentThread().interrupt();
	}
}
