package model;

import javax.swing.JTextField;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import controller.Controller;
import util.ErrorHandler;
import util.FirestoreUtils;
import util.ValidationUtils;

public class EditProfile {

	public boolean updateUserDocument(String email, String name, String surname1, String surname2, String birthdate) {
		try {
			Controller controller = Controller.getInstance();
			com.google.cloud.firestore.Firestore db = controller.getDb();
			if (db == null) {
				System.err.println("[ERROR] Firestore DB ez dago eskuragarri");
				return false;
			}

			FirestoreUtils firestoreUtils = new FirestoreUtils();
			com.google.cloud.firestore.DocumentSnapshot userDoc = firestoreUtils.getUserDocumentByEmail(db, email);
			if (userDoc == null) {
				System.err.println("[ERROR] Ez da erabiltzailea aurkitu email honekin: " + email);
				return false;
			}

			DocumentReference docRef = userDoc.getReference();

			java.util.Map<String, Object> updates = new java.util.HashMap<>();
			updates.put("name", name != null ? name : "");
			updates.put("surname", surname1 != null ? surname1 : "");
			updates.put("surname2", surname2 != null ? surname2 : "");
			if (birthdate != null)
				updates.put("birthdate", birthdate);

			docRef.update(updates).get();
			System.out.println("[INFO] Erabiltzailearen datuak eguneratuta");
			return true;
		} catch (Exception ex) {
			System.err.println("[ERROR] Errorea erabiltzailea eguneratzean: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	public boolean updatePasswordAuthAndSaveHash(String email, String newPassword) {
		if (newPassword == null || newPassword.isEmpty())
			return true;

		try {
			Controller controller = Controller.getInstance();
			com.google.cloud.firestore.Firestore db = controller.getDb();
			if (db == null) {
				System.err.println("[ERROR] Firestore DB ez dago eskuragarri");
				return false;
			}

			FirestoreUtils firestoreUtils = new FirestoreUtils();
			String uid = firestoreUtils.getUserIdByEmail(db, email);
			if (uid == null) {
				System.err.println("[ERROR] Ez da erabiltzailea aurkitu email honekin: " + email);
				return false;
			}

			UserRecord.UpdateRequest req = new com.google.firebase.auth.UserRecord.UpdateRequest(uid)
					.setPassword(newPassword);
			FirebaseAuth.getInstance().updateUser(req);

			String hashed = util.PasswordUtils.hashPasahitza(newPassword);

			com.google.cloud.firestore.DocumentSnapshot userDocForUpdate = firestoreUtils.getUserDocumentByEmail(db,
					email);
			if (userDocForUpdate != null) {
				userDocForUpdate.getReference().update(java.util.Map.of("password", hashed)).get();
			}

			System.out.println("[INFO] Pasahitza eguneratuta");
			return true;
		} catch (Exception ex) {
			System.err.println("[ERROR] Errorea pasahitza eguneratzean: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	public void loadProfileFromDb(JTextField tfName, JTextField tfSurname1, JTextField tfSurname2, JTextField tfDob) {
		new Thread(() -> {
			try {
				String email = CreateUserBackup.getCurrentUserEmail();
				if (email == null || email.trim().isEmpty()) {
					System.err.println("[ABISUA] Ez dago email-ik gordeta");
					return;
				}

				controller.Controller ctrl = controller.Controller.getInstance();
				Firestore db = ctrl.getDb();

				if (db == null) {
					System.err.println("[ERROR] Firestore DB ez dago eskuragarri. Ezin da profila kargatu.");
					return;
				}

				FirestoreUtils firestoreUtils = new FirestoreUtils();
				com.google.cloud.firestore.DocumentSnapshot userDoc = firestoreUtils.getUserDocumentByEmail(db, email);
				if (userDoc == null)
					return;
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
				System.err.println("[ERROR] Errorea profila kargatzerakoan");
			}
		}).start();
	}

	public void showMessage(Boolean dbOk, Boolean pwdOk, String name, String surname, String dob, Runnable onSuccess) {
		if (dbOk && pwdOk) {
			javax.swing.SwingUtilities.invokeLater(() -> {
				ErrorHandler.erakutsiInfo("Profila gordeta", "Zure profila ondo eguneratu da.\n\nIzena: " + name
						+ "\nAbizenak: " + surname + "\nJaiotze Data: " + dob);
				if (onSuccess != null) {
					onSuccess.run();
				}
			});
		} else {
			javax.swing.SwingUtilities.invokeLater(() -> {
				ErrorHandler.erakutsiErrorea("Errorea profila eguneratzean",
						"Ezin izan da profila eguneratu. Egiaztatu internet konexioa eta saiatu berriro.");
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

		// Balidatu izen-eremuak
		String errorea = ValidationUtils.balidatuBeteBeharrekoa(name, "Izena");
		if (errorea != null) {
			ErrorHandler.erakutsiErrorea("Balidazio errorea", errorea);
			return null;
		}

		errorea = ValidationUtils.balidatuBeteBeharrekoa(surname1, "Lehenengo abizena");
		if (errorea != null) {
			ErrorHandler.erakutsiErrorea("Balidazio errorea", errorea);
			return null;
		}

		errorea = ValidationUtils.balidatuBeteBeharrekoa(surname2, "Bigarren abizena");
		if (errorea != null) {
			ErrorHandler.erakutsiErrorea("Balidazio errorea", errorea);
			return null;
		}

		// Balidatu pasahitza aldatu nahi bada
		if (!pwd.isEmpty() || !pwd2.isEmpty()) {
			errorea = ValidationUtils.balidatuPasahitzakBerdinak(pwd, pwd2);
			if (errorea != null) {
				ErrorHandler.erakutsiErrorea("Pasahitza errorea", errorea);
				return null;
			}
		}

		User u = new User(name, surname1, surname2, pwd, dob);
		return u;
	}

	public void setLocalEmail(String localEmail) {
		if (localEmail == null || localEmail.trim().isEmpty()) {
			ErrorHandler.erakutsiErrorea("Errorea",
					"Ezin izan da zure email helbidea aurkitu. Saioa berriro hasi eta saiatu berriro.");
			return;
		}
	}

	public void updateProfileInDb(User userProfile, String targetEmail, Runnable onSuccess) {
		new Thread(() -> {
			boolean dbOk = updateUserDocument(targetEmail, userProfile.getName(), userProfile.getSurname(),
					userProfile.getSurname2(), userProfile.getDobString());
			boolean pwdOk = true;
			if (userProfile.getPassword() != null && !userProfile.getPassword().isEmpty()) {
				pwdOk = updatePasswordAuthAndSaveHash(targetEmail, userProfile.getPassword());
			}

			showMessage(dbOk, pwdOk, userProfile.getName(), userProfile.getFullSurname(), userProfile.getDobString(),
					onSuccess);

		}).start();
	}
}
