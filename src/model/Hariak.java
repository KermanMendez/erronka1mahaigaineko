package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import controller.Controller;

public class Hariak implements Runnable {

	private String threadName;
	private Controller controller = new Controller();
	private Firestore db = controller.getDb();
	private int sec;

	public Hariak() {
		this.threadName = "";
	}

	public Hariak(String name) {
		this.threadName = name;
	}

	@Override
	public void run() {
		for (int i = 0; i < 5; i++) {
			System.out.println("Thread " + threadName + " is running: " + i);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println("Thread " + threadName + " interrupted.");
			}
		}
		System.out.println("Thread " + threadName + " in execution.");
	}

	public List<Exercise> start(int aukeratutakoMaila, String aukeratutakoRutina)
			throws InterruptedException, ExecutionException {
		return getAriketak(aukeratutakoMaila, aukeratutakoRutina);
	}

	private List<Exercise> getAriketak(int level, String aukeratutakoRutina)
			throws InterruptedException, ExecutionException {

		List<Exercise> exercises = new ArrayList<>();

		QuerySnapshot querySnapshot = db.collection("workouts").whereEqualTo("level", level)
				.whereEqualTo("name", aukeratutakoRutina).get().get();

		if (querySnapshot.isEmpty()) {
			System.out.println("No se encontraron workouts para el nivel " + level);
			return exercises;
		}

		DocumentSnapshot routineDoc = querySnapshot.getDocuments().get(0);

		List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("exercise").get().get()
				.getDocuments();

		System.out.println("Número de ejercicios encontrados: " + exerciseDocs.size());

		exerciseDocs.forEach(doc -> {
			Exercise exercise = new Exercise();
			exercise.setReps(doc.get("reps"));
			exercise.setSets(doc.get("sets"));
			exercise.setSerieTime(doc.get("timeSets"));
			exercise.setRestTimeSec(doc.get("timePauseSec"));
			exercises.add(exercise);
		});

		if (exercises.isEmpty()) {
			System.out.println("La lista de ejercicios está vacía");
		}

		return exercises;
	}

	public Thread calculosWithStop(List<Exercise> exercises, DefaultListModel<String> listModel,
			java.util.function.Supplier<Boolean> geldituEgoeraHornitzailea) {
		Thread hilo1 = new Thread(() -> {
			int[] totalTime = { 0 };
			for (Exercise exercise : exercises) {
				int sets = exercise.getSets();
				int serieTime = exercise.getSerieTime();
				int restTime = exercise.getRestTimeSec();
				for (int i = 1; i <= sets; i++) {
					final int setNumber = i;
					for (int segundo = 1; segundo <= serieTime; segundo++) {
						if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
							return;
						final int sec = segundo;
						totalTime[0]++;
						javax.swing.SwingUtilities.invokeLater(() -> listModel.addElement(
								"[HILO 1] Serie " + setNumber + " - Tiempo: " + sec + "/" + serieTime + " seg"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
					if (i < sets) {
						for (int segundo = 1; segundo <= restTime; segundo++) {
							if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
								return;
							final int sec = segundo;
							totalTime[0]++;
							javax.swing.SwingUtilities.invokeLater(() -> listModel
									.addElement("[HILO 1] Descanso - Tiempo: " + sec + "/" + restTime + " seg"));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
				}
			}
			javax.swing.SwingUtilities
					.invokeLater(() -> listModel.addElement("[HILO 1] Tiempo total: " + totalTime[0] + " seg"));
		});
		Thread hilo2 = new Thread(() -> {
			int[] totalTime = { 0 };
			for (Exercise exercise : exercises) {
				int sets = exercise.getSets();
				int serieTime = exercise.getSerieTime();
				int restTime = exercise.getRestTimeSec();
				for (int i = 1; i <= sets; i++) {
					final int setNumber = i;
					for (int segundo = 1; segundo <= serieTime; segundo++) {
						sec = segundo;
						totalTime[0]++;
						javax.swing.SwingUtilities
								.invokeLater(() -> listModel.addElement("[HILO 2] Tiempo total transcurrido: "
										+ totalTime[0] + " seg (Serie " + setNumber + ")"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
					if (i < sets) {
						for (int segundo = 1; segundo <= restTime; segundo++) {
							sec = segundo;
							totalTime[0]++;
							javax.swing.SwingUtilities.invokeLater(() -> listModel.addElement(
									"[HILO 2] Tiempo total transcurrido: " + totalTime[0] + " seg (Descanso)"));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
				}
			}
			javax.swing.SwingUtilities
					.invokeLater(() -> listModel.addElement("[HILO 2] Tiempo total final: " + totalTime[0] + " seg"));
		});
		hilo1.start();
		hilo2.start();
		return hilo1;
	}

	public Thread calculosWithStop(List<Exercise> exercises, DefaultListModel<String> listModel,
			java.util.function.Supplier<Boolean> geldituEgoeraHornitzailea,
			java.util.function.Supplier<Boolean> atsedenSalto) {
		long startTime = System.currentTimeMillis();
		Thread hilo1 = new Thread(() -> {
			int[] totalTime = { 0 };
			for (int exIdx = 0; exIdx < exercises.size(); exIdx++) {
				Exercise exercise = exercises.get(exIdx);
				int sets = exercise.getSets();
				int serieTime = exercise.getSerieTime();
				int restTime = exercise.getRestTimeSec();
				for (int i = 1; i <= sets; i++) {
					final int setNumber = i;
					for (int segundo = 1; segundo <= serieTime; segundo++) {
						if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
							return;
						sec = segundo;
						totalTime[0]++;
						javax.swing.SwingUtilities.invokeLater(() -> listModel.addElement(
								"[HILO 1] Serie " + setNumber + " - Tiempo: " + sec + "/" + serieTime + " seg"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
					if (i < sets) {
						for (int segundo = 1; segundo <= restTime; segundo++) {
							if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
								return;
							if (atsedenSalto.get())
								break;
							final int sec = segundo;
							totalTime[0]++;
							javax.swing.SwingUtilities.invokeLater(() -> listModel
									.addElement("[HILO 1] Descanso - Tiempo: " + sec + "/" + restTime + " seg"));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
					if (exIdx == exercises.size() - 1 && i == sets) {
						long endTime = System.currentTimeMillis();
						long totalSeconds = (endTime - startTime) / 1000;
						System.out.println("Workout completado en: " + totalSeconds + " segundos");
						return;
					}
				}
			}
			javax.swing.SwingUtilities
					.invokeLater(() -> listModel.addElement("[HILO 1] Tiempo total: " + totalTime[0] + " seg"));
		});
		Thread hilo2 = new Thread(() -> {
			int[] totalTime = { 0 };
			for (int exIdx = 0; exIdx < exercises.size(); exIdx++) {
				Exercise exercise = exercises.get(exIdx);
				int sets = exercise.getSets();
				int serieTime = exercise.getSerieTime();
				int restTime = exercise.getRestTimeSec();
				for (int i = 1; i <= sets; i++) {
					final int setNumber = i;
					for (int segundo = 1; segundo <= serieTime; segundo++) {
						sec = segundo;
						totalTime[0]++;
						javax.swing.SwingUtilities
								.invokeLater(() -> listModel.addElement("[HILO 2] Tiempo total transcurrido: "
										+ totalTime[0] + " seg (Serie " + setNumber + ")"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
					if (i < sets) {
						for (int segundo = 1; segundo <= restTime; segundo++) {
							sec = segundo;
							totalTime[0]++;
							javax.swing.SwingUtilities.invokeLater(() -> listModel.addElement(
									"[HILO 2] Tiempo total transcurrido: " + totalTime[0] + " seg (Descanso)"));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
					}

					if (exIdx == exercises.size() - 1 && i == sets) {
						long endTime = System.currentTimeMillis();
						long totalSeconds = (endTime - startTime) / 1000;
						System.out.println("Workout completado en: " + totalSeconds + " segundos");
						return;
					}
				}
			}
			javax.swing.SwingUtilities
					.invokeLater(() -> listModel.addElement("[HILO 2] Tiempo total final: " + totalTime[0] + " seg"));
		});
		hilo1.start();
		hilo2.start();
		return hilo1;
	}

	public Thread calculosWithStop(List<Exercise> exercises, DefaultListModel<String> listModel,
			java.util.function.Supplier<Boolean> geldituEgoeraHornitzailea,
			java.util.function.Supplier<Boolean> atsedenSalto, java.util.function.Supplier<Boolean> geldituEgoera,
			Object geldituDenbora) {
		long startTime = System.currentTimeMillis();
		Thread hilo1 = new Thread(() -> {
			int[] totalTime = { 0 };
			for (int exIdx = 0; exIdx < exercises.size(); exIdx++) {
				Exercise exercise = exercises.get(exIdx);
				int sets = exercise.getSets();
				int serieTime = exercise.getSerieTime();
				int restTime = exercise.getRestTimeSec();
				for (int i = 1; i <= sets; i++) {
					final int setNumber = i;
					for (int segundo = 1; segundo <= serieTime; segundo++) {
						if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
							return;
						while (geldituEgoera.get()) {
							synchronized (geldituDenbora) {
								try {
									geldituDenbora.wait();
								} catch (InterruptedException e) {
									return;
								}
							}
						}
						final int sec = segundo;
						totalTime[0]++;
						javax.swing.SwingUtilities.invokeLater(() -> listModel.addElement(
								"[HILO 1] Serie " + setNumber + " - Tiempo: " + sec + "/" + serieTime + " seg"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
					if (i < sets) {
						for (int segundo = 1; segundo <= restTime; segundo++) {
							if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
								return;
							while (geldituEgoera.get()) {
								synchronized (geldituDenbora) {
									try {
										geldituDenbora.wait();
									} catch (InterruptedException e) {
										return;
									}
								}
							}
							if (atsedenSalto.get())
								break;
							final int sec = segundo;
							totalTime[0]++;
							javax.swing.SwingUtilities.invokeLater(() -> listModel
									.addElement("[HILO 1] Descanso - Tiempo: " + sec + "/" + restTime + " seg"));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
					if (exIdx == exercises.size() - 1 && i == sets) {
						long endTime = System.currentTimeMillis();
						long totalSeconds = (endTime - startTime) / 1000;
						System.out.println("Workout completado en: " + totalSeconds + " segundos");
						return;
					}
				}
			}
			javax.swing.SwingUtilities
					.invokeLater(() -> listModel.addElement("[HILO 1] Tiempo total: " + totalTime[0] + " seg"));
		});
		Thread hilo2 = new Thread(() -> {
			int[] totalTime = { 0 };
			for (int exIdx = 0; exIdx < exercises.size(); exIdx++) {
				Exercise exercise = exercises.get(exIdx);
				int sets = exercise.getSets();
				int serieTime = exercise.getSerieTime();
				int restTime = exercise.getRestTimeSec();
				for (int i = 1; i <= sets; i++) {
					final int setNumber = i;
					for (int segundo = 1; segundo <= serieTime; segundo++) {
						sec = segundo;
						totalTime[0]++;
						javax.swing.SwingUtilities
								.invokeLater(() -> listModel.addElement("[HILO 2] Tiempo total transcurrido: "
										+ totalTime[0] + " seg (Serie " + setNumber + ")"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
					if (i < sets) {
						for (int segundo = 1; segundo <= restTime; segundo++) {
							sec = segundo;
							totalTime[0]++;
							javax.swing.SwingUtilities.invokeLater(() -> listModel.addElement(
									"[HILO 2] Tiempo total transcurrido: " + totalTime[0] + " seg (Descanso)"));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
					if (exIdx == exercises.size() - 1 && i == sets) {
						long endTime = System.currentTimeMillis();
						long totalSeconds = (endTime - startTime) / 1000;
						System.out.println("Workout completado en: " + totalSeconds + " segundos");
						return;
					}
				}
			}
			javax.swing.SwingUtilities
					.invokeLater(() -> listModel.addElement("[HILO 2] Tiempo total final: " + totalTime[0] + " seg"));
		});
		hilo1.start();
		hilo2.start();
		return hilo1;
	}

	public void startThreads(List<Exercise> exercises, DefaultListModel<String> listModel,
			java.util.function.Supplier<Boolean> geldituEgoeraHornitzailea) {
		// Hilo 1: Countdown
		Thread countdownThread = new Thread(() -> {
			// Fixed the scope of the variable 'i' to avoid potential conflicts
			for (int i = 5; i >= 0; i--) {
				final int countdownValue = i; // Use a final variable for thread-safe operations
				System.out.println("Cuenta regresiva: " + countdownValue);
				javax.swing.SwingUtilities
						.invokeLater(() -> listModel.addElement("Cuenta regresiva: " + countdownValue));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}
			System.out.println("¡Comienzan los hilos!");
			javax.swing.SwingUtilities.invokeLater(() -> listModel.addElement("¡Comienzan los hilos!"));

			// Start other threads after countdown
			startExerciseThreads(exercises, listModel, geldituEgoeraHornitzailea);
		});

		countdownThread.start();
	}

	private void startExerciseThreads(List<Exercise> exercises, DefaultListModel<String> listModel,
			java.util.function.Supplier<Boolean> geldituEgoeraHornitzailea) {
		// Hilo 2: Total elapsed time
		Thread totalElapsedTimeThread = new Thread(() -> {
			int totalTime = 0;
			synchronized (this) { // Added synchronization block for thread safety
				for (Exercise exercise : exercises) {
					int sets = exercise.getSets();
					int serieTime = exercise.getSerieTime();
					int restTime = exercise.getRestTimeSec();
					for (int i = 1; i <= sets; i++) {
						for (int segundo = 1; segundo <= serieTime; segundo++) {
							if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
								return;
							totalTime++;
							final int elapsedTime = totalTime; // Use a final variable for thread-safe operations
							javax.swing.SwingUtilities.invokeLater(() -> listModel
									.addElement("[HILO 2] Tiempo total transcurrido: " + elapsedTime + " seg"));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
						for (int segundo = 1; segundo <= restTime; segundo++) {
							if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
								return;
							totalTime++;
							final int elapsedTime = totalTime; // Use a final variable for thread-safe operations
							javax.swing.SwingUtilities.invokeLater(() -> listModel
									.addElement("[HILO 2] Tiempo total transcurrido: " + elapsedTime + " seg"));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
				}
			}
		});

		// Hilo 3: Time per series
		Thread seriesTimeThread = new Thread(() -> {
			for (Exercise exercise : exercises) {
				int sets = exercise.getSets();
				int serieTime = exercise.getSerieTime();
				for (int i = 1; i <= sets; i++) {
					final int setNumber = i;
					for (int segundo = 1; segundo <= serieTime; segundo++) {
						if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
							return;
						final int sec = segundo;
						javax.swing.SwingUtilities.invokeLater(() -> listModel.addElement(
								"[HILO 3] Serie " + setNumber + " - Tiempo: " + sec + "/" + serieTime + " seg"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
				}
			}
		});

		// Hilo 4: Rest time
		Thread restTimeThread = new Thread(() -> {
			for (Exercise exercise : exercises) {
				int sets = exercise.getSets();
				int restTime = exercise.getRestTimeSec();
				for (int i = 1; i < sets; i++) { // Rest occurs between sets
					for (int segundo = 1; segundo <= restTime; segundo++) {
						if (geldituEgoeraHornitzailea.get() || Thread.currentThread().isInterrupted())
							return;
						final int sec = segundo;
						javax.swing.SwingUtilities.invokeLater(() -> listModel
								.addElement("[HILO 4] Descanso - Tiempo: " + sec + "/" + restTime + " seg"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
				}
			}
		});

		totalElapsedTimeThread.start();
		seriesTimeThread.start();
		restTimeThread.start();
	}

	public String historyLog() {
		CreateUserBackup createUserBackup = new CreateUserBackup();
		String email = createUserBackup.loadEmail();
		try {
			CollectionReference usersCollection = db.collection("users");
			ApiFuture<QuerySnapshot> query = usersCollection.whereEqualTo("email", email).get();
			List<QueryDocumentSnapshot> documents = query.get().getDocuments();
			System.out.println("Número de documentos encontrados para el email " + email + ": " + documents.size());
			return documents.isEmpty() ? null : documents.get(0).getId();
		} catch (Exception e) {
			System.out.println("Malo malo");
			return null;
		}
	}

	public int getSec() {
		return sec;
	}

	public void setSec(int sec) {
		this.sec = sec;
	}

	public void stopWorkout() {

		Thread.currentThread().interrupt();
	}
}