package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	
	/**
	 * Uneko data formateatua lortzen du
	 * Obtiene la fecha actual formateada
	 * 
	 * @return dd/MM/yyyy formatua duen String-a / String con formato dd/MM/yyyy
	 */
	public static String getCurrentFormattedDate() {
		synchronized (sdf) {
			return sdf.format(new Date());
		}
	}
	
	public String formatDate(Date date) {
		if (date == null) {
			return "";
		}
		synchronized (sdf) {
			return sdf.format(date);
		}
	}
	
	public Date parseDate(String dateStr) throws ParseException {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		synchronized (sdf) {
			return sdf.parse(dateStr.trim());
		}
	}
	
	public Date parseDateSafe(String dateStr) {
		try {
			return parseDate(dateStr);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public String getCurrentDateFormatted() {
		return getCurrentFormattedDate();
	}
	
	/**
	 * Date formatuatu String bihurtu (metodo estatiko erosoa)
	 * @param date Formateatzeko data
	 * @return Formateatutako String edo kate hutsa null bada
	 */
	public static String formatDateStatic(Date date) {
		if (date == null) {
			return "";
		}
		synchronized (sdf) {
			return sdf.format(date);
		}
	}
}

