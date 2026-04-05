package hr.javafx.onlinelearningsystem.model;


import java.util.List;

public record Question(Integer id, String text, Quiz quiz, List<String> options, Integer correctAnswerIndex){

    public boolean isCorrectAnswer(Integer selectedIndex) {
        return selectedIndex.equals(correctAnswerIndex);
    }

    public String getCorrectAnswer() {
        return options.get(correctAnswerIndex);
    }
}
