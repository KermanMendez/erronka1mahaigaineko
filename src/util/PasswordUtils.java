package util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Pasahitzen kudeaketa seguruaren utilitateak
 * Utilidades para el manejo seguro de contraseñas
 */
public class PasswordUtils {

	private static final int ITERAZIOAK = 65536;
	private static final int GAKO_LUZERA = 256;
	private static final int GATZ_LUZERA = 16;
	private static final String ALGORITMOA = "PBKDF2WithHmacSHA256";

	/**
	 * PBKDF2 erabiliz pasahitz batek hash segurua sortzen du
	 * Genera un hash seguro de la contraseña usando PBKDF2
	 * 
	 * @param pasahitza Testu arrunteko pasahitza
	 * @return "gatz$hash" formatuko hash-a (Base64)
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static String hashPasahitza(String pasahitza) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (pasahitza == null || pasahitza.isEmpty()) {
			throw new IllegalArgumentException("Pasahitza ezin da null edo hutsik egon");
		}

		// Ausazko gatza sortu
		byte[] gatza = sortuGatza();

		// Hash-a sortu
		byte[] hash = sortuHash(pasahitza, gatza);

		// Gatz$hash itzuli Base64-n
		return Base64.getEncoder().encodeToString(gatza) + "$" + Base64.getEncoder().encodeToString(hash);
	}

	/**
	 * Pasahitz bat bere hash-arekin bat datorrean egiaztatzen du
	 * Verifica si una contraseña coincide con su hash
	 * 
	 * @param pasahitza        Testu arrunteko pasahitza
	 * @param gordetakoPasahitza "gatz$hash" formatuan gordetako hash-a
	 * @return true pasahitza zuzena bada
	 */
	public static boolean egiaztaturPasahitza(String pasahitza, String gordetakoPasahitza) {
		if (pasahitza == null || gordetakoPasahitza == null || !gordetakoPasahitza.contains("$")) {
			return false;
		}

		try {
			// Gatza eta hash-a banatu
			String[] zatiak = gordetakoPasahitza.split("\\$");
			if (zatiak.length != 2) {
				return false;
			}

			byte[] gatza = Base64.getDecoder().decode(zatiak[0]);
			byte[] gordetakoHash = Base64.getDecoder().decode(zatiak[1]);

			// Emandako pasahitzarekin hash-a sortu
			byte[] saiakeraHash = sortuHash(pasahitza, gatza);

			// Denbora konstantean konparatu timing attacks saihesteko
			return denboraKonstanteanBerdinak(gordetakoHash, saiakeraHash);

		} catch (Exception e) {
			System.err.println("[ERROR] Errorea pasahitza egiaztatzerakoan: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Ausazko gatz bat sortzen du
	 * Genera un salt aleatorio
	 * 
	 * @return gatza duen byte array-a
	 */
	private static byte[] sortuGatza() {
		SecureRandom ausazkoa = new SecureRandom();
		byte[] gatza = new byte[GATZ_LUZERA];
		ausazkoa.nextBytes(gatza);
		return gatza;
	}

	/**
	 * Emandako gatzarekin pasahitzaren PBKDF2 hash-a sortzen du
	 * Genera el hash PBKDF2 de una contraseña con el salt proporcionado
	 * 
	 * @param pasahitza Pasahitza
	 * @param gatza     Gatza
	 * @return Sortutako hash-a
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private static byte[] sortuHash(String pasahitza, byte[] gatza)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		PBEKeySpec spec = new PBEKeySpec(pasahitza.toCharArray(), gatza, ITERAZIOAK, GAKO_LUZERA);
		SecretKeyFactory fabrika = SecretKeyFactory.getInstance(ALGORITMOA);
		return fabrika.generateSecret(spec).getEncoded();
	}

	/**
	 * Array-en konparazioa denbora konstantean timing attacks saihesteko
	 * Comparación de arrays en tiempo constante para prevenir timing attacks
	 * 
	 * @param a Lehenengo array-a
	 * @param b Bigarren array-a
	 * @return true berdinak badira
	 */
	private static boolean denboraKonstanteanBerdinak(byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}

		int emaitza = 0;
		for (int i = 0; i < a.length; i++) {
			emaitza |= a[i] ^ b[i];
		}
		return emaitza == 0;
	}

	// Atzeraeragingarritasuna: Metodo zaharrak mantendu
	// Mantener métodos antiguos para retrocompatibilidad

	/**
	 * @deprecated Erabili hashPasahitza() horren ordez
	 */
	@Deprecated
	public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return hashPasahitza(password);
	}

	/**
	 * @deprecated Erabili egiaztaturPasahitza() horren ordez
	 */
	@Deprecated
	public static boolean verifyPassword(String password, String storedPassword) {
		return egiaztaturPasahitza(password, storedPassword);
	}
}
