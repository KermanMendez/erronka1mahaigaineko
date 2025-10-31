package model;

import java.time.format.DateTimeFormatter;
import java.util.*;
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
	private int totalTime = 0;
	private int elapsedSeconds = 0;
	private int expectedTotalSeconds = 0;
	private Firestore db;
	private CreateUserBackup createUserBackup = new CreateUserBackup();
	private volatile boolean skipNow = false;
	private int level;

	public List<Exercise> start(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {
		return getExercises(level, routineName, connect);
	}

	public RoutineData loadRoutine(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {

		this.level = level;

		if (connect == null || !connect) {
			ReadBackup reader = new ReadBackup();
			ReadBackup.BackupData backup = reader.loadBackupData();
			if (backup == null)
				return new RoutineData(Collections.emptyList(), "", 0);

			ExercisesResult res = loadExercisesFromBackup(level, routineName, backup);
			String description = getDefaultRoutineDescription(res.routineDescription, res.exercises);
			return new RoutineData(res.exercises, description, res.totalSets);
		}

		ExercisesResult res = loadExercisesFromFirestore(level, routineName);
		String description = getDefaultRoutineDescription(res.routineDescription, res.exercises);
		return new RoutineData(res.exercises, description, res.totalSets);
	}

	private List<Exercise> getExercises(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {

		this.level = level;

		if (connect == null || !connect) {
			ReadBackup reader = new ReadBackup();
			ReadBackup.BackupData backup = reader.loadBackupData();
			if (backup == null)
				return new ArrayList<>();
			ExercisesResult res = loadExercisesFromBackup(level, routineName, backup);
			return res.exercises;
		}

		ExercisesResult res = loadExercisesFromFirestore(level, routineName);
		return res.exercises;
	}

	private static class ExercisesResult {
		final List<Exercise> exercises;
		final int totalSets;
		final String routineDescription;

		ExercisesResult(List<Exercise> exercises, int totalSets, String routineDescription) {
			this.exercises = exercises;
			this.totalSets = totalSets;
			this.routineDescription = routineDescription;
		}
	}

	private ExercisesResult loadExercisesFromBackup(int level, String routineName, ReadBackup.BackupData backup) {
		List<Exercise> exercises = new ArrayList<>();
		int totalSets = 0;
		String routineDescription = null;

		if (backup == null || backup.collections == null)
			return new ExercisesResult(exercises, 0, null);

		List<ReadBackup.DocumentData> workoutDocs = backup.collections.get("workouts");
		if (workoutDocs == null)
			return new ExercisesResult(exercises, 0, null);

		for (ReadBackup.DocumentData d : workoutDocs) {
			String levelValue = d.fields.get("level");
			String nameValue = d.fields.get("name");
			if (levelValue != null && nameValue != null && levelValue.equals(String.valueOf(level))
					&& nameValue.equals(routineName)) {

				routineDescription = d.fields.get("description");

				List<ReadBackup.DocumentData> exerciseDocs = d.subcollections.get("exercises");
				if (exerciseDocs == null)
					exerciseDocs = d.subcollections.get("exercise");

				if (exerciseDocs != null) {
					for (ReadBackup.DocumentData exDoc : exerciseDocs) {
						Exercise ex = exerciseFromBackupDoc(exDoc);
						exercises.add(ex);
						totalSets += ex.getSets();
					}
				}
				break;
			}
		}

		return new ExercisesResult(exercises, totalSets, routineDescription);
	}

	private Exercise exerciseFromBackupDoc(ReadBackup.DocumentData exDoc) {
		Exercise ex = new Exercise();
		ex.setName(exDoc.fields.get("name"));
		ex.setDescription(exDoc.fields.get("description"));
		ex.setReps(exDoc.fields.get("reps"));
		ex.setSets(exDoc.fields.get("sets"));
		ex.setSerieTime(exDoc.fields.get("timeSets"));
		ex.setRestTimeSec(exDoc.fields.get("timePauseSec"));
		return ex;
	}

	private ExercisesResult loadExercisesFromFirestore(int level, String routineName)
			throws InterruptedException, ExecutionException {

		List<Exercise> exercises = new ArrayList<>();
		int totalSets = 0;
		String routineDescription = null;

		Controller controller = new Controller(true);
		this.db = controller.getDb();

		QuerySnapshot querySnapshot = db.collection("workouts").whereEqualTo("level", level)
				.whereEqualTo("name", routineName).get().get();
		if (querySnapshot.isEmpty())
			return new ExercisesResult(exercises, 0, null);

		DocumentSnapshot routineDoc = querySnapshot.getDocuments().get(0);
		routineDescription = routineDoc.getString("description");

		List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("exercises").get().get()
				.getDocuments();

		for (QueryDocumentSnapshot doc : exerciseDocs) {
			Exercise ex = new Exercise();
			String name = doc.getString("name");
			String description = doc.getString("description");
			if (name != null)
				ex.setName(name);
			if (description != null)
				ex.setDescription(description);

			ex.setReps(doc.get("reps"));
			ex.setSets(doc.get("sets"));
			ex.setSerieTime(doc.get("timeSets"));
			ex.setRestTimeSec(doc.get("timePauseSec"));
			exercises.add(ex);
			totalSets += ex.getSets();
		}

		return new ExercisesResult(exercises, totalSets, routineDescription);
	}

	private String getDefaultRoutineDescription(String routineDescription, List<Exercise> exercises) {
		if (routineDescription != null && !routineDescription.trim().isEmpty())
			return routineDescription;
		if (!exercises.isEmpty() && exercises.get(0).getDescription() != null)
			return exercises.get(0).getDescription();
		return "";
	}

	private void runExerciseThread(List<Exercise> exercises, JLabel label, Supplier<Boolean> stopSupplier,
			Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier, Object pauseLock, int mode, boolean canPause) {

		if (exercises == null || exercises.isEmpty())
			return;

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
					final int serieTimeFinal = serieTime;

					SwingUtilities.invokeLater(() -> {
						if (label == null)
							return;
						if (mode == 0)
							label.setText("Denbora totala: " + totalTime + " seg");
						else if (mode == 1)
							label.setText("Sets " + currentSet + " - " + currentSec + "/" + serieTimeFinal + " seg");
					});

					sleep(1000);
				}

				if (mode == 0) {
					completedSets++;
				}

				if (s < sets) {
					skipNow = false;
					boolean stopped = handleRestPeriod(restTime, mode, label, stopSupplier, skipRest, pauseSupplier,
							pauseLock, false);
					if (stopped) {
						if (mode == 0)
							totalSeconds = elapsedSeconds;
						return;
					}
				}
			}

			if (exIdx < exercises.size() - 1) {
				int interExerciseRest = restTime;
				skipNow = false;
				boolean stopped = handleRestPeriod(interExerciseRest, mode, label, stopSupplier, skipRest,
						pauseSupplier, pauseLock, true);
				if (stopped) {
					if (mode == 0)
						totalSeconds = elapsedSeconds;
					return;
				}
			}
		}
	}

	private boolean handleRestPeriod(int restDuration, int mode, JLabel label, Supplier<Boolean> stopSupplier,
			Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier, Object pauseLock, boolean isInterExercise) {

		int elapsed = 0;
		while (elapsed < restDuration && !skipNow) {

			if (stopSupplier != null && stopSupplier.get()) {
				return true;
			}

			if (skipRest != null && skipRest.get()) {
				skipNow = true;
				break;
			}

			if (pauseSupplier != null && pauseSupplier.get()) {
				waitIfPaused(pauseSupplier, pauseLock);
			}

			if (mode == 0) {
				elapsedSeconds++;
				totalSeconds = elapsedSeconds;
				int remaining = expectedTotalSeconds - elapsedSeconds + 1;
				if (remaining < 0)
					remaining = 0;
				totalTime = remaining;
			}

			final int currentSec = ++elapsed;
			final int remainingRest = restDuration - currentSec + 1;
			SwingUtilities.invokeLater(() -> {
				if (label == null)
					return;
				if (mode == 0) {
					label.setText("Denbora totala: " + totalTime + " seg");
				} else if (mode == 2) {
					label.setText("Atsedena " + remainingRest + "/" + restDuration + " seg");
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
		return false;
	}

	private void waitIfPaused(Supplier<Boolean> pauseSupplier, Object pauseLock) {
		while (pauseSupplier != null && pauseSupplier.get()) {
			synchronized (pauseLock) {
				try {
					pauseLock.wait();
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	public void startExerciseThreads(List<Exercise> exercises, JLabel labelTotal, JLabel labelSeries,
			JLabel labelDescansos, JLabel labelHasiera, Supplier<Boolean> stopSupplier, Supplier<Boolean> skipSupplier,
			Supplier<Boolean> pauseSupplier, Object lock, String routineName, boolean thread1, boolean thread2,
			boolean thread3) {

		new Thread(() -> {
			try {
				for (int i = 5; i > 0; i--) {
					final int countdown = i;
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

				if (labelTotal != null)
					labelTotal.setVisible(true);
				if (labelSeries != null)
					labelSeries.setVisible(true);
				if (labelDescansos != null)
					labelDescansos.setVisible(true);
				if (labelHasiera != null)
					labelHasiera.setVisible(false);

				int computedTotalSets = 0;
				int computedTotalSeconds = computeExpectedTotalSeconds(exercises);
				for (Exercise e : exercises) {
					computedTotalSets += e.getSets();
				}
				this.expectedTotalSets = computedTotalSets;
				this.expectedTotalSeconds = computedTotalSeconds;
				this.elapsedSeconds = 0;
				this.completedSets = 0;

				Thread tTotal = new Thread(() -> runExerciseThread(exercises, labelTotal, stopSupplier, skipSupplier,
						pauseSupplier, lock, 0, thread1));
				Thread tSeries = new Thread(() -> runExerciseThread(exercises, labelSeries, stopSupplier, skipSupplier,
						pauseSupplier, lock, 1, thread2));
				Thread tRest = new Thread(() -> runExerciseThread(exercises, labelDescansos, stopSupplier, skipSupplier,
						pauseSupplier, lock, 2, thread3));

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
					double pct = 0.0;
					if (popupExpectedSets > 0) {
						pct = (popupCompletedSets * 100.0) / popupExpectedSets;
					}
					final String pctStr = String.format("%.1f", pct).replace('.', ',');
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
							"Rutina amaitu da! Denbora totala: " + popupTime + " seg\nSeries egindakoak: "
									+ popupCompletedSets + " / " + popupExpectedSets + " (" + pctStr + "%)"));
					historyLog(routineName);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}).start();
	}

	private int computeExpectedTotalSeconds(List<Exercise> exercises) {
		int total = 0;
		if (exercises == null)
			return 0;
		for (int i = 0; i < exercises.size(); i++) {
			Exercise e = exercises.get(i);
			int sets = e.getSets();
			int serieTime = e.getSerieTime();
			int restTime = e.getRestTimeSec();
			total += sets * serieTime;
			if (sets > 1)
				total += restTime * (sets - 1);
			if (i < exercises.size() - 1)
				total += restTime;
		}
		return total;
	}

	public void sumLevel() {

		if (!amaituta)
			return;

		String emaila = createUserBackup.loadEmail();

		try {
			QuerySnapshot querySnapshot = db.collection("users").whereEqualTo("email", emaila).get().get();
			if (querySnapshot.isEmpty())
				return;

			DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);

			if (level < 5) {
				level++;
				Map<String, Object> data = new HashMap<>();
				data.put("level", level);
				db.collection("users").document(userDoc.getId()).update(data);
			}

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void historyLog(String routineName) {
		String email = createUserBackup.loadEmail();

		try {
			if (db != null) {
				QuerySnapshot routineQuery = db.collection("workouts").whereEqualTo("name", routineName).get().get();
				if (routineQuery.isEmpty()) {
					throw new Exception("No routine found online");
				}
				DocumentSnapshot routineDoc = routineQuery.getDocuments().get(0);

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

			offline.gehituSarrera(uid, email, fields);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
