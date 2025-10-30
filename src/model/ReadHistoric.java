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

    public String[] getHistoric(int aukeratutakoMaila, String rutinarenIzena, Boolean connect)
            throws InterruptedException, ExecutionException {

        if (connect) {

            String email = new CreateUserBackup().loadEmail();

            QuerySnapshot querySnapshot = db.collection("users").whereEqualTo("email", email).get().get();

            if (querySnapshot.isEmpty()) {
                return new String[]{"Ez dituzu oraindik ariketak egin"};
            }

            List<String> levels = new ArrayList<>();

            for (DocumentSnapshot routineDoc : querySnapshot.getDocuments()) {
                List<QueryDocumentSnapshot> exerciseDocs = routineDoc.getReference().collection("historic")
                        .whereEqualTo("level", aukeratutakoMaila).get().get().getDocuments();

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
                        boolean matchesRoutine = true;
                        if (rutinarenIzena != null && !rutinarenIzena.trim().isEmpty()) {
                            matchesRoutine = workoutName.equalsIgnoreCase(rutinarenIzena.trim());
                        }
                        if (matchesRoutine) {
                            int totalSetsInWorkout = 0;
                            if (workoutId != null) {
                                List<QueryDocumentSnapshot> workoutExercises = db.collection("workouts")
                                        .document(workoutId).collection("exercises").get().get().getDocuments();
                                for (DocumentSnapshot wex : workoutExercises) {
                                    totalSetsInWorkout += new Exercise().setSets(wex.get("sets"));
                                }
                            }

                            double percent = 0.0;
                            if (totalSetsInWorkout > 0) {
                                percent = (totalSets * 100.0) / totalSetsInWorkout;
                                if (percent > 100.0) {
                                    percent = 100.0; // cap at 100
                                }
                            }

                            String pctStr = String.format("%.1f", percent).replace('.', ',');
                            String bukatutaWithPct = exerciseCompleted + " (" + pctStr + "% )";
                            String totalSetsDisplay = totalSets + " / " + totalSetsInWorkout;

                            levels.add("Data: " + exerciseDate + " | Bukatuta: " + bukatutaWithPct + " | Total Sets: "
                                    + totalSetsDisplay + " | Total Time: " + totalTime + " segundu");
                        }
                    }
                }
            }

            if (levels.isEmpty()) {
                return new String[]{"Ez daude historikorik workout honetan"};
            }

            return levels.toArray(new String[0]);
        }

        ReadBackup reader = new ReadBackup();
        ReadBackup.BackupData backup = reader.loadBackupData();
        if (backup == null) {
            return new String[]{"Ez dago konexiorik"};
        }

        String email = new CreateUserBackup().loadEmail();
        if (email == null) {
            return new String[]{"Ez dago konexiorik"};
        }

        String userId = null;
        if (backup.users != null) {
            for (ReadBackup.UserData u : backup.users) {
                if (u.email != null && u.email.equals(email)) {
                    userId = u.uid;
                    break;
                }
            }
        }
        if (userId == null) {
            return new String[]{"Ez dituzu oraindik ariketak egin"};
        }

        java.io.File histFile = new java.io.File("historic.xml");
        if (!histFile.exists() || histFile.length() == 0) {
            return new String[]{"Ez daude historikorik workout honetan"};
        }

        List<String> levelsOffline = new ArrayList<>();
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(histFile);
            doc.getDocumentElement().normalize();

            org.w3c.dom.NodeList users = doc.getElementsByTagName("user");
            for (int i = 0; i < users.getLength(); i++) {
                org.w3c.dom.Node userNode = users.item(i);
                if (userNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    continue;
                }
                org.w3c.dom.Element userElem = (org.w3c.dom.Element) userNode;
                String uidAttr = userElem.getAttribute("uid");
                if (!userId.equals(uidAttr)) {
                    continue;
                }

                java.util.Map<String, String> fields = new java.util.HashMap<>();
                org.w3c.dom.NodeList children = userElem.getChildNodes();
                for (int c = 0; c < children.getLength(); c++) {
                    org.w3c.dom.Node ch = children.item(c);
                    if (ch.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                        continue;
                    }
                    org.w3c.dom.Element fe = (org.w3c.dom.Element) ch;
                    fields.put(fe.getTagName(), fe.getTextContent());
                }

                String levelStr = fields.get("level");
                if (levelStr == null || !levelStr.equals(String.valueOf(aukeratutakoMaila))) {
                    continue;
                }

                String exerciseCompleted = "Ez";
                String compVal = fields.get("completed");
                if (compVal != null && (compVal.equalsIgnoreCase("true") || compVal.equalsIgnoreCase("bai")
                        || compVal.equalsIgnoreCase("yes"))) {
                    exerciseCompleted = "Bai";
                }

                String exerciseDate = fields.get("date");
                int totalSets = 0;
                if (fields.get("totalSets") != null) {
                    try {
                        totalSets = Integer.parseInt(fields.get("totalSets"));
                    } catch (NumberFormatException ignored) {
                    }
                } else if (fields.get("totalReps") != null) {
                    try {
                        totalSets = Integer.parseInt(fields.get("totalReps"));
                    } catch (NumberFormatException ignored) {
                    }
                }

                int totalTime = 0;
                if (fields.get("totalTime") != null) {
                    try {
                        totalTime = Integer.parseInt(fields.get("totalTime"));
                    } catch (NumberFormatException ignored) {
                    }
                }

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
                                        String setsStr = exd.fields.get("sets");
                                        if (setsStr != null) {
                                            try {
                                                totalSetsInWorkout += Integer.parseInt(setsStr);
                                            } catch (NumberFormatException ignored) {
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }

                double percent = 0.0;
                if (totalSetsInWorkout > 0) {
                    percent = (totalSets * 100.0) / totalSetsInWorkout;
                    if (percent > 100.0) {
                        percent = 100.0;
                    }
                }

                String pctStr = String.format("%.1f", percent).replace('.', ',');
                String bukatutaWithPct = exerciseCompleted + " (" + pctStr + "% )";
                String totalSetsDisplay = totalSets + " / " + totalSetsInWorkout;

                levelsOffline.add("Workout: " + workoutName + " | Data: " + exerciseDate + " | Bukatuta: "
                        + bukatutaWithPct + " | Total Sets: " + totalSetsDisplay + " | Total Time: " + totalTime
                        + " segundu");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"Ez daude historikorik workout honetan"};
        }

        java.io.File offlineFile = new java.io.File("offlineHistoric.xml");
        if (offlineFile.exists() && offlineFile.length() > 0) {
            try {
                javax.xml.parsers.DocumentBuilderFactory offFactory = javax.xml.parsers.DocumentBuilderFactory
                        .newInstance();
                javax.xml.parsers.DocumentBuilder offBuilder = offFactory.newDocumentBuilder();
                org.w3c.dom.Document offDoc = offBuilder.parse(offlineFile);
                offDoc.getDocumentElement().normalize();

                org.w3c.dom.NodeList offUsers = offDoc.getElementsByTagName("user");
                for (int i = 0; i < offUsers.getLength(); i++) {
                    org.w3c.dom.Node userNode = offUsers.item(i);
                    if (userNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                        continue;
                    }
                    org.w3c.dom.Element userElem = (org.w3c.dom.Element) userNode;
                    String uidAttr = userElem.getAttribute("uid");
                    String emailAttr = userElem.getAttribute("email");
                    if (uidAttr != null && uidAttr.equals(userId) || (emailAttr != null && emailAttr.equals(email))) {
                        java.util.Map<String, String> fields = new java.util.HashMap<>();
                        org.w3c.dom.NodeList children = userElem.getChildNodes();
                        for (int c = 0; c < children.getLength(); c++) {
                            org.w3c.dom.Node ch = children.item(c);
                            if (ch.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                                continue;
                            }
                            org.w3c.dom.Element fe = (org.w3c.dom.Element) ch;
                            fields.put(fe.getTagName(), fe.getTextContent());
                        }

                        String levelStr = fields.get("level");
                        if (levelStr == null || !levelStr.equals(String.valueOf(aukeratutakoMaila))) {
                            continue;
                        }

                        String exerciseCompleted = "Ez";
                        String compVal = fields.get("completed");
                        if (compVal != null && (compVal.equalsIgnoreCase("true") || compVal.equalsIgnoreCase("bai")
                                || compVal.equalsIgnoreCase("yes"))) {
                            exerciseCompleted = "Bai";
                        }

                        String exerciseDate = fields.get("date");
                        int totalSets = 0;
                        if (fields.get("totalSets") != null) {
                            try {
                                totalSets = Integer.parseInt(fields.get("totalSets"));
                            } catch (NumberFormatException ignored) {
                            }
                        } else if (fields.get("totalReps") != null) {
                            try {
                                totalSets = Integer.parseInt(fields.get("totalReps"));
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        int totalTime = 0;
                        if (fields.get("totalTime") != null) {
                            try {
                                totalTime = Integer.parseInt(fields.get("totalTime"));
                            } catch (NumberFormatException ignored) {
                            }
                        }

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
                                                String setsStr = exd.fields.get("sets");
                                                if (setsStr != null) {
                                                    try {
                                                        totalSetsInWorkout += Integer.parseInt(setsStr);
                                                    } catch (NumberFormatException ignored) {
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }

                            double percent = 0.0;
                            if (totalSetsInWorkout > 0) {
                                percent = (totalSets * 100.0) / totalSetsInWorkout;
                                if (percent > 100.0) {
                                    percent = 100.0;
                                }
                            }

                            String pctStr = String.format("%.1f", percent).replace('.', ',');
                            String bukatutaWithPct = exerciseCompleted + " (" + pctStr + "% )";
                            String totalSetsDisplay = totalSets + " / " + totalSetsInWorkout;

                            levelsOffline.add("Workout: " + workoutName + " | Data: " + exerciseDate + " | Bukatuta: "
                                    + bukatutaWithPct + " | Total Sets: " + totalSetsDisplay + " | Total Time: " + totalTime
                                    + " segundu");
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (levelsOffline.isEmpty()) {
            return new String[]{"Ez daude historikorik workout honetan"};
        }

        return levelsOffline.toArray(new String[0]);
    }
}