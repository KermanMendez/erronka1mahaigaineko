package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilidad para formateo y parseo de fechas
 */
public class DateUtils {
	
	public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	
	/**
	 * Formatea una fecha a String con formato dd/MM/yyyy
	 */
	public static String formatDate(Date date) {
		if (date == null) {
			return "";
		}
		synchronized (sdf) {
			return sdf.format(date);
		}
	}
	
	/**
	 * Parsea un String a Date con formato dd/MM/yyyy
	 */
	public static Date parseDate(String dateStr) throws ParseException {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		synchronized (sdf) {
			return sdf.parse(dateStr.trim());
		}
	}
	
	/**
	 * Parsea un String a Date de forma segura (devuelve null si hay error)
	 */
	public static Date parseDateSafe(String dateStr) {
		try {
			return parseDate(dateStr);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * Obtiene la fecha actual en formato dd/MM/yyyy
	 */
	public static String getCurrentDateFormatted() {
		return formatDate(new Date());
	}
}
