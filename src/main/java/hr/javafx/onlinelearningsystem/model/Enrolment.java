package hr.javafx.onlinelearningsystem.model;

import hr.javafx.onlinelearningsystem.enums.EnrolmentStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Enrolment extends Entity {
    private StudentProfile student;
    private Course course;
    private final LocalDate enrolmentDate;
    private EnrolmentStatus enrolmentStatus;
    private Double finalGrade;
    private final List<QuizSubmission> quizSubmissions = new ArrayList<>();

    public Enrolment(Long id, StudentProfile student, Course course, LocalDate enrolmentDate,
                     EnrolmentStatus enrolmentStatus) {
        super(id);
        this.student = student;
        this.course = course;
        this.enrolmentDate = enrolmentDate;
        this.enrolmentStatus = enrolmentStatus;
        this.finalGrade = null;
    }

    public StudentProfile getStudent() {
        return student;
    }
    public void setStudent(StudentProfile student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }
    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getEnrollmentDate() {
        return enrolmentDate;
    }

    public EnrolmentStatus getEnrollmentStatus() {
        return enrolmentStatus;
    }
    public void setEnrollmentStatus(EnrolmentStatus enrolmentStatus) {
        this.enrolmentStatus = enrolmentStatus;
    }

    public Optional<Double> getFinalGrade() {
        return Optional.ofNullable(finalGrade);
    }
    public void setFinalGrade(Double grade) {
        this.finalGrade = grade;
        if (grade != null) {
            this.enrolmentStatus = EnrolmentStatus.COMPLETED;
        }
    }

    public List<QuizSubmission> getQuizSubmissions() {
        return List.copyOf(quizSubmissions);
    }
    public void addQuizSubmission(QuizSubmission submission) {
        quizSubmissions.add(submission);
    }
    public void removeQuizSubmission(QuizSubmission submission) {
        quizSubmissions.remove(submission);
    }
}