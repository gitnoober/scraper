package Model;

import java.util.ArrayList;
import java.util.List;

public class Department {
    private String name;
    private List<Job> jobs;

    // Constructor
    public Department(String name) {
        this.name = name;
        this.jobs = new ArrayList<>();
    }

    // Method to add a job to the department
    public void addJob(Job job) {
        jobs.add(job);
    }

    // Getters
    public String getName() {
        return name;
    }

    public List<Job> getJobs() {
        return jobs;
    }
}
