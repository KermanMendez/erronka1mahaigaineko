package view;

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * Aplikazioaren logoa kargatu eta cache-an gordetzen du errendimendua hobetzeko
 */
public class LoadLogo {

	// Logo eskalatuaren cache estatikoa (Singleton patroia)
	private static ImageIcon cachedLogo = null;

	/**
	 * Logo eskalatua lortu (cache-a erabiltzen du badago)
	 * 
	 * @param logo parametro zaharra (ez da erabiltzen, bateragarritasuna
	 *             mantentzeko)
	 * @return Logo eskalatua eta cache-atua
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
	 * Logo eskalatua lortu (bertsio sinplifikatua parametrorik gabe)
	 */
	public ImageIcon getLogo() {
		return getLogo(null);
	}
}
