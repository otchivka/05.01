package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "QuestActivity";  // константа для логирования
    private static final String KEY_INDEX = "index";  // ключ для сохранения индекса

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mNextButton;
    private Button mPrevButton;
    private TextView mQuestionTextView;
    private Question[] mQuestionBank;
    private int mCurrentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) вызван");
        setContentView(R.layout.activity_main);

        // Инициализация массива вопросов
        mQuestionBank = new Question[]{
                new Question(R.string.question_text, true),
                new Question(R.string.question_2, false),
                new Question(R.string.question_3, true),
                new Question(R.string.question_4, true),
                new Question(R.string.question_5, false),
                new Question(R.string.question_6, false),
                new Question(R.string.question_7, true),
                new Question(R.string.question_8, true),
                new Question(R.string.question_9, true),
                new Question(R.string.question_10, false)
        };

        // Получаем ссылки на виджеты
        mTrueButton = findViewById(R.id.true_button);
        mFalseButton = findViewById(R.id.false_button);
        mQuestionTextView = findViewById(R.id.question_text_view);
        mNextButton = findViewById(R.id.next_button);
        mPrevButton = findViewById(R.id.prev_button);


        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            Log.d(TAG, "Восстановлен индекс: " + mCurrentIndex);
        }


        // Обработчик кнопки "Да"
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });

        // Обработчик кнопки "Нет"
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        // Обработчик кнопки "Далее"
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        // Обработчик кнопки "Назад"
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex - 1 + mQuestionBank.length) % mQuestionBank.length;
                updateQuestion();
            }
        });


        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });


        updateQuestion();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() вызван");
        outState.putInt(KEY_INDEX, mCurrentIndex);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() вызван");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() вызван");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() вызван");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() вызван");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() вызван");
    }


    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }


    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId;
        if (userPressedTrue == answerIsTrue) {
            messageResId = R.string.correct_toast;
        } else {
            messageResId = R.string.incorrect_toast;
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }
}