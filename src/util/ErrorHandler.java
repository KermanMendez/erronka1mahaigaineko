package util;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Erroreen kudeaketa zentralizatua euskerazko mezuekin
 * Centralizador de gestión de errores con mensajes en euskera
 */
public class ErrorHandler {

	// Errore motak / Tipos de error
	public enum ErrorMota {
		KONEXIO_ERROREA,
		AUTENTIFIKAZIO_ERROREA,
		DATU_ERROREA,
		VALIDAZIO_ERROREA,
		SISTEMA_ERROREA,
		SINKRONIZAZIO_ERROREA,
		EZEZAGUNA
	}

	/**
	 * Errore mezua erakutsi erabiltzaileari
	 * @param mezua Errore mezua
	 */
	public static void erakutsiErrorea(String mezua) {
		erakutsiErrorea("Errorea", mezua, null);
	}

	/**
	 * Errore mezua erakutsi erabiltzaileari titulua zehaztuz
	 * @param titulua Leihoaren titulua
	 * @param mezua Errore mezua
	 */
	public static void erakutsiErrorea(String titulua, String mezua) {
		erakutsiErrorea(titulua, mezua, null);
	}

	/**
	 * Errore mezua erakutsi salbuespena logeatuz
	 * @param titulua Leihoaren titulua
	 * @param mezua Errore mezua
	 * @param exception Salbuespena (aukerakoa)
	 */
	public static void erakutsiErrorea(String titulua, String mezua, Exception exception) {
		if (exception != null) {
			System.err.println("[ERROR] " + titulua + ": " + mezua);
			exception.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, mezua, titulua, JOptionPane.ERROR_MESSAGE);
		});
	}

	/**
	 * Informazio mezua erakutsi
	 * @param mezua Mezua
	 */
	public static void erakutsiInfo(String mezua) {
		erakutsiInfo("Informazioa", mezua);
	}

	/**
	 * Informazio mezua erakutsi titulua zehaztuz
	 * @param titulua Leihoaren titulua
	 * @param mezua Mezua
	 */
	public static void erakutsiInfo(String titulua, String mezua) {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, mezua, titulua, JOptionPane.INFORMATION_MESSAGE);
		});
	}

	/**
	 * Abisu mezua erakutsi
	 * @param mezua Mezua
	 */
	public static void erakutsiAbisua(String mezua) {
		erakutsiAbisua("Abisua", mezua);
	}

	/**
	 * Abisu mezua erakutsi titulua zehaztuz
	 * @param titulua Leihoaren titulua
	 * @param mezua Mezua
	 */
	public static void erakutsiAbisua(String titulua, String mezua) {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, mezua, titulua, JOptionPane.WARNING_MESSAGE);
		});
	}

	/**
	 * Baieztapen mezua erakutsi
	 * @param mezua Mezua
	 * @return true erabiltzaileak BAI sakatu badu
	 */
	public static boolean eskaBaieztapena(String mezua) {
		return eskaBaieztapena("Berretsi", mezua);
	}

	/**
	 * Baieztapen mezua erakutsi titulua zehaztuz
	 * @param titulua Leihoaren titulua
	 * @param mezua Mezua
	 * @return true erabiltzaileak BAI sakatu badu
	 */
	public static boolean eskaBaieztapena(String titulua, String mezua) {
		int aukera = JOptionPane.showConfirmDialog(null, mezua, titulua, 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		return aukera == JOptionPane.YES_OPTION;
	}

	/**
	 * Errore mota espezifikoaren arabera mezu egokia lortu
	 * @param mota Errore mota
	 * @param xehetasunak Xehetasun gehigarriak (aukerakoa)
	 * @return Errore mezua
	 */
	public static String lortuErroreMezua(ErrorMota mota, String xehetasunak) {
		String oinarriMezua;

		switch (mota) {
			case KONEXIO_ERROREA:
				oinarriMezua = "Ezin izan da zerbitzariarekin konektatu. Mesedez, egiaztatu zure internet konexioa.";
				break;
			case AUTENTIFIKAZIO_ERROREA:
				oinarriMezua = "Autentifikazio errorea. Egiaztatu zure kredentzialak.";
				break;
			case DATU_ERROREA:
				oinarriMezua = "Errorea datuak kargatzerakoan. Saiatu berriro geroago.";
				break;
			case VALIDAZIO_ERROREA:
				oinarriMezua = "Sarturiko datuak ez dira zuzenak.";
				break;
			case SISTEMA_ERROREA:
				oinarriMezua = "Sistema errorea gertatu da. Jarri harremanetan laguntza zerbitzuarekin.";
				break;
			case SINKRONIZAZIO_ERROREA:
				oinarriMezua = "Ezin izan dira datuak sinkronizatu. Datuak lokalean gorde dira.";
				break;
			default:
				oinarriMezua = "Errore ezezaguna gertatu da.";
				break;
		}

		if (xehetasunak != null && !xehetasunak.trim().isEmpty()) {
			return oinarriMezua + "\n\nXehetasunak: " + xehetasunak;
		}

		return oinarriMezua;
	}

	/**
	 * Errorea kudeatu eta mezua erakutsi
	 * @param mota Errore mota
	 * @param exception Salbuespena
	 */
	public static void kudeatu(ErrorMota mota, Exception exception) {
		String xehetasunak = exception != null ? exception.getMessage() : null;
		String mezua = lortuErroreMezua(mota, xehetasunak);
		erakutsiErrorea("Errorea", mezua, exception);
	}

	/**
	 * Errorea kudeatu xehetasunekin
	 * @param mota Errore mota
	 * @param xehetasunak Xehetasun testua
	 */
	public static void kudeatu(ErrorMota mota, String xehetasunak) {
		String mezua = lortuErroreMezua(mota, xehetasunak);
		erakutsiErrorea("Errorea", mezua, null);
	}
}
