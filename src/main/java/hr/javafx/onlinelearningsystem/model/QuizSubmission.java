package hr.javafx.onlinelearningsystem.model;

import java.time.LocalDateTime;
import java.util.Map;

public record QuizSubmission(Integer id, Quiz quiz, Enrolment enrolledStudent,
                             LocalDateTime submittedAt, Integer grade, Double percentage,
                             Map<Integer, Integer> selectedAnswers) {
}