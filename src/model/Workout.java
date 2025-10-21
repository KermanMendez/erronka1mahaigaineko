package model;

public class Workout {
	private String name;
	private Double duration;
	private int level;
	private String videoURL;
	private Exercise[] exercises;

	public Workout(String name, Double duration, int level, String videoURL, Exercise[] exercises) {
		this.name = name;
		this.duration = duration;
		this.level = level;
		this.videoURL = videoURL;
		this.exercises = exercises;
	}

	public Double getDuration() {
		return duration;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

	public Exercise[] getExercises() {
		return exercises;
	}

	public String getVideoURL() {
		return videoURL;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public void setExercises(Exercise[] exercises) {
		this.exercises = exercises;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVideoURL(String videoURL) {
		this.videoURL = videoURL;
	}
}