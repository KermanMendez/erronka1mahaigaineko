package model;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javax.swing.JLabel;
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

	private volatile boolean skipNow = false;

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
		List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("exercises").get().get()
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

	private void runExerciseThread(List<Exercise> exercises, JLabel label, String hiloTag,
			Supplier<Boolean> stopSupplier, Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier,
			Object pauseLock, int mode, boolean canPause) {

		int hiloTotalCounter = 0;

		for (int exIdx = 0; exIdx < exercises.size(); exIdx++) {
			Exercise ex = exercises.get(exIdx);
			int sets = ex.getSets();
			int serieTime = ex.getSerieTime();
			int restTime = ex.getRestTimeSec();

			for (int s = 1; s <= sets; s++) {

				for (int t = 1; t <= serieTime; t++) {
					if (stopSupplier != null && stopSupplier.get()) {
						if (mode == 0)
							totalSeconds = hiloTotalCounter;
						return;
					}
					if (canPause)
						waitIfPaused(pauseSupplier, pauseLock);

					if (mode == 0) {
						hiloTotalCounter++;
						totalTime = hiloTotalCounter;
					}

					final int currentSec = t;
					final int currentSet = s;
					SwingUtilities.invokeLater(() -> {
						if (label != null) {
							if (mode == 0)
								label.setText("Denbora totala: " + totalTime + " seg");
							else if (mode == 1)
								label.setText("Sets " + currentSet + " - " + currentSec + "/" + serieTime + " seg");
						}
					});

					sleep(1000);
				}

				if (s < sets) {
					skipNow = false;
					int elapsed = 0;
					while (elapsed < restTime && !skipNow) {
						if (stopSupplier != null && stopSupplier.get()) {
							if (mode == 0)
								totalSeconds = hiloTotalCounter;
							return;
						}

						if (skipRest != null && skipRest.get()) {
							skipNow = true;
							break;
						}

						if (canPause)
							waitIfPaused(pauseSupplier, pauseLock);

						if (mode == 0) {
							hiloTotalCounter++;
							totalSeconds = hiloTotalCounter;
							totalTime = hiloTotalCounter;
						}

						final int currentSec = ++elapsed;
						SwingUtilities.invokeLater(() -> {
							if (label != null) {
								if (mode == 0)
									label.setText("Denbora totala: " + totalTime + " seg");
								else if (mode == 2)
									label.setText("Atsedena " + currentSec + "/" + restTime + " seg");
							}
						});

						for (int i = 0; i < 5; i++) {
							if (skipRest != null && skipRest.get()) {
								skipNow = true;
								break;
							}
							sleep(200);
						}
					}
				}

			}

			if (exIdx < exercises.size() - 1) {
				int interExerciseRest = restTime;
				skipNow = false;
				int elapsed = 0;
				while (elapsed < interExerciseRest && !skipNow) {
					if (stopSupplier != null && stopSupplier.get()) {
						if (mode == 0)
							totalSeconds = hiloTotalCounter;
						return;
					}

					if (skipRest != null && skipRest.get()) {
						skipNow = true;
						break;
					}

					if (canPause)
						waitIfPaused(pauseSupplier, pauseLock);

					if (mode == 0) {
						hiloTotalCounter++;
						totalSeconds = hiloTotalCounter;
						totalTime = hiloTotalCounter;
					}

					final int currentSec = ++elapsed;
					SwingUtilities.invokeLater(() -> {
						if (label != null) {
							if (mode == 0)
								label.setText("Denbora totala: " + totalTime + " seg");
							else if (mode == 2)
								label.setText("Atsedena " + currentSec + "/" + interExerciseRest + " seg");
						}
					});

					for (int i = 0; i < 5; i++) {
						if (skipRest != null && skipRest.get()) {
							skipNow = true;
							break;
						}
						sleep(200);
					}
				}
			}
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

	public void startExerciseThreads(List<Exercise> exercises, JLabel labelTotal, JLabel labelSeries,
			JLabel labelDescansos, JLabel labelHasiera, Supplier<Boolean> stopSupplier, Supplier<Boolean> skipSupplier,
			Supplier<Boolean> pauseSupplier, Object lock, boolean thread1, boolean thread2, boolean thread3) {
		new Thread(() -> {
			try {
				for (int i = 5; i > 0; i--) {
					int countdown = i;
					SwingUtilities.invokeLater(() -> {
						if (labelHasiera != null)
							labelHasiera.setText("Prest! Hasiera " + countdown + "...");
					});
					Thread.sleep(1000);
				}

				SwingUtilities.invokeLater(() -> {
					if (labelHasiera != null)
						labelHasiera.setText("Hasi da entrenamendua!");
				});

				Thread.sleep(1000);

				if (exercises == null || exercises.isEmpty()) {
					SwingUtilities.invokeLater(() -> {
						if (labelHasiera != null)
							labelHasiera.setText("Ez da entrenamendurik aurkitu!");
					});
					return;
				}

				labelTotal.setVisible(true);
				labelSeries.setVisible(true);
				labelDescansos.setVisible(true);
				labelHasiera.setVisible(false);

				new Thread(() -> runExerciseThread(exercises, labelTotal, "⏱ TOTAL", stopSupplier, skipSupplier,
						pauseSupplier, lock, 0, thread1)).start();

				new Thread(() -> runExerciseThread(exercises, labelSeries, "💪 SERIEAK", stopSupplier, skipSupplier,
						pauseSupplier, lock, 1, thread2)).start();

				new Thread(() -> runExerciseThread(exercises, labelDescansos, "😴 ATSEDENAK", stopSupplier,
						skipSupplier, pauseSupplier, lock, 2, thread3)).start();

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}).start();
	}

	public void historyLog(String routineName) {
		CreateUserBackup backup = new CreateUserBackup();
		String email = backup.loadEmail();

		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
				"Rutina amaitu da! Denbora totala: " + totalSeconds + " seg"));

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
