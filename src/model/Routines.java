package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import controller.Controller;
import model.ReadBackup.BackupData;
import util.FirestoreUtils;
import util.ParseUtils;

public class Routines {

	private final Firestore db;
	private final Boolean connect;
	private final DefaultListModel<String> listModel = new DefaultListModel<>();
	private CreateUserBackup createUserBackup = new CreateUserBackup();
	private ReadBackup reader = new ReadBackup();
	private FirestoreUtils firestoreUtils = new FirestoreUtils();

	public Routines(Boolean connect) {
		this.db = Controller.getInstance().getDb();
		this.connect = connect;
	}

	public String[] levels() {

		String emaila = createUserBackup.loadEmail();
		int level = 1;

		try {
			if (connect == null || !connect || db == null) {
				ReadBackup.BackupData backup = reader.loadBackupData();
				if (backup == null) {
					System.err.println("[ERROR] Ezin izan da backup-a kargatu");
					return new String[] { "Ez dago mailarik eskuragarri" };
				}

				level = firestoreUtils.getUserLevelFromBackup(backup, emaila);

				String[] levelsArray = new String[level];
				for (int i = 1; i <= level; i++) {
					levelsArray[i - 1] = i + ". Maila";
				}
				return levelsArray;
			}

			QuerySnapshot querySnapshot = db.collection("users").whereEqualTo("email", emaila).get().get();
			if (querySnapshot.isEmpty()) {
				System.err.println("[ERROR] Ez da erabiltzailea aurkitu: " + emaila);
				return new String[] { "Ez dago mailarik eskuragarri" };
			}

			DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);
			level = userDoc.getLong("level").intValue();

			String[] levelsArray = new String[level];
			for (int i = 1; i <= level; i++) {
				levelsArray[i - 1] = i + ". Maila";
			}

			return levelsArray;
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("[ERROR] Errorea mailak eskuratzerakoan");
			return new String[] { "Errorea mailak kargatzerakoan" };
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
						listModel.addElement("Ez dago ariketarik maila honetarako");
						System.err.println("[ABISUA] Ez dago ariketarik maila " + maila + "rako");
						return;
					}

					exercises.forEach(exercise -> {
						System.out.println("[INFO] Ariketa: " + exercise.getName() + " - Serieak: " + exercise.getSets()
								+ ", Errepikazioak: " + exercise.getReps());
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

			if (querySnapshot.isEmpty()) {
				System.err.println("[ABISUA] Ez dago rutinik " + selectedLevel + ". mailarako");
				return new String[] { "Ez dago rutinik maila honetarako" };
			}

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
			BackupData backup = reader.loadBackupData();

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
									int sets = ParseUtils.parseInt(exDoc.fields.get("sets"));
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

	/**
	 * Actualiza el comboBox de rutinas cuando cambia el nivel
	 * 
	 * @param isHistoric true para ViewHistoric, false para Workouts
	 */
	public static void updateRoutinesComboBox(JComboBox<String> comboMaila, JComboBox<String> comboMailaRutinakLevel,
			Routines routines, Boolean connect, JList<String> listaWorkout, boolean isHistoric) {

		int aukeratutakoMaila = comboMaila.getSelectedIndex() + 1;

		new Thread(() -> {
			try {
				String[] routinesForLevel = routines.getRoutines(aukeratutakoMaila, connect);
				final String[] chosenRoutine = new String[1];

				SwingUtilities.invokeLater(() -> {
					comboMailaRutinakLevel.setModel(new DefaultComboBoxModel<>(routinesForLevel));
					if (routinesForLevel != null && routinesForLevel.length > 0) {
						comboMailaRutinakLevel.setSelectedIndex(0);
						chosenRoutine[0] = routinesForLevel[0];
					} else {
						chosenRoutine[0] = "";
					}
				});

				Thread.sleep(50);

				String rutinarenIzenaToUse = chosenRoutine[0] != null && !chosenRoutine[0].isEmpty() ? chosenRoutine[0]
						: (comboMailaRutinakLevel.getItemCount() > 0 ? comboMailaRutinakLevel.getItemAt(0) : "");

				// Cargar lista según el tipo de vista
				updateList(aukeratutakoMaila, rutinarenIzenaToUse, connect, listaWorkout, isHistoric);

			} catch (InterruptedException | ExecutionException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	/**
	 * Actualiza la lista cuando cambia la rutina seleccionada
	 * 
	 * @param isHistoric true para ViewHistoric, false para Workouts
	 */
	public static void updateWorkoutList(JComboBox<String> comboMaila, JComboBox<String> comboMailaRutinakLevel,
			Boolean connect, JList<String> listaWorkout, boolean isHistoric) {

		int aukeratutakoMaila = comboMaila.getSelectedIndex() + 1;
		String rutinarenIzena = comboMailaRutinakLevel.getSelectedItem() != null
				? comboMailaRutinakLevel.getSelectedItem().toString()
				: "";

		new Thread(() -> {
			try {
				updateList(aukeratutakoMaila, rutinarenIzena, connect, listaWorkout, isHistoric);
			} catch (InterruptedException | ExecutionException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	/**
	 * Método común para actualizar la lista (ejercicios o histórico)
	 */
	private static void updateList(int nivel, String rutinaNombre, Boolean connect, JList<String> listaWorkout,
			boolean isHistoric) throws InterruptedException, ExecutionException {

		String[] datos;

		if (isHistoric) {
			// Para ViewHistoric: cargar histórico
			ReadHistoric readHistoric = new ReadHistoric(connect);
			datos = readHistoric.getHistoric(nivel, rutinaNombre, connect);
		} else {
			// Para Workouts: cargar ejercicios
			Routines routines = new Routines(connect);
			datos = routines.getLevels(nivel, rutinaNombre, connect);
		}

		SwingUtilities.invokeLater(() -> {
			listaWorkout.setModel(new AbstractListModel<String>() {
				private static final long serialVersionUID = 1L;
				String[] balioak = datos;

				public int getSize() {
					return balioak.length;
				}

				public String getElementAt(int index) {
					return balioak[index];
				}
			});
		});
	}

}