package edu.augusta.sccs.trivia.cassandra;

import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ServerQuestion {

    public static final String QUESTION_UUID = "question_uuid";
    public static final String DIFFICULTY = "difficulty";
    public static final String QUESTION = "question";
    public static final String ANSWER = "answer";
    public static final String CHOICES = "choices";

    private UUID question_uuid;

    private int answerType;

    private int difficulty;

    private String question;

    private String answer;

    private String choices;

    public UUID getQuestionUuid() {
        return question_uuid;
    }

    public void setQuestionUuid(UUID question_uuid) {
        this.question_uuid = question_uuid;
    }

    public int getAnswerType() {
        return answerType;
    }

    public void setAnswerType(int answerType) {
        this.answerType = answerType;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean hasChoices() {
        return choices != null && !choices.isEmpty();
    }

    public String getChoices() {
        return choices;
    }

    public List<String> getListOfChoices() {
        List<String> choices = new ArrayList<>();
        if(hasChoices()) {
            String[] tokens = this.choices.split(":");
            for(String token : tokens) {
                choices.add(token);
            }
        }
        return choices;
    }

    public void setChoices(String choices) {
        this.choices = choices;
    }

    public static ServerQuestion convert(Row x) {
        ServerQuestion question = new ServerQuestion();
        question.setQuestionUuid(x.getUuid(QUESTION_UUID));
        question.setDifficulty(x.getInt(DIFFICULTY));
        question.setQuestion(x.getString(QUESTION));
        question.setAnswer(x.getString(ANSWER));
        question.setChoices(x.getString(CHOICES));
        return question;
    }
}
