package util;

import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import service.BackupReaderService.BackupData;
import service.BackupReaderService.DocumentData;
import service.BackupReaderService.UserData;

public class FirestoreUtils {

	public DocumentSnapshot getUserDocumentByEmail(Firestore db, String email)
			throws InterruptedException, ExecutionException {
		if (db == null || email == null || email.trim().isEmpty()) {
			return null;
		}

		QuerySnapshot querySnapshot = db.collection("users").whereEqualTo("email", email).get().get();

		if (querySnapshot.isEmpty()) {
			return null;
		}

		return querySnapshot.getDocuments().get(0);
	}

	public String getUserIdByEmail(Firestore db, String email) throws InterruptedException, ExecutionException {
		DocumentSnapshot doc = getUserDocumentByEmail(db, email);
		return doc != null ? doc.getId() : null;
	}

	public String getUserIdFromBackup(BackupData backup, String email) {
		if (backup == null || backup.users == null || email == null) {
			return null;
		}

		for (UserData u : backup.users) {
			if (email.equals(u.email)) {
				return u.uid;
			}
		}

		return null;
	}

	public int getUserLevelFromBackup(BackupData backup, String email) {
		if (backup == null || backup.collections == null || email == null) {
			return 1;
		}

		var users = backup.collections.get("users");
		if (users == null) {
			return 1;
		}

		for (DocumentData ud : users) {
			String userEmail = ud.fields.get("email");
			if (userEmail != null && userEmail.equals(email)) {
				String levelStr = ud.fields.get("level");
				if (levelStr != null) {
					return ParseUtils.parseInt(levelStr);
				}
				break;
			}
		}

		return 1;
	}
}
