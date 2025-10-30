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
	private int elapsedSeconds = 0;
	private int expectedTotalSeconds = 0;
	private Firestore db;
	private CreateUserBackup createUserBackup = new CreateUserBackup();

	private volatile boolean skipNow = false;

	private int level;

	public Hariak() {
	}

	public List<Exercise> start(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {
		return getExercises(level, routineName, connect);
	}

	private List<Exercise> getExercises(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {

		this.level = level;

		List<Exercise> exercises = new ArrayList<>();

		if (connect == null || !connect) {
			ReadBackup reader = new ReadBackup();
			ReadBackup.BackupData backup = reader.loadBackupData();
			if (backup == null)
				return exercises;

			List<ReadBackup.DocumentData> workoutDocs = backup.collections.get("workouts");
			if (workoutDocs == null)
				return exercises;

			for (ReadBackup.DocumentData d : workoutDocs) {
				String levelValue = d.fields.get("level");
				String nameValue = d.fields.get("name");
				if (levelValue != null && nameValue != null && levelValue.equals(String.valueOf(level))
						&& nameValue.equals(routineName)) {
					List<ReadBackup.DocumentData> exerciseDocs = d.subcollections.get("exercises");
					if (exerciseDocs == null)
						exerciseDocs = d.subcollections.get("exercise");
					if (exerciseDocs != null) {
						for (ReadBackup.DocumentData exDoc : exerciseDocs) {
							Exercise ex = new Exercise();
							ex.setName(exDoc.fields.get("name"));
							ex.setDescription(exDoc.fields.get("description"));
							ex.setReps(exDoc.fields.get("reps"));
							ex.setSets(exDoc.fields.get("sets"));
							ex.setSerieTime(exDoc.fields.get("timeSets"));
							ex.setRestTimeSec(exDoc.fields.get("timePauseSec"));
							exercises.add(ex);
						}
					}
					break;
				}
			}
			return exercises;
		}

		Controller controller = new Controller(connect);
		db = controller.getDb();
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

		for (int exIdx = 0; exIdx < exercises.size(); exIdx++) {
			Exercise ex = exercises.get(exIdx);
			int sets = ex.getSets();
			int serieTime = ex.getSerieTime();
			int restTime = ex.getRestTimeSec();

			for (int s = 1; s <= sets; s++) {

				for (int t = 1; t <= serieTime; t++) {
					if (stopSupplier != null && stopSupplier.get()) {
						if (mode == 0)
							totalSeconds = elapsedSeconds;
						return;
					}
					if (canPause)
						waitIfPaused(pauseSupplier, pauseLock);

					if (mode == 0) {
						elapsedSeconds++;
						totalSeconds = elapsedSeconds;
						int remaining = expectedTotalSeconds - elapsedSeconds + 1;
						if (remaining < 0)
							remaining = 0;
						totalTime = remaining;
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

				if (mode == 0) {
					completedSets++;
				}

				if (s < sets) {
					skipNow = false;
					int elapsed = 0;
					while (elapsed < restTime && !skipNow) {
						if (stopSupplier != null && stopSupplier.get()) {
							if (mode == 0)
								totalSeconds = elapsedSeconds;
							return;
						}

						if (skipRest != null && skipRest.get()) {
							skipNow = true;
							break;
						}

						if (canPause)
							waitIfPaused(pauseSupplier, pauseLock);

						if (mode == 0) {
							elapsedSeconds++;
							totalSeconds = elapsedSeconds;
							int remaining = expectedTotalSeconds - elapsedSeconds + 1;
							if (remaining < 0)
								remaining = 0;
							totalTime = remaining;
						}

						final int currentSec = ++elapsed;
						final int remainingRest = restTime - currentSec + 1;
						SwingUtilities.invokeLater(() -> {
							if (label != null) {
								if (mode == 0)
									label.setText("Denbora totala: " + totalTime + " seg");
								else if (mode == 2)
									label.setText("Atsedena " + remainingRest + "/" + restTime + " seg");
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
							totalSeconds = elapsedSeconds;
						return;
					}

					if (skipRest != null && skipRest.get()) {
						skipNow = true;
						break;
					}

					if (canPause)
						waitIfPaused(pauseSupplier, pauseLock);

					if (mode == 0) {
						elapsedSeconds++;
						totalSeconds = elapsedSeconds;
						int remaining = expectedTotalSeconds - elapsedSeconds + 1;
						if (remaining < 0)
							remaining = 0;
						totalTime = remaining;
					}

					final int currentSec = ++elapsed;
					SwingUtilities.invokeLater(() -> {
						if (label != null) {
							if (mode == 0)
								label.setText("Denbora totala: " + totalTime + " seg");
							else if (mode == 2) {
								int remainingRest = interExerciseRest - currentSec + 1;
								label.setText("Atsedena " + remainingRest + "/" + interExerciseRest + " seg");
							}
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

				int computedTotalSets = 0;
				this.completedSets = 0;
				if (exercises != null) {
					for (Exercise e : exercises) {
						computedTotalSets += e.getSets();
					}
				}
				this.expectedTotalSets = computedTotalSets;

				int computedTotalSeconds = 0;
				if (exercises != null) {
					for (int i = 0; i < exercises.size(); i++) {
						Exercise e = exercises.get(i);
						int sets = e.getSets();
						int serieTime = e.getSerieTime();
						int restTime = e.getRestTimeSec();
						computedTotalSeconds += sets * serieTime;
						if (sets > 1)
							computedTotalSeconds += restTime * (sets - 1);
						if (i < exercises.size() - 1)
							computedTotalSeconds += restTime;
					}
				}
				this.expectedTotalSeconds = computedTotalSeconds;
				this.elapsedSeconds = 0;

				Thread tTotal = new Thread(() -> runExerciseThread(exercises, labelTotal, "⏱ TOTAL", stopSupplier,
						skipSupplier, pauseSupplier, lock, 0, thread1));
				Thread tSeries = new Thread(() -> runExerciseThread(exercises, labelSeries, "💪 SERIEAK", stopSupplier,
						skipSupplier, pauseSupplier, lock, 1, thread2));
				Thread tRest = new Thread(() -> runExerciseThread(exercises, labelDescansos, "😴 ATSEDENAK",
						stopSupplier, skipSupplier, pauseSupplier, lock, 2, thread3));

				tTotal.start();
				tSeries.start();
				tRest.start();

				tTotal.join();
				tSeries.join();
				tRest.join();

				if (stopSupplier == null || !stopSupplier.get()) {
					amaituta = true;
					totalSeconds = elapsedSeconds;
				}

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

	public void sumLevel() {

		String emaila = createUserBackup.loadEmail();

		try {
			QuerySnapshot querySnapshot = db.collection("users").whereEqualTo("email", emaila).get().get();

			DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);

			if (amaituta == true) {
				if (level < 5) {
					level++;
					Map<String, Object> data = new HashMap<>();
					data.put("level", level);
					db.collection("users").document(userDoc.getId()).update(data);
				}
			}

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void historyLog(String routineName) {
		String email = createUserBackup.loadEmail();

		// Try online first; if db is not available or write fails, save offline
		try {
			if (db != null) {
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
				data.put("totalSets", completedSets);
				data.put("totalTime", totalSeconds);
				data.put("workoutId", routineDoc.getId());
				data.put("level", level);

				history.add(data);

				sumLevel();
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			OfflineHistoric offline = new OfflineHistoric();
			String uid = null;
			ReadBackup reader = new ReadBackup();
			ReadBackup.BackupData backup = reader.loadBackupData();
			if (backup != null && backup.users != null) {
				for (ReadBackup.UserData u : backup.users) {
					if (u.email != null && u.email.equals(email)) {
						uid = u.uid;
						break;
					}
				}
			}

			String workoutId = null;
			if (backup != null && backup.collections != null) {
				List<ReadBackup.DocumentData> workouts = backup.collections.get("workouts");
				if (workouts != null) {
					for (ReadBackup.DocumentData wd : workouts) {
						String nameVal = wd.fields.get("name");
						String levelVal = wd.fields.get("level");
						if (nameVal != null && nameVal.equals(routineName) && levelVal != null
								&& levelVal.equals(String.valueOf(level))) {
							workoutId = wd.id;
							break;
						}
					}
				}
			}

			String today = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			Map<String, String> fields = new HashMap<>();
			fields.put("completed", String.valueOf(amaituta));
			fields.put("date", today);
			fields.put("totalSets", String.valueOf(completedSets));
			fields.put("totalTime", String.valueOf(totalSeconds));
			if (workoutId != null)
				fields.put("workoutId", workoutId);
			else
				fields.put("workoutName", routineName);
			fields.put("level", String.valueOf(level));

			offline.addEntry(uid, email, fields);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int getSec() {
		return sec;
	}

	public void setSec(int sec) {
		this.sec = sec;
	}
}