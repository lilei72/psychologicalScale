package com.example.android.questionnaire.data;
// 计算得分后返回显示的结果
public class Result {

    private String questionType;

    private float score;

    private String description;

    public Result(String questionType, float score, String description) {
        this.questionType = questionType;
        this.score = score;
        this.description = description;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
