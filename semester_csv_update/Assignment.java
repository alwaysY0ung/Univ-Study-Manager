public class Assignment {
    private boolean completed;
    private String name;
    private String dueDate;
    private String subject;
    private String type;
    private String details;

    public Assignment(boolean completed, String name, String dueDate, String subject, String type, String details) {
        this.completed = completed;
        this.name = name;
        this.dueDate = dueDate;
        this.subject = subject;
        this.type = type;
        this.details = details;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean getCompleted() {
        return completed;
    }

    public String getName() {
        return name;
    }

    public String getDueDate() {
        return dueDate;
    }
    public String getSubject() {
        return subject;
    }

    public String getType() {
        return type;
    }
    public String getDetails() {
        return details;
    }
}
