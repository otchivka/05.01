package ru.popov.droidquest;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "QuestActivity";
    private static final String KEY_INDEX = "index";
    private static final int REQUEST_CODE_DECEIT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mNextButton;
    private Button mPrevButton;
    private Button mDeceitButton;
    private TextView mQuestionTextView;

    private Question[] mQuestionBank;
    private int mCurrentIndex = 0;
    private boolean mIsDeceiver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) вызван");
        setContentView(R.layout.activity_main);

        mQuestionBank = new Question[]{
                new Question(R.string.question_text, true),
                new Question(R.string.question_2, false),
                new Question(R.string.question_3, true),
                new Question(R.string.question_4, true),
                new Question(R.string.question_5, false)
        };

        mTrueButton = findViewById(R.id.true_button);
        mFalseButton = findViewById(R.id.false_button);
        mQuestionTextView = findViewById(R.id.question_text_view);
        mNextButton = findViewById(R.id.next_button);
        mPrevButton = findViewById(R.id.prev_button);
        mDeceitButton = findViewById(R.id.deceit_button);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mIsDeceiver = savedInstanceState.getBoolean("isDeceiver", false);
            Log.d(TAG, "Восстановлен индекс: " + mCurrentIndex + ", обман: " + mIsDeceiver);
        }

        setListeners();
        updateQuestion();
    }

    private void setListeners() {
        mTrueButton.setOnClickListener(v -> checkAnswer(true));
        mFalseButton.setOnClickListener(v -> checkAnswer(false));

        mNextButton.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            mIsDeceiver = false;
            updateQuestion();
        });

        mPrevButton.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex - 1 + mQuestionBank.length) % mQuestionBank.length;
            mIsDeceiver = false;
            updateQuestion();
        });

        mQuestionTextView.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            mIsDeceiver = false;
            updateQuestion();
        });

        mDeceitButton.setOnClickListener(v -> {
            boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
            Intent intent = DeceitActivity.newIntent(MainActivity.this, answerIsTrue);
            startActivityForResult(intent, REQUEST_CODE_DECEIT);
        });
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId;

        if (mIsDeceiver) {
            messageResId = R.string.judgment_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_CODE_DECEIT && data != null) {
            mIsDeceiver = DeceitActivity.wasAnswerShown(data);
            Log.d(TAG, "Обман зафиксирован: " + mIsDeceiver);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() вызван");
        outState.putInt(KEY_INDEX, mCurrentIndex);
        outState.putBoolean("isDeceiver", mIsDeceiver);
    }

    @Override protected void onStart() { super.onStart(); Log.d(TAG, "onStart()"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "onResume()"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "onPause()"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "onStop()"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy()"); }
}