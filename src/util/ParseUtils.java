package util;

/**
 * Utilidad para operaciones de parseo y conversión
 */
public class ParseUtils {
	
	/**
	 * Convierte un Object a int de forma segura
	 * Soporta Number, String y null
	 */
	public static int parseInt(Object value) {
		if (value == null) {
			return 0;
		}
		
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		
		if (value instanceof String) {
			String strValue = ((String) value).trim();
			if (strValue.isEmpty()) {
				return 0;
			}
			try {
				return Integer.parseInt(strValue);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		
		return 0;
	}
	
	/**
	 * Parsea múltiples valores hasta encontrar uno válido
	 */
	public static int parseIntFirstValid(Object... values) {
		for (Object v : values) {
			if (v != null) {
				int result = parseInt(v);
				if (result != 0 || "0".equals(String.valueOf(v).trim())) {
					return result;
				}
			}
		}
		return 0;
	}
	
	/**
	 * Parsea un String a boolean considerando múltiples formatos
	 */
	public static boolean parseBoolean(String val) {
		if (val == null) {
			return false;
		}
		val = val.trim().toLowerCase();
		return val.equals("true") || val.equals("bai") || val.equals("yes") || val.equals("1");
	}
	
	/**
	 * Convierte un boolean a String en euskera (Bai/Ez)
	 */
	public static String booleanToEuskera(boolean value) {
		return value ? "Bai" : "Ez";
	}
	
	/**
	 * Obtiene un valor int desde Long de Firestore
	 */
	public static int getIntValue(Long val) {
		return val != null ? val.intValue() : 0;
	}
}
