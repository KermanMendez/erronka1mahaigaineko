package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import controller.Controller;

public class Routines {

	private final Firestore db;
	private final DefaultListModel<String> listModel = new DefaultListModel<>();

	public Routines() {
		this.db = new Controller().getDb();
	}

	public void ariketak(int aukera) {
		int maila = aukera + 1;

		new Thread(() -> {
			try {
				List<Exercise> exercises = getAriketak(maila);

				SwingUtilities.invokeLater(() -> {
					listModel.clear();
					if (exercises.isEmpty()) {
						listModel.addElement("Ez daude ariketarik maila honetarako");
						return;
					}

					exercises.forEach(exercise -> {
						System.out.println(exercise.getName() + " - Sets: " + exercise.getSets() + ", Reps: "
								+ exercise.getReps());
						listModel.addElement(exercise.toString());
					});
				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	private List<Exercise> getAriketak(int level) throws InterruptedException, ExecutionException {
		List<Exercise> exercises = new ArrayList<>();

		QuerySnapshot querySnapshot = db.collection("workouts").whereEqualTo("level", level).get().get();

		if (querySnapshot.isEmpty())
			return exercises;

		DocumentSnapshot routineDoc = querySnapshot.getDocuments().get(0);

		List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("exercise").get().get()
				.getDocuments();

		exerciseDocs.forEach(doc -> {
			Exercise exercise = doc.toObject(Exercise.class);
			exercises.add(exercise);
		});

		return exercises;
	}

	public String[] getRoutines(int selectedLevel) throws InterruptedException, ExecutionException {

		QuerySnapshot querySnapshot = db.collection("workouts").whereEqualTo("level", selectedLevel).get().get();

		if (querySnapshot.isEmpty())
			return new String[] { "Ez daude workout-ak maila honetarako" };

		List<String> workoutNames = new ArrayList<>();

		for (DocumentSnapshot routineDoc : querySnapshot.getDocuments()) {
			String name = routineDoc.getString("name");
			if (name != null) {
				workoutNames.add(name);
			}
		}

		return workoutNames.toArray(new String[0]);

	}

	public String[] getLevels(int nivelSeleccionado, String nivelText) throws InterruptedException, ExecutionException {

		QuerySnapshot querySnapshot = db.collection("workouts").whereEqualTo("level", nivelSeleccionado)
				.whereEqualTo("name", nivelText).get().get();

		if (querySnapshot.isEmpty()) {
			return new String[] { "Ez daude workout-ak maila honetarako" };
		}

		List<String> levels = new ArrayList<>();

		for (DocumentSnapshot routineDoc : querySnapshot.getDocuments()) {
			List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("exercises").get().get()
					.getDocuments();

			for (DocumentSnapshot exerciseDoc : exerciseDocs) {
				String exerciseName = exerciseDoc.getString("name");
				String exerciseDesc = exerciseDoc.getString("description");
				
				Object setsObj = exerciseDoc.get("sets");
				int sets = 0;
				if (setsObj != null) {
					sets = Integer.parseInt(setsObj.toString());
				}

				if (exerciseName != null && exerciseDesc != null) {
					levels.add(exerciseName + " – " + exerciseDesc + " (Total Sets: " + sets + ")");
				}
			}
		}

		if (levels.isEmpty()) {
			return new String[] { "Ez daude ariketarik workout honetan" };
		}

		return levels.toArray(new String[0]);
	}

	public DefaultListModel<String> getListModel() {
		return listModel;
	}
}