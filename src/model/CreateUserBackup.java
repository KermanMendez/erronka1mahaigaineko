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

public class CreateUserBackup {

	private final String FICHERO = "user.dat";
	private final byte CLAVE = 0x5A;

	private byte[] xorBytes(byte[] data) {
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (byte) (data[i] ^ CLAVE);
		}
		return result;
	}

	public void saveEmail(String email) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(email);
		}
		byte[] encryptedData = xorBytes(baos.toByteArray());

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
			byte[] decryptedData = xorBytes(fileBytes);
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