package model;

import controller.Controller;

public class EditProfile {

	public boolean updateUserDocument(String email, String name, String surname1, String surname2, String birthdate) {
		try {
			Controller controller = new Controller(true);
			com.google.cloud.firestore.Firestore db = controller.getDb();
			if (db == null)
				return false;

			com.google.cloud.firestore.QuerySnapshot query = db.collection("users").whereEqualTo("email", email).get()
					.get();
			if (query.isEmpty())
				return false;

			com.google.cloud.firestore.DocumentReference docRef = query.getDocuments().get(0).getReference();

			java.util.Map<String, Object> updates = new java.util.HashMap<>();
			updates.put("name", name != null ? name : "");
			updates.put("surname", surname1 != null ? surname1 : "");
			updates.put("surname2", surname2 != null ? surname2 : "");
			if (birthdate != null)
				updates.put("birthdate", birthdate);

			docRef.update(updates).get();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean updatePasswordAuthAndSaveHash(String email, String newPassword) {
		if (newPassword == null || newPassword.isEmpty())
			return true;

		try {
			Controller controller = new Controller(true);
			com.google.cloud.firestore.Firestore db = controller.getDb();
			if (db == null)
				return false;

			com.google.cloud.firestore.QuerySnapshot query = db.collection("users").whereEqualTo("email", email).get()
					.get();
			if (query.isEmpty())
				return false;

			com.google.cloud.firestore.DocumentSnapshot doc = query.getDocuments().get(0);
			String uid = doc.getId();

			com.google.firebase.auth.UserRecord.UpdateRequest req = new com.google.firebase.auth.UserRecord.UpdateRequest(
					uid).setPassword(newPassword);
			com.google.firebase.auth.FirebaseAuth.getInstance().updateUser(req);

			model.ConnectDB conn = new model.ConnectDB();
			String hashed = conn.hashPassword(newPassword);
			doc.getReference().update(java.util.Map.of("password", hashed)).get();

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

}
