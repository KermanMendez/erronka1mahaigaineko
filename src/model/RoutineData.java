package model;

import java.util.List;

public class RoutineData {
    private final List<Exercise> exercises;
    private final String description;
    private final int totalSets;

    public RoutineData(List<Exercise> exercises, String description, int totalSets) {
        this.exercises = exercises;
        this.description = description;
        this.totalSets = totalSets;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public String getDescription() {
        return description;
    }

    public int getTotalSets() {
        return totalSets;
    }
}
