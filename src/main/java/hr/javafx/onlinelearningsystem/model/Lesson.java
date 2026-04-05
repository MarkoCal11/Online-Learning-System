package hr.javafx.onlinelearningsystem.model;

public class Lesson extends Entity {

    private String title;
    private String content;
    private Course course;

    public Lesson(String title, String content, Course course){
        this.title=title;
        this.content=content;
        this.course=course;
    }

    public Lesson(Long id, String title, String content, Course course){
        super(id);
        this.title=title;
        this.content=content;
        this.course=course;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public Course getCourse() {
        return course;
    }
    public void setCourse(Course course) {
        this.course = course;
    }
}
