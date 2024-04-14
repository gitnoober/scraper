package Model;

import java.util.List;

public class Job {
    private String title;
    private String location;
    private List<String> description;
    private List<String> qualification;
    private String jobType;
    private String postedBy;

    // Constructor
    public Job(String title, String location, List<String> description, List<String> qualification, String jobType, String postedBy) {
        this.title = title;
        this.location = location;
        this.description = description;
        this.qualification = qualification;
        this.jobType = jobType;
        this.postedBy = postedBy;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<String> getQualification() {
        return qualification;
    }

    public String getJobType() {
        return jobType;
    }

    public String getPostedBy() {
        return postedBy;
    }
}
