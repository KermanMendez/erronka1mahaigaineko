package model;

public class Exercise {

	private String name;
	private String description;
	private String img;
	private String videoURL;
	private int sets;
	private int reps;
	private int serieTime;
	private int restTime;

	public Exercise() {
	}

	public Exercise(String name, String description, String img, String videoURL, int sets, int reps, int serieTime,
			int restTime) {
		this.name = name;
		this.description = description;
		this.img = img;
		this.videoURL = videoURL;
		this.sets = sets;
		this.reps = reps;
		this.serieTime = serieTime;
		this.restTime = restTime;
	}

	public Exercise(String name, int sets, int reps) {
		this.name = name;
		this.sets = sets;
		this.reps = reps;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getImg() {
		return img;
	}

	public String getVideoURL() {
		return videoURL;
	}

	public int getSets() {
		return sets;
	}

	public int getReps() {
		return reps;
	}

	public int getSerieTime() {
		return serieTime;
	}

	public int getRestTime() {
		return restTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public void setVideoURL(String videoURL) {
		this.videoURL = videoURL;
	}

	public void setSets(Object sets) {
		this.sets = convertToInt(sets);
	}

	public void setReps(Object reps) {
		this.reps = convertToInt(reps);
	}

	public void setSerieTime(Object serieTime) {
		this.serieTime = convertToInt(serieTime);
	}

	public void setRestTime(Object restTime) {
		this.restTime = convertToInt(restTime);
	}

	private int convertToInt(Object value) {
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
				System.out.println("Warning: Could not parse '" + strValue + "' as integer, using 0");
				return 0;
			}
		}

		System.out.println(
				"Warning: Unexpected type " + value.getClass().getSimpleName() + " for numeric field, using 0");
		return 0;
	}

	@Override
	public String toString() {
		return name + " — " + sets + " sets × " + reps + " reps";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		Exercise exercise = (Exercise) obj;
		return sets == exercise.sets && reps == exercise.reps && serieTime == exercise.serieTime
				&& restTime == exercise.restTime && (name != null ? name.equals(exercise.name) : exercise.name == null)
				&& (description != null ? description.equals(exercise.description) : exercise.description == null)
				&& (img != null ? img.equals(exercise.img) : exercise.img == null)
				&& (videoURL != null ? videoURL.equals(exercise.videoURL) : exercise.videoURL == null);
	}
}