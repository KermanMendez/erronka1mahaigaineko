package model;

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * Carga y cachea el logo de la aplicación para mejorar el rendimiento
 */
public class LoadLogo {
	
	// Cache estático del logo escalado (Singleton pattern)
	private static ImageIcon cachedLogo = null;
	
	/**
	 * Obtiene el logo escalado (usa cache si ya existe)
	 * @param logo parámetro legacy (ignorado, mantiene compatibilidad)
	 * @return Logo escalado y cacheado
	 */
	public ImageIcon getLogo(ImageIcon logo) {
		if (cachedLogo == null) {
			synchronized (LoadLogo.class) {
				if (cachedLogo == null) {
					ImageIcon jatorrekoIkono = new ImageIcon(getClass().getResource("/img/logo.png"));
					Image irudiaEskalatua = jatorrekoIkono.getImage().getScaledInstance(360, 260, Image.SCALE_SMOOTH);
					cachedLogo = new ImageIcon(irudiaEskalatua);
				}
			}
		}
		return cachedLogo;
	}
	
	/**
	 * Obtiene el logo escalado (versión simplificada sin parámetros)
	 */
	public ImageIcon getLogo() {
		return getLogo(null);
	}
}
