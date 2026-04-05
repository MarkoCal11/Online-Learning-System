package hr.javafx.onlinelearningsystem.model;


public class Course extends Entity {

    private String title;
    private String description;
    private Integer ects;
    private TeacherProfile teacher;

    private Integer studentCount;
    private Integer lessonCount;
    private Integer quizCount;


    public Course(String title, String description, Integer ects, TeacherProfile teacher){
        this.title = title;
        this.description = description;
        this.ects = ects;
        this.teacher = teacher;
    }

    public Course(Long id, String title, String description, Integer ects,
                  TeacherProfile teacher, Integer studentCount, Integer lessonCount, Integer quizCount){
        super(id);
        this.title = title;
        this.description = description;
        this.ects = ects;
        this.teacher = teacher;
        this.studentCount = studentCount;
        this.lessonCount = lessonCount;
        this.quizCount = quizCount;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEcts() {
        return ects;
    }
    public void setEcts(Integer ects) {
        this.ects = ects;
    }

    public TeacherProfile getTeacher() {
        return teacher;
    }
    public void setTeacher(TeacherProfile teacher) {
        this.teacher = teacher;
    }
    public boolean hasTeacher() {
        return teacher != null;
    }

    public Integer getStudentCount() {
        return studentCount;
    }
    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getLessonsCount() {
        return lessonCount;
    }
    public void setLessonsCount(Integer lessonsCount) {
        this.lessonCount = lessonsCount;
    }

    public Integer getQuizzesCount() {
        return quizCount;
    }
    public void setQuizzesCount(Integer quizzesCount) {
        this.quizCount = quizzesCount;
    }
}
