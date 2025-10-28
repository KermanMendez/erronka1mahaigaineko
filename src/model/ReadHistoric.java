package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import controller.Controller;

public class ReadHistoric {

	Firestore db;

	public ReadHistoric(Boolean connect) {
		this.db = new Controller(connect).getDb();
	}

	public String[] getHistoric(int nivelSeleccionado, Boolean connect)
			throws InterruptedException, ExecutionException {

		if (connect) {

			String email = new CreateUserBackup().loadEmail();

			QuerySnapshot querySnapshot = db.collection("users").whereEqualTo("email", email).get().get();

			if (querySnapshot.isEmpty()) {
				return new String[] { "Ez dituzu oraindik ariketak egin" };
			}

			List<String> levels = new ArrayList<>();

			for (DocumentSnapshot routineDoc : querySnapshot.getDocuments()) {
				List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("historic")
						.whereEqualTo("level", nivelSeleccionado).get().get().getDocuments();

				for (DocumentSnapshot exerciseDoc : exerciseDocs) {
					String exerciseCompleted = exerciseDoc.getBoolean("completed") != null
							&& exerciseDoc.getBoolean("completed") ? "Bai" : "Ez";
					String exerciseDate = exerciseDoc.getString("date");
					int totalSets = exerciseDoc.getLong("totalSets") != null
							? exerciseDoc.getLong("totalSets").intValue()
							: 0;
					int totalTime = exerciseDoc.getLong("totalTime") != null
							? exerciseDoc.getLong("totalTime").intValue()
							: 0;
					String workoutId = exerciseDoc.getString("workoutId");
					String workoutName = workoutId;
					if (workoutId != null) {
						DocumentSnapshot workoutDoc = db.collection("workouts").document(workoutId).get().get();
						if (workoutDoc.exists() && workoutDoc.getString("name") != null) {
							workoutName = workoutDoc.getString("name");
						}
					}
					if (workoutName != null && exerciseDate != null) {
						levels.add("Workout: " + workoutName + " | Data: " + exerciseDate + " | Bukatuta: "
								+ exerciseCompleted + " | Total Sets: " + totalSets + " | Total Time: " + totalTime
								+ " segundu");
					}
				}
			}

			if (levels.isEmpty()) {
				return new String[] { "Ez daude historikorik workout honetan" };
			}

			return levels.toArray(new String[0]);
		}
		return new String[] { "Ez dago konexiorik" };
	}
}