package model;

public class Exercise {

	private int reps;
	private String name;
	
	public Exercise(int reps, String name) {
		this.reps = reps;
		this.name = name;
	}

	public int getReps() {
		return reps;
	}

	public void setReps(int reps) {
		this.reps = reps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
