package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import util.CryptoUtils;

public class CreateUserBackup {

	private final String FICHERO = "user.dat";
	private CryptoUtils cryptoUtils = new CryptoUtils();

	/**
	 * Uneko erabiltzailearen emaila modu estatikoan lortzen du
	 * Obtiene el email del usuario actual de forma estática
	 * 
	 * Metodo laguntzailea instantziak sortu gabe
	 * Método helper para evitar crear instancias repetidas
	 * 
	 * @return Uneko erabiltzailearen emaila edo null ez badago / Email del usuario actual o null si no existe
	 */
	public static String getCurrentUserEmail() {
		return new CreateUserBackup().loadEmail();
	}

	public void saveEmail(String email) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(email);
		}
		byte[] encryptedData = cryptoUtils.xorBytes(baos.toByteArray());

		try (FileOutputStream fos = new FileOutputStream(FICHERO)) {
			fos.write(encryptedData);
		}
	}

	public String loadEmail() {
		File file = new File(FICHERO);
		if (!file.exists() || file.length() == 0) {
			return null;
		}

		try {
			byte[] fileBytes = Files.readAllBytes(Paths.get(FICHERO));
			byte[] decryptedData = cryptoUtils.xorBytes(fileBytes);
			try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decryptedData))) {
				Object obj = ois.readObject();
				if (obj instanceof String) {
					return (String) obj;
				} else {
					System.err.println("Invalid data format in user.dat");
					return null;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}