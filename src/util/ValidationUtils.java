package util;

import java.util.regex.Pattern;

/**
 * Datuak balidatzeko utilitate klasea
 */
public class ValidationUtils {

	// Email patroiak
	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	// Data patroiak
	private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");

	/**
	 * Email helbidea baliozkoa den egiaztatu
	 * @param email Egiaztatzeko email-a
	 * @return true baliozkoa bada
	 */
	public static boolean emailBaliozkoa(String email) {
		if (email == null || email.trim().isEmpty()) {
			return false;
		}
		return EMAIL_PATTERN.matcher(email.trim()).matches();
	}

	/**
	 * Pasahitza gutxieneko baldintzak betetzen dituen egiaztatu
	 * @param pasahitza Egiaztatzeko pasahitza
	 * @return true baliozkoa bada (gutxienez 6 karaktere)
	 */
	public static boolean pasahitzaBaliozkoa(String pasahitza) {
		return pasahitza != null && pasahitza.length() >= 6;
	}

	/**
	 * Bi pasahitzak berdinak diren egiaztatu
	 * @param pasahitza1 Lehenengo pasahitza
	 * @param pasahitza2 Bigarren pasahitza
	 * @return true berdinak badira
	 */
	public static boolean pasahitzakBerdinak(String pasahitza1, String pasahitza2) {
		if (pasahitza1 == null || pasahitza2 == null) {
			return false;
		}
		return pasahitza1.equals(pasahitza2);
	}

	/**
	 * Testua hutsik dagoen edo zuriuneak bakarrik dituen egiaztatu
	 * @param testua Egiaztatzeko testua
	 * @return true hutsik badago
	 */
	public static boolean testuaHutsik(String testua) {
		return testua == null || testua.trim().isEmpty();
	}

	/**
	 * Data formatua zuzena den egiaztatu (dd/MM/yyyy)
	 * @param data Egiaztatzeko data
	 * @return true formatua zuzena bada
	 */
	public static boolean dataFormatuaZuzena(String data) {
		if (data == null || data.trim().isEmpty()) {
			return false;
		}
		return DATE_PATTERN.matcher(data.trim()).matches();
	}

	/**
	 * Eremu bete bat balidatu
	 * @param balioa Balidatzeko balioa
	 * @param eremuIzena Eremuaren izena (errore mezuetarako)
	 * @return Errore mezua, edo null baliozkoa bada
	 */
	public static String balidatuBeteBeharrekoa(String balioa, String eremuIzena) {
		if (testuaHutsik(balioa)) {
			return eremuIzena + " beteta egon behar du.";
		}
		return null;
	}

	/**
	 * Email bat balidatu
	 * @param email Balidatzeko email-a
	 * @return Errore mezua, edo null baliozkoa bada
	 */
	public static String balidatuEmail(String email) {
		if (testuaHutsik(email)) {
			return "Email-a beteta egon behar du.";
		}
		if (!emailBaliozkoa(email)) {
			return "Email formatua ez da zuzena.";
		}
		return null;
	}

	/**
	 * Pasahitza bat balidatu
	 * @param pasahitza Balidatzeko pasahitza
	 * @return Errore mezua, edo null baliozkoa bada
	 */
	public static String balidatuPasahitza(String pasahitza) {
		if (testuaHutsik(pasahitza)) {
			return "Pasahitza beteta egon behar du.";
		}
		if (!pasahitzaBaliozkoa(pasahitza)) {
			return "Pasahitzak gutxienez 6 karaktere izan behar ditu.";
		}
		return null;
	}

	/**
	 * Bi pasahitza balidatu berdinak direla
	 * @param pasahitza1 Lehenengo pasahitza
	 * @param pasahitza2 Bigarren pasahitza
	 * @return Errore mezua, edo null baliozkoa bada
	 */
	public static String balidatuPasahitzakBerdinak(String pasahitza1, String pasahitza2) {
		String errorea = balidatuPasahitza(pasahitza1);
		if (errorea != null) {
			return errorea;
		}

		if (!pasahitzakBerdinak(pasahitza1, pasahitza2)) {
			return "Pasahitzak ez dira berdinak.";
		}
		return null;
	}

	/**
	 * Erregistro osoa balidatu (izen osoa, email, pasahitzak, data)
	 * @param izena Izena
	 * @param abizena1 Lehenengo abizena
	 * @param abizena2 Bigarren abizena
	 * @param email Email-a
	 * @param pasahitza1 Pasahitza
	 * @param pasahitza2 Pasahitza (errepikatu)
	 * @param jaiotzeData Jaiotze data
	 * @return Errore mezua, edo null guztia zuzena bada
	 */
	public static String balidatuErregistroa(String izena, String abizena1, String abizena2,
			String email, String pasahitza1, String pasahitza2, Object jaiotzeData) {
		
		String errorea = balidatuBeteBeharrekoa(izena, "Izena");
		if (errorea != null) return errorea;

		errorea = balidatuBeteBeharrekoa(abizena1, "Lehenengo abizena");
		if (errorea != null) return errorea;

		errorea = balidatuBeteBeharrekoa(abizena2, "Bigarren abizena");
		if (errorea != null) return errorea;

		errorea = balidatuEmail(email);
		if (errorea != null) return errorea;

		errorea = balidatuPasahitzakBerdinak(pasahitza1, pasahitza2);
		if (errorea != null) return errorea;

		if (jaiotzeData == null) {
			return "Jaiotze data hautatu behar duzu.";
		}

		return null;
	}
}
