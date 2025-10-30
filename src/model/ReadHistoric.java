package model;

import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.*;
import controller.Controller;

public class ReadHistoric {

	private final Firestore db;

	public ReadHistoric(Boolean connect) {
		this.db = new Controller(connect).getDb();
	}

	public String[] getHistoric(int aukeratutakoMaila, String rutinarenIzena, Boolean connect)
			throws InterruptedException, ExecutionException {

		List<String> resultList = new ArrayList<>();

		if (connect) {
			String email = new CreateUserBackup().loadEmail();
			QuerySnapshot querySnapshot = db.collection("users").whereEqualTo("email", email).get().get();

			if (!querySnapshot.isEmpty()) {
				for (DocumentSnapshot routineDoc : querySnapshot.getDocuments()) {
					List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("historic")
							.whereEqualTo("level", aukeratutakoMaila).get().get().getDocuments();

					for (DocumentSnapshot exerciseDoc : exerciseDocs) {
						addEntryIfMatch(resultList, db, exerciseDoc, rutinarenIzena);
					}
				}
			}
		}

		ReadBackup reader = new ReadBackup();
		ReadBackup.BackupData backup = reader.loadBackupData();
		String email = new CreateUserBackup().loadEmail();

		if (backup != null && email != null) {
			String userId = null;
			if (backup.users != null) {
				for (ReadBackup.UserData u : backup.users) {
					if (email.equals(u.email)) {
						userId = u.uid;
						break;
					}
				}
			}

			resultList.addAll(readOfflineXml("historic.xml", userId, email, backup, aukeratutakoMaila, rutinarenIzena));
			resultList.addAll(
					readOfflineXml("offlineHistoric.xml", userId, email, backup, aukeratutakoMaila, rutinarenIzena));
		}

		if (resultList.isEmpty()) {
			return new String[] { "Ez daude historikorik workout honetan" };
		}

		Set<String> unique = new LinkedHashSet<>(resultList);
		return unique.toArray(new String[0]);
	}

	private void addEntryIfMatch(List<String> list, Firestore db, DocumentSnapshot exerciseDoc, String rutinarenIzena)
			throws InterruptedException, ExecutionException {

		String exerciseCompleted = exerciseDoc.getBoolean("completed") != null && exerciseDoc.getBoolean("completed")
				? "Bai"
				: "Ez";
		String exerciseDate = exerciseDoc.getString("date");
		int totalSets = getIntValue(exerciseDoc.getLong("totalSets"));
		int totalTime = getIntValue(exerciseDoc.getLong("totalTime"));
		String workoutId = exerciseDoc.getString("workoutId");
		String workoutName = workoutId;

		if (workoutId != null) {
			DocumentSnapshot workoutDoc = db.collection("workouts").document(workoutId).get().get();
			if (workoutDoc.exists() && workoutDoc.getString("name") != null) {
				workoutName = workoutDoc.getString("name");
			}
		}

		if (workoutName == null || exerciseDate == null)
			return;

		if (rutinarenIzena != null && !rutinarenIzena.trim().isEmpty()) {
			String sel = rutinarenIzena.trim().toLowerCase();
			String wname = workoutName.trim().toLowerCase();
			if (!(wname.equals(sel) || wname.contains(sel) || sel.contains(wname)))
				return;
		}

		int totalSetsInWorkout = 0;
		if (workoutId != null) {
			List<QueryDocumentSnapshot> workoutExercises = db.collection("workouts").document(workoutId)
					.collection("exercises").get().get().getDocuments();
			for (DocumentSnapshot wex : workoutExercises) {
				totalSetsInWorkout += new Exercise().setSets(wex.get("sets"));
			}
		}

		double percent = (totalSetsInWorkout > 0) ? (totalSets * 100.0) / totalSetsInWorkout : 0.0;
		if (percent > 100.0)
			percent = 100.0;

		String pctStr = String.format("%.1f", percent).replace('.', ',');
		String bukatutaWithPct = exerciseCompleted + " (" + pctStr + "%)";
		String totalSetsDisplay = totalSets + " / " + totalSetsInWorkout;

		list.add("Data: " + exerciseDate + " | Bukatuta: " + bukatutaWithPct + " | Total Sets: " + totalSetsDisplay
				+ " | Total Time: " + totalTime + " segundu");
	}

