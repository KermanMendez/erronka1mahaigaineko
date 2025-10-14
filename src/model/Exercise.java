package model;

public class Exercise {

	private String name;
	private String description;
	private String img;
	private int serieNumber;
	private int repNumber;
	private int serieTime;
	private int restTime;

	public Exercise(String name, String description, String img, int serieNumber, int repNumber,
			int serieTime, int restTime) {
		this.name = name;
		this.description = description;
		this.img = img;
		this.serieNumber = serieNumber;
		this.repNumber = repNumber;
		this.serieTime = serieTime;
		this.restTime = restTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public int getSerieNumber() {
		return serieNumber;
	}

	public void setSerieNumber(int serieNumber) {
		this.serieNumber = serieNumber;
	}

	public int getRepNumber() {
		return repNumber;
	}

	public void setRepNumber(int repNumber) {
		this.repNumber = repNumber;
	}

	public int getSerieTime() {
		return serieTime;
	}

	public void setSerieTime(int serieTime) {
		this.serieTime = serieTime;
	}

	public int getRestTime() {
		return restTime;
	}

	public void setRestTime(int restTime) {
		this.restTime = restTime;
	}

	@Override
	public String toString() {
		return "Exercise [name=" + name + ", description=" + description + ", img=" + img + ", serieNumber="
				+ serieNumber + ", repNumber=" + repNumber + ", serieTime=" + serieTime + ", restTime=" + restTime
				+ "]";
	}

	
	
}
