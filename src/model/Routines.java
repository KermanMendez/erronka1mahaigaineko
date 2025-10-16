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

	public DefaultListModel<String> getListModel() {
		return listModel;
	}
}