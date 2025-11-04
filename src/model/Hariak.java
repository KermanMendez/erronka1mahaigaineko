package model;

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
import util.FirestoreUtils;

public class Hariak {

	private boolean amaituta = false;
	private long totalSeconds;
	private int completedSets = 0;
	private int expectedTotalSets = 0;
	private int totalTime = 0;
	private int elapsedSeconds = 0;
	private int expectedTotalSeconds = 0;
	private Firestore db;
	private volatile boolean skipNow = false;
	private int level;
	private FirestoreUtils firestoreUtils = new FirestoreUtils();

	/**
	 * Carga la rutina completa con todos sus datos
	 * 
	 * @deprecated Use loadRoutine() instead
	 */
	@Deprecated
	public List<Exercise> start(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {
		return getExercises(level, routineName, connect);
	}

	/**
	 * Rutinaren ariketa zerrenda soilik lortzen du Obtiene solo la lista de
	 * ejercicios de una rutina
	 * 
	 * Metodo erraztu bat da loadRoutine() erabiltzen duena Método simplificado que
	 * usa loadRoutine()
	 */
	public List<Exercise> getExercises(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {
		RoutineData routineData = loadRoutine(level, routineName, connect);
		return routineData.getExercises();
	}

	/**
	 * Carga la rutina completa con descripción y total de sets
	 */
	public RoutineData loadRoutine(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {

		this.level = level;

		if (connect == null || !connect) {
			ReadBackup.BackupData backup = ReadBackup.loadBackupSafe();
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
					for (ReadBackup.DocumentData exerciseDoc : exerciseDocs) {
						Exercise exercise = exerciseFromBackupDoc(exerciseDoc);
						exercises.add(exercise);
						totalSets += exercise.getSets();
					}
				}
				break;
			}
		}

		return new ExercisesResult(exercises, totalSets, routineDescription);
	}

	/**
	 * Crea un objeto Exercise desde un documento de backup
	 */
	private Exercise exerciseFromBackupDoc(ReadBackup.DocumentData exDoc) {
		Exercise exercise = new Exercise();
		exercise.setName(exDoc.fields.get("name"));
		exercise.setDescription(exDoc.fields.get("description"));
		exercise.setReps(exDoc.fields.get("reps"));
		exercise.setSets(exDoc.fields.get("sets"));
		exercise.setSerieTime(exDoc.fields.get("timeSets"));
		exercise.setRestTimeSec(exDoc.fields.get("timePauseSec"));
		return exercise;
	}

	/**
	 * Crea un objeto Exercise desde un documento de Firestore
	 */
	private Exercise exerciseFromFirestoreDoc(QueryDocumentSnapshot exerciseDoc) {
		Exercise exercise = new Exercise();
		String name = exerciseDoc.getString("name");
		String description = exerciseDoc.getString("description");

		if (name != null)
			exercise.setName(name);
		if (description != null)
			exercise.setDescription(description);

		exercise.setReps(exerciseDoc.get("reps"));
		exercise.setSets(exerciseDoc.get("sets"));
		exercise.setSerieTime(exerciseDoc.get("timeSets"));
		exercise.setRestTimeSec(exerciseDoc.get("timePauseSec"));

		return exercise;
	}

	private ExercisesResult loadExercisesFromFirestore(int level, String routineName)
			throws InterruptedException, ExecutionException {

		List<Exercise> exercises = new ArrayList<>();
		int totalSets = 0;
		String routineDescription = null;

		Controller controller = Controller.getInstance();
		this.db = controller.getDb();

		QuerySnapshot querySnapshot = db.collection("workouts").whereEqualTo("level", level)
				.whereEqualTo("name", routineName).get().get();
		if (querySnapshot.isEmpty())
			return new ExercisesResult(exercises, 0, null);

		DocumentSnapshot routineDoc = querySnapshot.getDocuments().get(0);
		routineDescription = routineDoc.getString("description");

		List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("exercises").get().get()
				.getDocuments();

		for (QueryDocumentSnapshot exerciseDoc : exerciseDocs) {
			Exercise exercise = exerciseFromFirestoreDoc(exerciseDoc);
			exercises.add(exercise);
			totalSets += exercise.getSets();
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

	/**
	 * Ejecuta el thread de ejercicios con el modo especificado Refactorizado para
	 * reducir complejidad de bucles anidados
	 */
	private void runExerciseThread(List<Exercise> exercises, JLabel label, Supplier<Boolean> stopSupplier,
			Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier, Object pauseLock, int mode, boolean canPause) {

		if (exercises == null || exercises.isEmpty())
			return;

		for (int exerciseIndex = 0; exerciseIndex < exercises.size(); exerciseIndex++) {
			Exercise currentExercise = exercises.get(exerciseIndex);

			// Ejecutar todos los sets del ejercicio
			boolean stopped = executeExerciseSets(currentExercise, label, stopSupplier, skipRest, pauseSupplier,
					pauseLock, mode, canPause);
			if (stopped) {
				if (mode == 0)
					totalSeconds = elapsedSeconds;
				return;
			}

			// Descanso entre ejercicios (si no es el último)
			if (exerciseIndex < exercises.size() - 1) {
				skipNow = false;
				stopped = handleRestPeriod(currentExercise.getRestTimeSec(), mode, label, stopSupplier, skipRest,
						pauseSupplier, pauseLock, true);
				if (stopped) {
					if (mode == 0)
						totalSeconds = elapsedSeconds;
					return;
				}
			}
		}
	}

	/**
	 * Ejecuta todos los sets de un ejercicio Extraído para reducir complejidad de
	 * bucles anidados
	 */
	private boolean executeExerciseSets(Exercise exercise, JLabel label, Supplier<Boolean> stopSupplier,
			Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier, Object pauseLock, int mode, boolean canPause) {

		int sets = exercise.getSets();
		int serieTime = exercise.getSerieTime();
		int restTime = exercise.getRestTimeSec();

		for (int setNumber = 1; setNumber <= sets; setNumber++) {
			// Ejecutar una serie completa
			boolean stopped = executeSet(setNumber, serieTime, label, stopSupplier, pauseSupplier, pauseLock, mode,
					canPause);
			if (stopped) {
				return true;
			}

			if (mode == 0) {
				completedSets++;
			}

			// Descanso entre series (si no es la última)
			if (setNumber < sets) {
				skipNow = false;
				stopped = handleRestPeriod(restTime, mode, label, stopSupplier, skipRest, pauseSupplier, pauseLock,
						false);
				if (stopped) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Ejecuta un set (serie) individual Extraído para reducir complejidad de bucles
	 * anidados
	 */
	private boolean executeSet(int currentSet, int serieTime, JLabel label, Supplier<Boolean> stopSupplier,
			Supplier<Boolean> pauseSupplier, Object pauseLock, int mode, boolean canPause) {

		for (int secondsElapsed = 1; secondsElapsed <= serieTime; secondsElapsed++) {
			if (stopSupplier != null && stopSupplier.get()) {
				if (mode == 0)
					totalSeconds = elapsedSeconds;
				return true;
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

			final int currentSec = secondsElapsed;
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
		return false;
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

	public void executeWorkout(int level, String routineName, Boolean connect, JLabel labelTotal, JLabel labelSeries,
			JLabel labelDescansos, JLabel labelHasiera, JLabel lblRutinaDeskribapena, JLabel lblRutinaSets,
			Supplier<Boolean> stopSupplier, Supplier<Boolean> skipSupplier, Supplier<Boolean> pauseSupplier,
			Object lock, Runnable onWorkoutStarted, Runnable onWorkoutFinished) {

		new Thread(() -> {
			try {
				RoutineData result = loadRoutine(level, routineName, connect);
				List<Exercise> exercises = result.getExercises();
				String desc = result.getDescription();
				int totalSets = result.getTotalSets();

				final String description = (desc == null || desc.trim().isEmpty()) ? "Ez da deskripziorik aurkitu"
						: desc;
				final int finalTotalSets = totalSets;

				SwingUtilities.invokeLater(() -> {
					if (lblRutinaDeskribapena != null)
						lblRutinaDeskribapena.setText(description);
					if (lblRutinaSets != null)
						lblRutinaSets.setText("Serieak: " + finalTotalSets);
				});

				startExerciseThreads(exercises, labelTotal, labelSeries, labelDescansos, labelHasiera, stopSupplier,
						skipSupplier, pauseSupplier, lock, routineName, true, true, true, onWorkoutStarted,
						onWorkoutFinished);

			} catch (InterruptedException | ExecutionException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	public void startExerciseThreads(List<Exercise> exercises, JLabel labelTotal, JLabel labelSeries,
			JLabel labelDescansos, JLabel labelHasiera, Supplier<Boolean> stopSupplier, Supplier<Boolean> skipSupplier,
			Supplier<Boolean> pauseSupplier, Object lock, String routineName, boolean thread1, boolean thread2,
			boolean thread3, Runnable onWorkoutStarted, Runnable onWorkoutFinished) {

		new Thread(() -> {
			try {
				for (int i = 5; i > 0; i--) {
					final int countdown = i;
					SwingUtilities.invokeLater(() -> {
						if (labelHasiera != null)
							labelHasiera.setText("Prest! Hasten da " + countdown + " segundutan...");
					});
					Thread.sleep(1000);
				}

				SwingUtilities.invokeLater(() -> {
					if (labelHasiera != null)
						labelHasiera.setText("Goazen! Entrenamendua hasi da!");
				});
				Thread.sleep(1000);

				if (exercises == null || exercises.isEmpty()) {
					SwingUtilities.invokeLater(() -> {
						if (labelHasiera != null)
							labelHasiera.setText("Ez da ariketarik aurkitu rutina honetarako!");
					});
					System.err.println("[ERROR] Ez da ariketarik aurkitu");
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

				// Notificar que el entrenamiento ha comenzado (habilitar botones)
				if (onWorkoutStarted != null) {
					SwingUtilities.invokeLater(onWorkoutStarted);
				}

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
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(null,
								"Rutina amaitu duzu!\n\n" + " Estatistikak:\n" + "  Denbora totala: " + popupTime
										+ " segundo\n" + "  Serieak: " + popupCompletedSets + " / " + popupExpectedSets
										+ " (" + pctStr + "%)\n\n" + "Zorionak zure ahaleginagatik!",
								"Rutina Amaituta", JOptionPane.INFORMATION_MESSAGE);
						// Callback exekutatu popup itxi ondoren
						if (onWorkoutFinished != null) {
							onWorkoutFinished.run();
						}
					});
					historyLog(routineName);
				} else {
					// Ez bada serie bat ere egin, callback berehalakoa
					SwingUtilities.invokeLater(() -> {
						if (onWorkoutFinished != null) {
							onWorkoutFinished.run();
						}
					});
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}).start();
	}

	/**
	 * Calcula el tiempo total esperado para completar todos los ejercicios Incluye
	 * tiempo de series y descansos
	 */
	private int computeExpectedTotalSeconds(List<Exercise> exercises) {
		if (exercises == null || exercises.isEmpty())
			return 0;

		int total = 0;
		for (int i = 0; i < exercises.size(); i++) {
			Exercise e = exercises.get(i);
			total += computeExerciseTime(e);

			// Añadir descanso entre ejercicios (excepto el último)
			if (i < exercises.size() - 1) {
				total += e.getRestTimeSec();
			}
		}
		return total;
	}

	/**
	 * Calcula el tiempo total de un ejercicio individual (tiempo de series +
	 * descansos entre series)
	 */
	private int computeExerciseTime(Exercise exercise) {
		int sets = exercise.getSets();
		int serieTime = exercise.getSerieTime();
		int restTime = exercise.getRestTimeSec();

		// Tiempo total de series
		int totalSerieTime = sets * serieTime;

		// Tiempo total de descansos entre series (n-1 descansos)
		int totalRestTime = (sets > 1) ? restTime * (sets - 1) : 0;

		return totalSerieTime + totalRestTime;
	}

	public int getUserLevel() {
		String emaila = CreateUserBackup.getCurrentUserEmail();

		int userLevel = 0;
		try {
			DocumentSnapshot userDoc = firestoreUtils.getUserDocumentByEmail(db, emaila);
			if (userDoc == null) {
				userLevel = 1;
				return userLevel;
			}

			Object levelObj = userDoc.get("level");
			userLevel = util.ParseUtils.parseInt(levelObj);
			if (userLevel < 1)
				userLevel = 1;

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return userLevel;
	}

	public void sumLevel() {

		if (!amaituta)
			return;

		String emaila = CreateUserBackup.getCurrentUserEmail();

		int userLevel = getUserLevel();

		if (level < userLevel) {
			return;
		} else {
			try {
				DocumentSnapshot userDoc = firestoreUtils.getUserDocumentByEmail(db, emaila);
				if (userDoc == null)
					return;

				level++;
				Map<String, Object> data = new HashMap<>();
				data.put("level", level);
				db.collection("users").document(userDoc.getId()).update(data);

			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	public void historyLog(String routineName) {
		String email = CreateUserBackup.getCurrentUserEmail();

		try {
			if (db != null) {
				QuerySnapshot routineQuery = db.collection("workouts").whereEqualTo("name", routineName).get().get();
				if (routineQuery.isEmpty()) {
					throw new Exception("No routine found online");
				}
				DocumentSnapshot routineDoc = routineQuery.getDocuments().get(0);

				String userId = firestoreUtils.getUserIdByEmail(db, email);
				if (userId == null)
					return;

				CollectionReference history = db.collection("users").document(userId).collection("historic");

				String today = util.DateUtils.getCurrentFormattedDate();
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
			ReadBackup.BackupData backup = ReadBackup.loadBackupSafe();
			String uid = firestoreUtils.getUserIdFromBackup(backup, email);

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

			String today = util.DateUtils.getCurrentFormattedDate();
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
