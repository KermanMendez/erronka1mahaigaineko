package model;

import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.*;
import controller.Controller;
import util.ParseUtils;

public class ReadHistoric {

	private final Firestore db;
	private final ParseUtils parse = new ParseUtils();

	public ReadHistoric(Boolean connect) {
		this.db = Controller.getInstance().getDb();
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
			System.out.println("[INFO] Ez dago historikorik aurkitu rutina honetarako");
			return new String[] { "Ez dago historikorik rutina honetarako" };
		}

		Set<String> unique = new LinkedHashSet<>(resultList);
		System.out.println("[INFO] " + unique.size() + " historiko sarrera aurkituak");
		return unique.toArray(new String[0]);
	}

	private void addEntryIfMatch(List<String> list, Firestore db, DocumentSnapshot exerciseDoc, String rutinarenIzena)
			throws InterruptedException, ExecutionException {

		String exerciseCompleted = parse
				.booleanToEuskera(exerciseDoc.getBoolean("completed") != null && exerciseDoc.getBoolean("completed"));
		String exerciseDate = exerciseDoc.getString("date");
		int totalSets = ParseUtils.getIntValue(exerciseDoc.getLong("totalSets"));
		int totalTime = ParseUtils.getIntValue(exerciseDoc.getLong("totalTime"));
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

		list.add("Data: " + exerciseDate + " | Bukatuta: " + bukatutaWithPct + " | Serieak: " + totalSetsDisplay
				+ " | ⏱Denbora: " + totalTime + " seg");
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

				String exerciseCompleted = parse.booleanToEuskera(parse.parseBoolean(fields.get("completed")));
				String exerciseDate = fields.get("date");
				int totalSets = parse.parseIntFirstValid(fields.get("totalSets"), fields.get("totalReps"));
				int totalTime = ParseUtils.parseInt(fields.get("totalTime"));
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
										totalSetsInWorkout += ParseUtils.parseInt(exd.fields.get("sets"));
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

				result.add("Data: " + exerciseDate + " | Bukatuta: " + bukatutaWithPct + " | Serieak: "
						+ totalSetsDisplay + " | Denbora: " + totalTime + " seg");
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Errorea XML historikoa irakurtzerakoan");
		}
		return result;
	}

}
