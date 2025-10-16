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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public Exercise[] getExercises() {
        return exercises;
    }

    public void setExercises(Exercise[] exercises) {
        this.exercises = exercises;
    }
}