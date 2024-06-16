class SubjectGrade {
    private String subjectName;
    private int credit;
    private String grade;

    public SubjectGrade(String subjectName, int credit, String grade) {
        this.subjectName = subjectName;
        this.credit = credit;
        this.grade = grade;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getCredit() {
        return credit;
    }

    public String getGrade() {
        return grade;
    }
}