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

	private boolean amaituta = false;
	private long totalSeconds;
	private int completedSets = 0;
	private int expectedTotalSets = 0;
	private int sec;
	private int totalTime = 0;
	private Firestore db;

	private volatile boolean skipNow = false;

	public Hariak() {
	}

	public List<Exercise> start(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {
		return getExercises(level, routineName, connect);
	}

	private List<Exercise> getExercises(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {

		Controller controller = new Controller(connect);
		db = controller.getDb();
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

				// After finishing a full serie (all seconds), count it as a completed set in
				// the mode 0 thread
				if (mode == 0) {
					completedSets++;
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
			Supplier<Boolean> pauseSupplier, Object lock, String routineName, boolean thread1, boolean thread2,
			boolean thread3) {
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

				// Pre-calculate expected total sets (sum of sets per exercise) and reset
				// completedSets
				int computedTotalSets = 0;
				this.completedSets = 0;
				if (exercises != null) {
					for (Exercise e : exercises) {
						computedTotalSets += e.getSets();
					}
				}
				// store expected total sets so we can show expected vs completed later
				this.expectedTotalSets = computedTotalSets;

				// Create named threads so we can join and detect normal completion
				Thread tTotal = new Thread(() -> runExerciseThread(exercises, labelTotal, "⏱ TOTAL", stopSupplier,
						skipSupplier, pauseSupplier, lock, 0, thread1));
				Thread tSeries = new Thread(() -> runExerciseThread(exercises, labelSeries, "💪 SERIEAK", stopSupplier,
						skipSupplier, pauseSupplier, lock, 1, thread2));
				Thread tRest = new Thread(() -> runExerciseThread(exercises, labelDescansos, "😴 ATSEDENAK",
						stopSupplier, skipSupplier, pauseSupplier, lock, 2, thread3));

				tTotal.start();
				tSeries.start();
				tRest.start();

				// Wait for all three threads to finish. If stopSupplier becomes true, the
				// runExerciseThread
				// implementations return early; in that case we won't mark amaituta as true.
				tTotal.join();
				tSeries.join();
				tRest.join();

				// If stop was not requested, then the routine completed normally
				if (stopSupplier == null || !stopSupplier.get()) {
					amaituta = true;
					// Ensure totalSeconds reflects the totalTime accumulated by mode 0 thread
					totalSeconds = totalTime;
					// Log history automatically when finished normally
				}

				// Show a popup with the total time and sets completed only if at least one set
				// was completed
				final long popupTime = totalSeconds;
				final int popupCompletedSets = this.completedSets;
				final int popupExpectedSets = this.expectedTotalSets;
				if (popupCompletedSets > 0) {
					SwingUtilities.invokeLater(
							() -> JOptionPane.showMessageDialog(null, "Rutina amaitu da! Denbora totala: " + popupTime
									+ " seg\nSeries egindakoak: " + popupCompletedSets + " / " + popupExpectedSets));
					historyLog(routineName);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}).start();

	}

	public void historyLog(String routineName) {
		CreateUserBackup backup = new CreateUserBackup();
		String email = backup.loadEmail();

		try {
			DocumentSnapshot routineDoc = db.collection("workouts").whereEqualTo("name", routineName).get().get()
					.getDocuments().get(0);

			QuerySnapshot userQuery = db.collection("users").whereEqualTo("email", email).get().get();
			if (userQuery.isEmpty())
				return;

			String userId = userQuery.getDocuments().get(0).getId();
			CollectionReference history = db.collection("users").document(userId).collection("historic");

			String today = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			Map<String, Object> data = new HashMap<>();
			data.put("completed", amaituta);
			data.put("date", today);
			// Store the actual number of completed sets (if stopped early this will be less
			// than expected)
			data.put("totalSets", completedSets);
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
}