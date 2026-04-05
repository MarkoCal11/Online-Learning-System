package hr.javafx.onlinelearningsystem.model;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Quiz extends Entity{

    private String title;
    private List<Question> questions;
    private LocalDate dueDate;
    private Course course;
    private boolean readyToTake;

    private Quiz(Builder builder) {
        super(builder.id);
        this.title = builder.title;
        this.questions = List.copyOf(builder.questions);
        this.dueDate = builder.dueDate;
        this.course = builder.course;
        this.readyToTake = builder.readyToTake;
    }

    public String getTitle() {
        return title;
    }

    public List<Question> getQuestions() {
        return List.copyOf(questions);
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Course getCourse() {
        return course;
    }

    public boolean isReadyToTake() {
        return readyToTake;
    }

    public boolean isExpired() {
        return dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public boolean canBeTaken() {
        return readyToTake && !isExpired();
    }

    public static class Builder {
        private final Long id;
        private final String title;
        private Course course;

        private List<Question> questions = new ArrayList<>();
        private LocalDate dueDate;
        private boolean readyToTake;

        public Builder(Long id, String title, Course course) {
            this.id = id;
            this.title = title;
            this.course = course;
        }

        public Builder addQuestions(Question question) {
            questions.add(question);
            return this;
        }

        public Builder onDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder readyToTake(boolean readyToTake) {
            this.readyToTake = readyToTake;
            return this;
        }

        public Quiz build() {
            return new Quiz(this);
        }
    }
}
