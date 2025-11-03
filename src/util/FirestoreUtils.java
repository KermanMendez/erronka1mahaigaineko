package util;

import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import model.ReadBackup;

/**
 * Utilidad para operaciones comunes de Firestore
 */
public class FirestoreUtils {
	
	/**
	 * Obtiene el documento de usuario por email (online)
	 * @return DocumentSnapshot o null si no se encuentra
	 */
	public static DocumentSnapshot getUserDocumentByEmail(Firestore db, String email) 
			throws InterruptedException, ExecutionException {
		if (db == null || email == null || email.trim().isEmpty()) {
			return null;
		}
		
		QuerySnapshot querySnapshot = db.collection("users")
				.whereEqualTo("email", email)
				.get()
				.get();
		
		if (querySnapshot.isEmpty()) {
			return null;
		}
		
		return querySnapshot.getDocuments().get(0);
	}
	
	/**
	 * Obtiene el UID del usuario por email (online)
	 * @return UID o null si no se encuentra
	 */
	public static String getUserIdByEmail(Firestore db, String email) 
			throws InterruptedException, ExecutionException {
		DocumentSnapshot doc = getUserDocumentByEmail(db, email);
		return doc != null ? doc.getId() : null;
	}
	
	/**
	 * Obtiene el UID del usuario por email desde backup (offline)
	 * @return UID o null si no se encuentra
	 */
	public static String getUserIdFromBackup(ReadBackup.BackupData backup, String email) {
		if (backup == null || backup.users == null || email == null) {
			return null;
		}
		
		for (ReadBackup.UserData u : backup.users) {
			if (email.equals(u.email)) {
				return u.uid;
			}
		}
		
		return null;
	}
	
	/**
	 * Obtiene el nivel del usuario desde backup (offline)
	 * @return nivel o 1 por defecto
	 */
	public static int getUserLevelFromBackup(ReadBackup.BackupData backup, String email) {
		if (backup == null || backup.collections == null || email == null) {
			return 1;
		}
		
		var users = backup.collections.get("users");
		if (users == null) {
			return 1;
		}
		
		for (ReadBackup.DocumentData ud : users) {
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
