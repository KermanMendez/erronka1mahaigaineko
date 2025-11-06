package service;

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
import model.Exercise;
import model.RoutineData;
import service.BackupReaderService.BackupData;
import util.FirestoreUtils;

/**
 * Entrenamendu errutinen exekuzioa kudeatzen duen zerbitzua. Hari anitzak
 * erabiliz, online edo offline moduan funtzionatzen du.
 */
public class WorkoutExecutionService {

	/** Entrenamendua osatua izan den ala ez adierazten duen bandera */
	private boolean amaituta = false;

	/** Entrenamenduan igarotako segundo guztiak */
	private long totalSeconds;

	/** Osatutako serie kopurua */
	private int completedSets = 0;

	/** Espero den serie kopuru osoa */
	private int expectedTotalSets = 0;

	/** Geratzen den denbora segundutan */
	private int totalTime = 0;

	/** Igarotako segundoak */
	private int elapsedSeconds = 0;

	/** Espero den segundo kopuru osoa */
	private int expectedTotalSeconds = 0;

	/** Firestore datu-basearen instantzia */
	private Firestore db;

	/**
	 * Atsedena orain saltatu behar den adierazten duen bandera (volatile hari
	 * anitzek atzitu dezaketelako)
	 */
	private volatile boolean skipNow = false;

	/** Uneko maila */
	private int level;

	/** Firestore erabiltzeko utilitatea */
	private FirestoreUtils firestoreUtils = new FirestoreUtils();

	/**
	 * Ariketak, serie kopurua eta deskribapena biltzen dituen klase laguntzailea.
	 * 
	 * <p>
	 * Metodo pribatuetan erabiltzen da emaitzak itzultzeko, Firestore edo backup
	 * lokal batetik kargatutako datuekin.
	 * </p>
	 */
	private class ExercisesResult {
		/** Ariketa zerrenda */
		final List<Exercise> exercises;

		/** Serie kopuru totala */
		final int totalSets;

		/** Rutinaren deskribapena */
		final String routineDescription;

		/**
		 * ExercisesResult instantzia berria sortzen du.
		 * 
		 * @param exercises          ariketa zerrenda
		 * @param totalSets          serie kopuru totala
		 * @param routineDescription rutinaren deskribapena
		 */
		ExercisesResult(List<Exercise> exercises, int totalSets, String routineDescription) {
			this.exercises = exercises;
			this.totalSets = totalSets;
			this.routineDescription = routineDescription;
		}
	}