	private List<String> readOfflineXml(String fileName, String userId, String email, ReadBackup.BackupData backup,
			int level, String rutinarenIzena) {

		List<String> result = new ArrayList<>();
		java.io.File file = new java.io.File(fileName);
		if (!file.exists() || file.length() == 0)
			return result;

		try {
			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();

			org.w3c.dom.NodeList users = doc.getElementsByTagName("user");
			for (int i = 0; i < users.getLength(); i++) {
				org.w3c.dom.Element userElem = (org.w3c.dom.Element) users.item(i);
				String uidAttr = userElem.getAttribute("uid");
				String emailAttr = userElem.getAttribute("email");

				boolean validUser = (uidAttr != null && uidAttr.equals(userId))
						|| (emailAttr != null && emailAttr.equals(email));
				if (!validUser)
					continue;

				Map<String, String> fields = new HashMap<>();
				org.w3c.dom.NodeList children = userElem.getChildNodes();
				for (int c = 0; c < children.getLength(); c++) {
					org.w3c.dom.Node ch = children.item(c);
					if (ch.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
						org.w3c.dom.Element fe = (org.w3c.dom.Element) ch;
						fields.put(fe.getTagName(), fe.getTextContent());
					}
				}

				if (!String.valueOf(level).equals(fields.get("level")))
					continue;

				String exerciseCompleted = parseCompleted(fields.get("completed"));
				String exerciseDate = fields.get("date");
				int totalSets = parseInt(fields.get("totalSets"), fields.get("totalReps"));
				int totalTime = parseInt(fields.get("totalTime"));
				String workoutId = fields.get("workoutId");
				String workoutName = workoutId;

				int totalSetsInWorkout = 0;
				if (backup.collections != null && workoutId != null) {
					List<ReadBackup.DocumentData> workoutDocs = backup.collections.get("workouts");
					if (workoutDocs != null) {
						for (ReadBackup.DocumentData wd : workoutDocs) {
							if (workoutId.equals(wd.id)) {
								if (wd.fields.get("name") != null) {
									workoutName = wd.fields.get("name");
								}
								List<ReadBackup.DocumentData> exerciseDocs = wd.subcollections.get("exercises");
								if (exerciseDocs != null) {
									for (ReadBackup.DocumentData exd : exerciseDocs) {
										totalSetsInWorkout += parseInt(exd.fields.get("sets"));
									}
								}
								break;
							}
						}
					}
				}

				if (rutinarenIzena != null && !rutinarenIzena.trim().isEmpty()) {
					String sel = rutinarenIzena.trim().toLowerCase();
					String wname = workoutName != null ? workoutName.trim().toLowerCase() : "";
					if (!(wname.equals(sel) || wname.contains(sel) || sel.contains(wname)))
						continue;
				}

				double percent = (totalSetsInWorkout > 0) ? (totalSets * 100.0) / totalSetsInWorkout : 0.0;
				if (percent > 100.0)
					percent = 100.0;

				String pctStr = String.format("%.1f", percent).replace('.', ',');
				String bukatutaWithPct = exerciseCompleted + " (" + pctStr + "%)";
				String totalSetsDisplay = totalSets + " / " + totalSetsInWorkout;

				result.add("Data: " + exerciseDate + " | Bukatuta: " + bukatutaWithPct + " | Total Sets: "
						+ totalSetsDisplay + " | Total Time: " + totalTime + " segundu");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static int getIntValue(Long val) {
		return val != null ? val.intValue() : 0;
	}

	private static int parseInt(String... vals) {
		for (String v : vals) {
			if (v != null) {
				try {
					return Integer.parseInt(v.trim());
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return 0;
	}

	private static String parseCompleted(String val) {
		if (val == null)
			return "Ez";
		val = val.trim().toLowerCase();
		return (val.equals("true") || val.equals("bai") || val.equals("yes")) ? "Bai" : "Ez";
	}
}
