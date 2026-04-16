package ru.popov.backgroundnotification;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class BackgroundDemoActivity extends AppCompatActivity {

    // Таймер (Часть 1)
    private TextView mTimerText;
    private Button mStartTimerButton;
    private Button mStopTimerButton;
    private Handler mHandler = new Handler();
    private int mSeconds = 0;
    private boolean mTimerRunning = false;
    private Runnable mTimerRunnable;

    // Фоновая загрузка (Часть 2)
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private TextView mResultText;
    private Button mStartLoadingButton;
    private Button mCancelLoadingButton;
    private LoadingTask mLoadingTask;

    // ==================== ШАГ 2: onCreate - инициализация ====================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_demo);

        // Добавляем кнопку "Назад" в ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Фоновые задачи");
        }

        initTimerUI();
        initLoadingUI();
    }

    // ==================== ШАГ 3: Инициализация таймера ====================
    private void initTimerUI() {
        mTimerText = findViewById(R.id.timer_text);
        mStartTimerButton = findViewById(R.id.start_timer_button);
        mStopTimerButton = findViewById(R.id.stop_timer_button);

        mStartTimerButton.setOnClickListener(v -> startTimer());
        mStopTimerButton.setOnClickListener(v -> stopTimer());
    }

    // ==================== ШАГ 4: Запуск таймера с Handler ====================
    private void startTimer() {
        mTimerRunning = true;
        mSeconds = 0;
        mStartTimerButton.setEnabled(false);
        mStopTimerButton.setEnabled(true);
        runTimer();
    }

    /**
     * ШАГ 4.1: Использование Handler для периодического обновления UI
     * Handler.post() и postDelayed() для обновления таймера каждую секунду
     */
    private void runTimer() {
        mTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (mTimerRunning) {
                    mSeconds++;
                    mTimerText.setText(mSeconds + " секунд");
                    // postDelayed - запускает код с задержкой
                    mHandler.postDelayed(this, 1000);
                }
            }
        };
        mHandler.post(mTimerRunnable);
    }

    // ==================== ШАГ 5: Остановка таймера ====================
    private void stopTimer() {
        mTimerRunning = false;
        mStartTimerButton.setEnabled(true);
        mStopTimerButton.setEnabled(false);
        if (mTimerRunnable != null) {
            mHandler.removeCallbacks(mTimerRunnable);
        }
    }

    // ==================== ШАГ 6: Инициализация UI для загрузки ====================
    private void initLoadingUI() {
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressText = findViewById(R.id.progress_text);
        mResultText = findViewById(R.id.result_text);
        mStartLoadingButton = findViewById(R.id.start_loading_button);
        mCancelLoadingButton = findViewById(R.id.cancel_loading_button);

        mStartLoadingButton.setOnClickListener(v -> startLoading());
        mCancelLoadingButton.setOnClickListener(v -> cancelLoading());
    }

    // ==================== ШАГ 7: Запуск AsyncTask ====================
    private void startLoading() {
        mStartLoadingButton.setEnabled(false);
        mCancelLoadingButton.setEnabled(true);
        mResultText.setText("");
        mProgressBar.setProgress(0);
        mProgressText.setText("Прогресс: 0%");

        mLoadingTask = new LoadingTask();
        mLoadingTask.execute(100); // Эмулируем загрузку 100 единиц
    }

    // ==================== ШАГ 8: Отмена загрузки (Задание 1) ====================
    private void cancelLoading() {
        if (mLoadingTask != null && mLoadingTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadingTask.cancel(true);
            mResultText.setText("Загрузка отменена пользователем");
            mStartLoadingButton.setEnabled(true);
            mCancelLoadingButton.setEnabled(false);
            Toast.makeText(this, "Загрузка отменена", Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== ШАГ 9: AsyncTask для фоновой загрузки ====================
    /**
     * AsyncTask: <Params, Progress, Result>
     * - Params: Integer (максимальное количество элементов)
     * - Progress: Integer (текущий прогресс)
     * - Result: String (результат выполнения)
     */
    private class LoadingTask extends AsyncTask<Integer, Integer, String> {
        private Random mRandom = new Random();

        // Выполняется в UI-потоке перед началом задачи
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mResultText.setText("Начинаем загрузку...");
        }

        // Выполняется в фоновом потоке (нельзя обновлять UI)
        @Override
        protected String doInBackground(Integer... params) {
            int max = params[0];
            try {
                for (int i = 1; i <= max; i++) {
                    if (isCancelled()) {
                        return "Загрузка прервана";
                    }

                    // Эмуляция длительной операции
                    Thread.sleep(100);

                    // ЗАДАНИЕ 2: Эмуляция ошибки сети (10% вероятность)
                    if (mRandom.nextInt(10) == 0) {
                        return "Ошибка сети! Не удалось загрузить данные.";
                    }

                    // Публикуем прогресс для обновления UI
                    publishProgress(i, max);
                }
            } catch (InterruptedException e) {
                return "Загрузка прервана";
            }
            return "Загрузка завершена! Загружено " + max + " элементов";
        }

        // Выполняется в UI-потоке при вызове publishProgress
        @Override
        protected void onProgressUpdate(Integer... values) {
            int current = values[0];
            int total = values[1];
            int percent = (current * 100) / total;
            mProgressBar.setProgress(percent);
            mProgressText.setText("Прогресс: " + percent + "% (" + current + "/" + total + ")");
        }

        // Выполняется в UI-потоке после завершения doInBackground
        @Override
        protected void onPostExecute(String result) {
            mResultText.setText(result);
            mStartLoadingButton.setEnabled(true);
            mCancelLoadingButton.setEnabled(false);

            if (result.startsWith("Ошибка")) {
                Toast.makeText(BackgroundDemoActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }

        // Выполняется в UI-потоке при отмене задачи
        @Override
        protected void onCancelled(String result) {
            if (result != null) {
                mResultText.setText(result);
            } else {
                mResultText.setText("Загрузка отменена");
            }
            mStartLoadingButton.setEnabled(true);
            mCancelLoadingButton.setEnabled(false);
        }
    }

    // ==================== ШАГ 10: Очистка ресурсов ====================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimerRunning = false;
        if (mTimerRunnable != null) {
            mHandler.removeCallbacks(mTimerRunnable);
        }
        if (mLoadingTask != null) {
            mLoadingTask.cancel(true);
        }
    }

    // Кнопка "Назад" в ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}