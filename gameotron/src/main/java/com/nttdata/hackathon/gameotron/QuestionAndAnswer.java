package com.nttdata.hackathon.gameotron;

/**
 * Created by HUETTM on 23.06.2017.
 */
public class QuestionAndAnswer {

    private final String question;

    private final String answer;

    public QuestionAndAnswer(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}
