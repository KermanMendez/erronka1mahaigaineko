package model;

import java.awt.Image;

import javax.swing.ImageIcon;

public class LoadLogo {

	public ImageIcon getLogo(ImageIcon logo) {
		if (logo == null) {
			ImageIcon jatorrekoIkono = new ImageIcon(getClass().getResource("/img/logo.png"));
			Image irudiaEskalatua = jatorrekoIkono.getImage().getScaledInstance(360, 260, Image.SCALE_SMOOTH);
			logo = new ImageIcon(irudiaEskalatua);
		}
		return logo;
	}
}
