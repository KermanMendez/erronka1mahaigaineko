package model;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

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

	public void loadProfileFromDb(JTextField tfName, JTextField tfSurname1, JTextField tfSurname2, JTextField tfDob) {
		new Thread(() -> {
			try {
				String email = new CreateUserBackup().loadEmail();
				if (email == null || email.trim().isEmpty()) {
					return;
				}

				controller.Controller controller = new controller.Controller(true);
				com.google.cloud.firestore.Firestore db = controller.getDb();
				if (db == null)
					return;

				com.google.cloud.firestore.QuerySnapshot query = db.collection("users").whereEqualTo("email", email)
						.get().get();
				if (query.isEmpty())
					return;

				com.google.cloud.firestore.DocumentSnapshot userDoc = query.getDocuments().get(0);
				String name = userDoc.getString("name");
				String surname = userDoc.getString("surname");
				String surname2 = userDoc.getString("surname2");
				String birth = userDoc.getString("birthdate");
				if (birth == null)
					birth = userDoc.getString("birthdate");

				final String fName = name != null ? name : "";
				String fSurname1 = "";
				String fSurname2 = "";
				if (surname != null) {
					if (surname2 != null && !surname2.isEmpty()) {
						fSurname1 = surname;
						fSurname2 = surname2;
					} else {
						String[] parts = surname.trim().split("\\s+", 2);
						fSurname1 = parts.length > 0 ? parts[0] : "";
						fSurname2 = parts.length > 1 ? parts[1] : "";
					}
				}
				final String ffSurname1 = fSurname1;
				final String ffSurname2 = fSurname2;
				final String fBirth = birth != null ? birth : "";

				javax.swing.SwingUtilities.invokeLater(() -> {
					tfName.setText(fName);
					tfSurname1.setText(ffSurname1);
					tfSurname2.setText(ffSurname2);
					tfDob.setText(fBirth);
				});

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	public void showMessage(Boolean dbOk, Boolean pwdOk, String name, String surname, String dob, Runnable onSuccess) {
		if (dbOk && pwdOk) {
			javax.swing.SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(null, "Profila ondo eguneratuta.\nIzena: " + name + "\nAbizenak: "
						+ surname + "\nJaiotze Data: " + dob, "Gordeta", JOptionPane.INFORMATION_MESSAGE);
				if (onSuccess != null) {
					onSuccess.run();
				}
			});
		} else {
			javax.swing.SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(null,
						"Errorea profila eguneratzean. Konexioa konprobatu eta berriro zailatu", "Error",
						JOptionPane.ERROR_MESSAGE);
			});
		}
		return;
	}

	public User validateChanges(JTextField tfName, JTextField tfSurname1, JTextField tfSurname2,
			javax.swing.JPasswordField pfPassword, javax.swing.JPasswordField pfPassword2, JTextField finalTfDob) {

		String name = tfName.getText().trim();
		String surname1 = tfSurname1.getText().trim();
		String surname2 = tfSurname2.getText().trim();
		String pwd = new String(pfPassword.getPassword());
		String pwd2 = new String(pfPassword2.getPassword());
		String dob = finalTfDob.getText().trim();

		if (!pwd.isEmpty() || !pwd2.isEmpty()) {
			if (!pwd.equals(pwd2)) {
				JOptionPane.showMessageDialog(null, "Pasahitzak ez dira berdinak.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			if (pwd.length() < 6) {
				JOptionPane.showMessageDialog(null, "Pasahitza gutxienez 6 karaktere izan behar ditu", "Error",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}

		User u = new User(name, surname1, surname2, pwd, dob);
		return u;
	}

	public void setLocalEmail(String localEmail) {
		if (localEmail == null || localEmail.trim().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Ez da emaila aurkitu. Saioa hasi eta berriro zailatu", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}
