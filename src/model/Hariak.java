package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import controller.Controller;

public class Hariak implements Runnable {

	private String threadName;

	Controller controller = new Controller();
	Firestore db = controller.getDb();

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

	public void start(int aukeratutakoMaila, String aukeratutakoRutina)
			throws InterruptedException, ExecutionException {

		getAriketak(aukeratutakoMaila, aukeratutakoRutina);

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
		} else {
			calculos(exercises);
		}

		return exercises;
	}

	private void calculos(List<Exercise> exercises) {

		Thread hilo1 = new Thread(() -> {
			System.out.println("\n=== HILO 1: Serie + Descanso alternados ===");
			int totalTime = 0;

			for (Exercise exercise : exercises) {
				System.out.println("\n[HILO 1] Ejercicio - Sets: " + exercise.getSets());

				for (int i = 1; i <= exercise.getSets(); i++) {
					int serieTime = exercise.getSerieTime();
					System.out.println("[HILO 1] Iniciando Serie " + i + " (" + serieTime + " segundos)");

					for (int segundo = 1; segundo <= serieTime; segundo++) {
						System.out.println(
								"[HILO 1] Serie " + i + " - Tiempo: " + segundo + "/" + serieTime + " segundos");
						totalTime++;

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					if (i < exercise.getSets()) {
						int restTime = exercise.getRestTimeSec();
						System.out.println("[HILO 1] Iniciando Descanso (" + restTime + " segundos)");

						for (int segundo = 1; segundo <= restTime; segundo++) {
							System.out.println("[HILO 1] Descanso - Tiempo: " + segundo + "/" + restTime + " segundos");
							totalTime++;

							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			System.out.println("\n[HILO 1] Tiempo total: " + totalTime + " segundos");
		});

		Thread hilo2 = new Thread(() -> {
			System.out.println("\n=== HILO 2: Contador de Tiempo Total ===");
			int totalTime = 0;

			for (Exercise exercise : exercises) {
				System.out.println("\n[HILO 2] Ejercicio - Sets: " + exercise.getSets());

				for (int i = 1; i <= exercise.getSets(); i++) {
					int serieTime = exercise.getSerieTime();

					for (int segundo = 1; segundo <= serieTime; segundo++) {
						totalTime++;
						System.out.println(
								"[HILO 2] Tiempo total transcurrido: " + totalTime + " segundos (Serie " + i + ")");

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					if (i < exercise.getSets()) {
						int restTime = exercise.getRestTimeSec();

						for (int segundo = 1; segundo <= restTime; segundo++) {
							totalTime++;
							System.out.println(
									"[HILO 2] Tiempo total transcurrido: " + totalTime + " segundos (Descanso)");

							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			System.out.println("\n[HILO 2] Tiempo total final: " + totalTime + " segundos");
		});

		hilo1.start();
		hilo2.start();

		try {
			hilo1.join();
			hilo2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("\n=== Los dos hilos han terminado ===");
	}
}