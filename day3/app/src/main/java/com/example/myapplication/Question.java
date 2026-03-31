package com.example.myapplication;  // Замените "ваша_фамилия" на свою фамилию

public class Question {
    private int mTextResId;      // идентификатор строкового ресурса вопроса
    private boolean mAnswerTrue;  // правильный ответ (true - Да, false - Нет)

    // Конструктор
    public Question(int textResId, boolean answerTrue) {
        mTextResId = textResId;
        mAnswerTrue = answerTrue;
    }

    // Геттеры
    public int getTextResId() {
        return mTextResId;
    }

    public boolean isAnswerTrue() {
        return mAnswerTrue;
    }

    // Сеттеры (опционально)
    public void setTextResId(int textResId) {
        mTextResId = textResId;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswerTrue = answerTrue;
    }
}