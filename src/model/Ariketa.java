package model;

public class Ariketa {

	private String izena;
	private String deskribapena;
	private String irudia;
	private int serieKopurua;
	private int errepikapenKopurua;
	private int serieDenbora;
	private int atsedenDenbora;

	public Ariketa(String izena, String deskribapena, String irudia, int serieKopurua, int errepikapenKopurua,
			int serieDenbora, int atsedenDenbora) {
		super();
		this.izena = izena;
		this.deskribapena = deskribapena;
		this.irudia = irudia;
		this.serieKopurua = serieKopurua;
		this.errepikapenKopurua = errepikapenKopurua;
		this.serieDenbora = serieDenbora;
		this.atsedenDenbora = atsedenDenbora;
	}

	public String getIzena() {
		return izena;
	}

	public void setIzena(String izena) {
		this.izena = izena;
	}

	public String getDeskribapena() {
		return deskribapena;
	}

	public void setDeskribapena(String deskribapena) {
		this.deskribapena = deskribapena;
	}

	public String getIrudia() {
		return irudia;
	}

	public void setIrudia(String irudia) {
		this.irudia = irudia;
	}

	public int getSerieKopurua() {
		return serieKopurua;
	}

	public void setSerieKopurua(int serieKopurua) {
		this.serieKopurua = serieKopurua;
	}

	public int getErrepikapenKopurua() {
		return errepikapenKopurua;
	}

	public void setErrepikapenKopurua(int errepikapenKopurua) {
		this.errepikapenKopurua = errepikapenKopurua;
	}

	public int getSerieDenbora() {
		return serieDenbora;
	}

	public void setSerieDenbora(int serieDenbora) {
		this.serieDenbora = serieDenbora;
	}

	public int getAtsedenDenbora() {
		return atsedenDenbora;
	}

	public void setAtsedenDenbora(int atsedenDenbora) {
		this.atsedenDenbora = atsedenDenbora;
	}

	@Override
	public String toString() {
		return "Ariketa [izena=" + izena + ", deskribapena=" + deskribapena + ", irudia=" + irudia + ", serieKopurua="
				+ serieKopurua + ", errepikapenKopurua=" + errepikapenKopurua + ", serieDenbora=" + serieDenbora
				+ ", atsedenDenbora=" + atsedenDenbora + "]";
	}

}
