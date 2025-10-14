package model;

import org.mindrot.jbcrypt.BCrypt;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

public class CreateUser {
	
	public boolean createUser(String username, String email, String plainPassword) throws Exception {
		Firestore db = FirestoreClient.getFirestore();
		CollectionReference usersCol = db.collection("users");

		if (db.collection("users").document(username).get().get().exists()) {
			return false;
		}

		String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

		ApiFuture<WriteResult> writeUser = usersCol.document(username).set(new java.util.HashMap<>() {
			{
				put("username", username);
				put("email", email);
				put("password", hashedPassword);
			}
		});
		return true;
	}
}
