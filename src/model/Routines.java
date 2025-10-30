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
	private final Boolean connect;
	private final DefaultListModel<String> listModel = new DefaultListModel<>();
	CreateUserBackup createUserBackup = new CreateUserBackup();

	public Routines(Boolean connect) {
		this.db = new Controller(connect).getDb();
		this.connect = connect;
	}

	public String[] levels() {

		String emaila = createUserBackup.loadEmail();
		int level = 1;

		try {
			if (connect == null || !connect || db == null) {
				ReadBackup reader = new ReadBackup();
				ReadBackup.BackupData backup = reader.loadBackupData();
				if (backup == null)
					return new String[] { "No levels available" };

				List<ReadBackup.DocumentData> users = backup.collections.get("users");
				if (users == null)
					return new String[] { "No levels available" };

				for (ReadBackup.DocumentData ud : users) {
					String userEmail = ud.fields.get("email");
					if (userEmail != null && userEmail.equals(emaila)) {
						String levelStr = ud.fields.get("level");
						if (levelStr != null) {
							try {
								level = Integer.parseInt(levelStr);
							} catch (NumberFormatException nfe) {
								level = 1;
							}
						}
						break;
					}
				}

				String[] levelsArray = new String[level];
				for (int i = 1; i <= level; i++) {
					levelsArray[i - 1] = "Level " + i;
				}
				return levelsArray;
			}

			QuerySnapshot querySnapshot = db.collection("users").whereEqualTo("email", emaila).get().get();
			if (querySnapshot.isEmpty()) {
				return new String[] { "No levels available" };
			}

			DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
			level = userDoc.getLong("level").intValue();

			String[] levelsArray = new String[level];
			for (int i = 1; i <= level; i++) {
				levelsArray[i - 1] = "Level " + i;
			}

			return levelsArray;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return new String[] { "Error retrieving levels" };
		}
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

	public String[] getRoutines(int selectedLevel, Boolean connect) throws InterruptedException, ExecutionException {

		List<String> workoutNames = new ArrayList<>();

		if (connect) {
			QuerySnapshot querySnapshot = db.collection("workouts").whereEqualTo("level", selectedLevel).get().get();

			if (querySnapshot.isEmpty())
				return new String[] { "Ez daude workout-ak maila honetarako" };

			for (DocumentSnapshot routineDoc : querySnapshot.getDocuments()) {
				String name = routineDoc.getString("name");
				if (name != null) {
					workoutNames.add(name);
				}
			}

			return workoutNames.toArray(new String[0]);
		} else {
			ReadBackup reader = new ReadBackup();
			ReadBackup.BackupData backup = reader.loadBackupData();

			if (backup != null) {
				List<ReadBackup.DocumentData> workoutDocs = backup.collections.get("workouts");
				if (workoutDocs != null) {
					for (ReadBackup.DocumentData d : workoutDocs) {
						String levelValue = d.fields.get("level");
						if (levelValue != null && levelValue.equals(String.valueOf(selectedLevel))) {
							String workoutName = d.fields.get("name");
							if (workoutName != null) {
								workoutNames.add(workoutName);
							}
						}
					}
				}
			}

			return workoutNames.toArray(new String[0]);
		}

	}

	public String[] getLevels(int nivelSeleccionado, String nivelText, Boolean connect)
			throws InterruptedException, ExecutionException {

		if (connect) {

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
		} else {
			ReadBackup reader = new ReadBackup();
			ReadBackup.BackupData backup = reader.loadBackupData();

			List<String> levels = new ArrayList<>();

			if (backup != null) {
				backup.collections.forEach((name, docs) -> {
					for (ReadBackup.DocumentData d : docs) {
						String levelValue = d.fields.get("level");
						String nameValue = d.fields.get("name");
						if (levelValue != null && levelValue.equals(String.valueOf(nivelSeleccionado))
								&& nameValue != null && nameValue.equals(nivelText)) {
							List<ReadBackup.DocumentData> exerciseDocs = d.subcollections.get("exercises");
							if (exerciseDocs != null) {
								for (ReadBackup.DocumentData exDoc : exerciseDocs) {
									String exerciseName = exDoc.fields.get("name");
									String exerciseDesc = exDoc.fields.get("description");
									String setsStr = exDoc.fields.get("sets");
									int sets = setsStr != null ? Integer.parseInt(setsStr) : 0;

									if (exerciseName != null && exerciseDesc != null) {
										levels.add(exerciseName + " – " + exerciseDesc + " (Total Sets: " + sets + ")");
									}
								}
							}
						}
					}
				});
			}

			if (levels.isEmpty()) {
				return new String[] { "Ez daude ariketarik workout honetan" };
			}

			return levels.toArray(new String[0]);
		}
	}

	public DefaultListModel<String> getListModel() {
		return listModel;
	}
}