	/**
	 * Rutinaren ariketa zerrenda soilik lortzen du.
	 * 
	 * <p>
	 * Metodo erraztu bat da {@link #loadRoutine(int, String, Boolean)} erabiltzen
	 * duena eta ariketak bakarrik itzultzen ditu.
	 * </p>
	 * 
	 * @param level       rutinaren zailtasun maila
	 * @param routineName rutinaren izena
	 * @param connect     konexioa erabilgarri dagoen adierazten du (true=online,
	 *                    false/null=offline)
	 * @return rutinaren ariketa zerrenda
	 * @throws InterruptedException haria eten bada
	 * @throws ExecutionException   exekuzioan erroreren bat gertatzen bada
	 */
	public List<Exercise> getExercises(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {
		RoutineData routineData = loadRoutine(level, routineName, connect);
		return routineData.getExercises();
	}

	/**
	 * Rutina osoa kargatzen du deskribapena eta serie kopuru osoarekin.
	 * 
	 * <p>
	 * Konexioaren arabera, Firestore-tik edo backup lokal batetik kargatzen du
	 * informazioa. Rutinaren ariketak, deskribapena eta serie kopuru totala
	 * itzultzen ditu.
	 * </p>
	 * 
	 * @param level       rutinaren zailtasun maila
	 * @param routineName rutinaren izena
	 * @param connect     konexioa erabilgarri dagoen adierazten du (true=online,
	 *                    false/null=offline)
	 * @return RoutineData objektua ariketak, deskribapena eta serie kopuruarekin
	 * @throws InterruptedException haria eten bada
	 * @throws ExecutionException   exekuzioan erroreren bat gertatzen bada
	 */
	public RoutineData loadRoutine(int level, String routineName, Boolean connect)
			throws InterruptedException, ExecutionException {

		this.level = level;

		if (connect == null || !connect) {
			BackupReaderService.BackupData backup = BackupReaderService.loadBackupSafe();
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

	/**
	 * Backup lokal batetik ariketak kargatzen ditu.
	 * 
	 * <p>
	 * Backup-eko datuetatik bilatzen du maila eta izen zehatzeko rutina bat, eta
	 * bere ariketak, serie kopurua eta deskribapena itzultzen ditu.
	 * </p>
	 * 
	 * @param level       rutinaren zailtasun maila
	 * @param routineName rutinaren izena
	 * @param backup      kargatu den backup datua
	 * @return ExercisesResult objektua ariketak eta datuak dituela
	 */
	private ExercisesResult loadExercisesFromBackup(int level, String routineName,
			BackupReaderService.BackupData backup) {
		List<Exercise> exercises = new ArrayList<>();
		int totalSets = 0;
		String routineDescription = null;

		if (backup == null || backup.collections == null)
			return new ExercisesResult(exercises, 0, null);

		List<BackupReaderService.DocumentData> workoutDocs = backup.collections.get("workouts");
		if (workoutDocs == null)
			return new ExercisesResult(exercises, 0, null);

		for (BackupReaderService.DocumentData d : workoutDocs) {
			String levelValue = d.fields.get("level");
			String nameValue = d.fields.get("name");
			if (levelValue != null && nameValue != null && levelValue.equals(String.valueOf(level))
					&& nameValue.equals(routineName)) {

				routineDescription = d.fields.get("description");

				List<BackupReaderService.DocumentData> exerciseDocs = d.subcollections.get("exercises");
				if (exerciseDocs == null)
					exerciseDocs = d.subcollections.get("exercise");

				if (exerciseDocs != null) {
					for (BackupReaderService.DocumentData exerciseDoc : exerciseDocs) {
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
	 * Exercise objektu bat sortzen du backup dokumentu batetik.
	 * 
	 * @param exDoc backup dokumentuaren datuak
	 * @return Exercise objektua osatutako datuekin
	 */
	private Exercise exerciseFromBackupDoc(BackupReaderService.DocumentData exDoc) {
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
	 * Exercise objektu bat sortzen du Firestore dokumentu batetik.
	 * 
	 * @param exerciseDoc Firestore dokumentuaren query snapshot-a
	 * @return Exercise objektua osatutako datuekin
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

	/**
	 * Firestore-tik ariketak kargatzen ditu.
	 * 
	 * <p>
	 * Firestore datu-basetik bilatzen du maila eta izen zehatzeko rutina bat, eta
	 * bere ariketak, serie kopurua eta deskribapena itzultzen ditu.
	 * </p>
	 * 
	 * @param level       rutinaren zailtasun maila
	 * @param routineName rutinaren izena
	 * @return ExercisesResult objektua ariketak eta datuak dituela
	 * @throws InterruptedException haria eten bada
	 * @throws ExecutionException   exekuzioan erroreren bat gertatzen bada
	 */
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

	/**
	 * Lehenetsitako rutina deskribapena lortzen du.
	 * 
	 * <p>
	 * Rutinaren deskribapena lehentasunarekin itzultzen du; bestela, lehen
	 * ariketaren deskribapena; bestela, testu hutsa.
	 * </p>
	 * 
	 * @param routineDescription rutinaren deskribapena
	 * @param exercises          ariketa zerrenda
	 * @return deskribapena edo testu hutsa
	 */
	private String getDefaultRoutineDescription(String routineDescription, List<Exercise> exercises) {
		if (routineDescription != null && !routineDescription.trim().isEmpty())
			return routineDescription;
		if (!exercises.isEmpty() && exercises.get(0).getDescription() != null)
			return exercises.get(0).getDescription();
		return "";
	}

	/**
	 * Ariketen haria exekutatzen du zehaztutako moduan.
	 * 
	 * <p>
	 * Metodo hau hiru modutan funtziona dezake:
	 * </p>
	 * <ul>
	 * <li>mode=0: denbora totala kontrolatzen du</li>
	 * <li>mode=1: serie unekoak kontrolatzen ditu</li>
	 * <li>mode=2: atsedenak kontrolatzen ditu</li>
	 * </ul>
	 * 
	 * @param exercises     exekutatu beharreko ariketa zerrenda
	 * @param label         eguneratu beharreko JLabel-a
	 * @param stopSupplier  gelditzeko baldintza hornitzen duen Supplier-a
	 * @param skipRest      atsedena saltatu behar den adierazten duen Supplier-a
	 * @param pauseSupplier pausan dagoen adierazten duen Supplier-a
	 * @param pauseLock     pausaren sinkronizaziorako blokeoa
	 * @param mode          exekuzio modua (0=denbora totala, 1=serieak,
	 *                      2=atsedenak)
	 * @param canPause      pausatzeko gai den ala ez
	 */
	private void runExerciseThread(List<Exercise> exercises, JLabel label, Supplier<Boolean> stopSupplier,
			Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier, Object pauseLock, int mode, boolean canPause) {

		if (exercises == null || exercises.isEmpty())
			return;

		for (int exerciseIndex = 0; exerciseIndex < exercises.size(); exerciseIndex++) {
			Exercise currentExercise = exercises.get(exerciseIndex);

			// Ariketaren serie guztiak exekutatu
			boolean stopped = executeExerciseSets(currentExercise, label, stopSupplier, skipRest, pauseSupplier,
					pauseLock, mode, canPause);
			if (stopped) {
				if (mode == 0)
					totalSeconds = elapsedSeconds;
				return;
			}

			// Ariketen arteko atsedena (azkena ez bada)
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
	 * Ariketa baten serie guztiak exekutatzen ditu.
	 * 
	 * <p>
	 * Metodo hau atera da bukle habiaratuen konplexutasuna murrizteko.
	 * </p>
	 * 
	 * @param exercise      exekutatu beharreko ariketa
	 * @param label         eguneratu beharreko JLabel-a
	 * @param stopSupplier  gelditzeko baldintza hornitzen duen Supplier-a
	 * @param skipRest      atsedena saltatu behar den adierazten duen Supplier-a
	 * @param pauseSupplier pausan dagoen adierazten duen Supplier-a
	 * @param pauseLock     pausaren sinkronizaziorako blokeoa
	 * @param mode          exekuzio modua
	 * @param canPause      pausatzeko gai den ala ez
	 * @return true gelditu bada, bestela false
	 */
	private boolean executeExerciseSets(Exercise exercise, JLabel label, Supplier<Boolean> stopSupplier,
			Supplier<Boolean> skipRest, Supplier<Boolean> pauseSupplier, Object pauseLock, int mode, boolean canPause) {

		int sets = exercise.getSets();
		int serieTime = exercise.getSerieTime();
		int restTime = exercise.getRestTimeSec();

		for (int setNumber = 1; setNumber <= sets; setNumber++) {
			// Serie oso bat exekutatu
			boolean stopped = executeSet(setNumber, serieTime, label, stopSupplier, pauseSupplier, pauseLock, mode,
					canPause);
			if (stopped) {
				return true;
			}

			if (mode == 0) {
				completedSets++;
			}

			// Serieen arteko atsedena (azkena ez bada)
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
	 * Serie (set) bakarra exekutatzen du.
	 * 
	 * <p>
	 * Metodo hau atera da bukle habiaratuen konplexutasuna murrizteko.
	 * </p>
	 * 
	 * @param currentSet    uneko serie zenbakia
	 * @param serieTime     serie bakoitzaren iraupena segundutan
	 * @param label         eguneratu beharreko JLabel-a
	 * @param stopSupplier  gelditzeko baldintza hornitzen duen Supplier-a
	 * @param pauseSupplier pausan dagoen adierazten duen Supplier-a
	 * @param pauseLock     pausaren sinkronizaziorako blokeoa
	 * @param mode          exekuzio modua
	 * @param canPause      pausatzeko gai den ala ez
	 * @return true gelditu bada, bestela false
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

	/**
	 * Atseden garaia kudeatzen du.
	 * 
	 * <p>
	 * Serieen arteko edo ariketen arteko atsedena kontrolatzen du, saltatzeko eta
	 * pausatzeko aukerak kontuan hartuz.
	 * </p>
	 * 
	 * @param restDuration    atseden iraupena segundutan
	 * @param mode            exekuzio modua
	 * @param label           eguneratu beharreko JLabel-a
	 * @param stopSupplier    gelditzeko baldintza hornitzen duen Supplier-a
	 * @param skipRest        atsedena saltatu behar den adierazten duen Supplier-a
	 * @param pauseSupplier   pausan dagoen adierazten duen Supplier-a
	 * @param pauseLock       pausaren sinkronizaziorako blokeoa
	 * @param isInterExercise ariketen artekoa den ala ez
	 * @return true gelditu bada, bestela false
	 */
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

	/**
	 * Pausan egonez gero, itxaroten du.
	 * 
	 * <p>
	 * Pausan dagoen bitartean, haria blokeoan mantentzen du pauseLock-ekin, eta
	 * pausatik ateratzen denean jarraituko du.
	 * </p>
	 * 
	 * @param pauseSupplier pausan dagoen adierazten duen Supplier-a
	 * @param pauseLock     pausaren sinkronizaziorako blokeoa
	 */
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

	/**
	 * Haria milisegundo jakin batzuez lo jartzen du.
	 * 
	 * @param ms lo jartzeko milisegundoak
	 */
	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Entrenamendua exekutatzen du.
	 * 
	 * <p>
	 * Metodo nagusia, rutina bat kargatzen du eta exekutatzen du hari berri batean.
	 * Interfaze grafikoko etiketak eguneratzen ditu eta callback-ak deitzen ditu
	 * entrenamendua hasterakoan eta amaitzerakoan.
	 * </p>
	 * 
	 * @param level                 rutinaren zailtasun maila
	 * @param routineName           rutinaren izena
	 * @param connect               konexioa erabilgarri dagoen adierazten du
	 * @param labelTotal            denbora totala bistaratzeko etiketa
	 * @param labelSeries           serieak bistaratzeko etiketa
	 * @param labelDescansos        atsedenak bistaratzeko etiketa
	 * @param labelHasiera          hasierako mezuak bistaratzeko etiketa
	 * @param lblRutinaDeskribapena rutinaren deskribapena bistaratzeko etiketa
	 * @param lblRutinaSets         serie kopurua bistaratzeko etiketa
	 * @param stopSupplier          gelditzeko baldintza hornitzen duen Supplier-a
	 * @param skipSupplier          atsedena saltatu behar den adierazten duen
	 *                              Supplier-a
	 * @param pauseSupplier         pausan dagoen adierazten duen Supplier-a
	 * @param lock                  pausaren sinkronizaziorako blokeoa
	 * @param onWorkoutStarted      entrenamendua hasterakoan exekutatuko den
	 *                              callback-a
	 * @param onWorkoutFinished     entrenamendua amaitzerakoan exekutatuko den
	 *                              callback-a
	 */
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

	/**
	 * Ariketa hariak hasten ditu.
	 * 
	 * <p>
	 * Hiru hari paraleloak sortzen ditu entrenamendua kontrolatzeko:
	 * </p>
	 * <ul>
	 * <li>Haria 1: denbora totala</li>
	 * <li>Haria 2: serie unekoak</li>
	 * <li>Haria 3: atsedenak</li>
	 * </ul>
	 * 
	 * <p>
	 * 5 segundoko kontaketa atzeratu bat egiten du hasi aurretik, eta entrenamendua
	 * amaitzen denean, estatistikak bistaratzen ditu eta historia gordetzen du.
	 * </p>
	 * 
	 * @param exercises         exekutatu beharreko ariketa zerrenda
	 * @param labelTotal        denbora totala bistaratzeko etiketa
	 * @param labelSeries       serieak bistaratzeko etiketa
	 * @param labelDescansos    atsedenak bistaratzeko etiketa
	 * @param labelHasiera      hasierako mezuak bistaratzeko etiketa
	 * @param stopSupplier      gelditzeko baldintza hornitzen duen Supplier-a
	 * @param skipSupplier      atsedena saltatu behar den adierazten duen
	 *                          Supplier-a
	 * @param pauseSupplier     pausan dagoen adierazten duen Supplier-a
	 * @param lock              pausaren sinkronizaziorako blokeoa
	 * @param routineName       rutinaren izena
	 * @param thread1           lehen haria pausatzeko gai den ala ez
	 * @param thread2           bigarren haria pausatzeko gai den ala ez
	 * @param thread3           hirugarren haria pausatzeko gai den ala ez
	 * @param onWorkoutStarted  entrenamendua hasterakoan exekutatuko den callback-a
	 * @param onWorkoutFinished entrenamendua amaitzerakoan exekutatuko den
	 *                          callback-a
	 */
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

				// Entrenamendua hasi dela jakinarazi (botoiak gaitu)
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
	 * Ariketa guztiak osatzeko espero den denbora totala kalkulatzen du.
	 * 
	 * <p>
	 * Serieen eta atsedenen denbora guztia barne hartzen du.
	 * </p>
	 * 
	 * @param exercises ariketa zerrenda
	 * @return espero den segundo kopuru totala
	 */
	private int computeExpectedTotalSeconds(List<Exercise> exercises) {
		if (exercises == null || exercises.isEmpty())
			return 0;

		int total = 0;
		for (int i = 0; i < exercises.size(); i++) {
			Exercise e = exercises.get(i);
			total += computeExerciseTime(e);

			// Ariketen arteko atsedena gehitu (azkena izan ezik)
			if (i < exercises.size() - 1) {
				total += e.getRestTimeSec();
			}
		}
		return total;
	}

	/**
	 * Ariketa bakar baten denbora totala kalkulatzen du.
	 * 
	 * <p>
	 * Serieen denbora eta serieen arteko atsedenen denbora barne hartzen du.
	 * </p>
	 * 
	 * @param exercise ariketa
	 * @return ariketaren segundo kopuru totala
	 */
	private int computeExerciseTime(Exercise exercise) {
		int sets = exercise.getSets();
		int serieTime = exercise.getSerieTime();
		int restTime = exercise.getRestTimeSec();

		// Serieen denbora totala
		int totalSerieTime = sets * serieTime;

		// Serieen arteko atsedenen denbora totala (n-1 atseden)
		int totalRestTime = (sets > 1) ? restTime * (sets - 1) : 0;

		return totalSerieTime + totalRestTime;
	}

	/**
	 * Erabiltzailearen uneko maila lortzen du.
	 * 
	 * <p>
	 * Firestore-tik erabiltzailearen maila kontsultatzen du bere email-aren bidez.
	 * Ez bada aurkitzen edo erroreren bat gertatzen bada, 1 itzultzen du.
	 * </p>
	 * 
	 * @return erabiltzailearen maila (gutxienez 1)
	 */
	public int getUserLevel() {
		String emaila = UserBackupService.getCurrentUserEmail();

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

	/**
	 * Erabiltzailearen maila gehitzen du.
	 * 
	 * <p>
	 * Entrenamendua osatu bada eta erabiltzailearen maila uneko mailaren berdina
	 * edo txikiagoa bada, maila bat gehitzen dio eta Firestore-n eguneratzen du.
	 * </p>
	 */
	public void sumLevel() {

		if (!amaituta)
			return;

		String emaila = UserBackupService.getCurrentUserEmail();

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

	/**
	 * Entrenamendua historian gordetzen du.
	 * 
	 * <p>
	 * Entrenamendua amaitzean, erabiltzailearen historian sarrera berri bat sortzen
	 * du honako informazioarekin: data, denbora totala, serie kopurua, rutinaren
	 * izena/ID-a, maila eta osatu den ala ez.
	 * </p>
	 * 
	 * <p>
	 * Konexioa badago, Firestore-n gordetzen du; bestela, backup lokalean gordetzen
	 * du OfflineHistoricService erabiliz.
	 * </p>
	 * 
	 * @param routineName rutinaren izena
	 */
	public void historyLog(String routineName) {
		String email = UserBackupService.getCurrentUserEmail();

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
			OfflineHistoricService offline = new OfflineHistoricService();
			BackupData backup = BackupReaderService.loadBackupSafe();
			String uid = firestoreUtils.getUserIdFromBackup(backup, email);

			String workoutId = null;
			if (backup != null && backup.collections != null) {
				List<BackupReaderService.DocumentData> workouts = backup.collections.get("workouts");
				if (workouts != null) {
					for (BackupReaderService.DocumentData wd : workouts) {
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
