package util;

import java.util.Base64;

/**
 * Utilidad para operaciones de encriptación/desencriptación XOR
 */
public class CryptoUtils {
	
	private static final byte DEFAULT_KEY = 0x5A;
	
	/**
	 * Aplica encriptación/desencriptación XOR a un array de bytes
	 */
	public static byte[] xorBytes(byte[] data) {
		return xorBytes(data, DEFAULT_KEY);
	}
	
	/**
	 * Aplica encriptación/desencriptación XOR con una clave personalizada
	 */
	public static byte[] xorBytes(byte[] data, byte key) {
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (byte) (data[i] ^ key);
		}
		return result;
	}
	
	/**
	 * Encripta un texto usando XOR y lo codifica en Base64
	 */
	public static String xorEncrypt(String text) {
		return xorEncrypt(text, DEFAULT_KEY);
	}
	
	/**
	 * Encripta un texto usando XOR con clave personalizada y lo codifica en Base64
	 */
	public static String xorEncrypt(String text, byte key) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		byte[] data = text.getBytes();
		byte[] encrypted = xorBytes(data, key);
		return Base64.getEncoder().encodeToString(encrypted);
	}
	
	/**
	 * Desencripta un texto codificado en Base64 usando XOR
	 */
	public static String xorDecrypt(String base64Text) {
		return xorDecrypt(base64Text, DEFAULT_KEY);
	}
	
	/**
	 * Desencripta un texto codificado en Base64 usando XOR con clave personalizada
	 */
	public static String xorDecrypt(String base64Text, byte key) {
		if (base64Text == null || base64Text.isEmpty()) {
			return "";
		}
		try {
			byte[] encryptedBytes = Base64.getDecoder().decode(base64Text);
			byte[] decrypted = xorBytes(encryptedBytes, key);
			return new String(decrypted);
		} catch (IllegalArgumentException e) {
			return "";
		}
	}
}
