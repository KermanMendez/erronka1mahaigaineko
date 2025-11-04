package util;

import java.util.Base64;

public class CryptoUtils {
	
	private static final byte DEFAULT_KEY = 0x5A;
	
	public byte[] xorBytes(byte[] data) {
		return xorBytes(data, DEFAULT_KEY);
	}
	
	public static byte[] xorBytes(byte[] data, byte key) {
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (byte) (data[i] ^ key);
		}
		return result;
	}
	
	public String xorEncrypt(String text) {
		return xorEncrypt(text, DEFAULT_KEY);
	}
	
	private String xorEncrypt(String text, byte key) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		byte[] data = text.getBytes();
		byte[] encrypted = xorBytes(data, key);
		return Base64.getEncoder().encodeToString(encrypted);
	}
	
	public String xorDecrypt(String base64Text) {
		return xorDecrypt(base64Text, DEFAULT_KEY);
	}
	
	private String xorDecrypt(String base64Text, byte key) {
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
