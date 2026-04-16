package ru.popov.backgroundnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationDemoActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "demo_channel";
    private static final String CHANNEL_NAME = "Демонстрационный канал";
    private static final String CHANNEL_DESCRIPTION = "Канал для демонстрации уведомлений";
    private static final int NOTIFICATION_ID = 1001;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100;

    private NotificationManager mNotificationManager;
    private Handler mHandler = new Handler();
    private boolean notificationsEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_demo);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Уведомления");
        }

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Создаём канал уведомлений
        createNotificationChannel();

        // ЗАПРАШИВАЕМ РАЗРЕШЕНИЕ на уведомления (Android 13+)
        requestNotificationPermission();

        handleNotificationAction();
        initButtons();
    }

    // ЗАПРОС РАЗРЕШЕНИЯ НА УВЕДОМЛЕНИЯ
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                notificationsEnabled = true;
            }
        } else {
            notificationsEnabled = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notificationsEnabled = true;
                Toast.makeText(this, "Разрешение на уведомления получено", Toast.LENGTH_SHORT).show();
            } else {
                notificationsEnabled = false;
                Toast.makeText(this, "Разрешение на уведомления не получено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH  // ВАЖНО: HIGH для гарантированного показа
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setShowBadge(true);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private void handleNotificationAction() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("action")) {
            String action = intent.getStringExtra("action");
            if ("OK".equals(action)) {
                Toast.makeText(this, "Нажата кнопка OK", Toast.LENGTH_SHORT).show();
            } else if ("CANCEL".equals(action)) {
                Toast.makeText(this, "Уведомление закрыто", Toast.LENGTH_SHORT).show();
                mNotificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }

    private void initButtons() {
        Button simpleButton = findViewById(R.id.simple_notification_button);
        Button actionButton = findViewById(R.id.action_notification_button);
        Button progressButton = findViewById(R.id.progress_notification_button);
        Button bigTextButton = findViewById(R.id.big_text_notification_button);
        Button bigPictureButton = findViewById(R.id.big_picture_notification_button);
        Button cancelButton = findViewById(R.id.cancel_notification_button);

        simpleButton.setOnClickListener(v -> checkAndShowNotification(() -> showSimpleNotification()));
        actionButton.setOnClickListener(v -> checkAndShowNotification(() -> showActionNotification()));
        progressButton.setOnClickListener(v -> checkAndShowNotification(() -> showProgressNotification()));
        bigTextButton.setOnClickListener(v -> checkAndShowNotification(() -> showBigTextNotification()));
        bigPictureButton.setOnClickListener(v -> checkAndShowNotification(() -> showBigPictureNotification()));
        cancelButton.setOnClickListener(v -> {
            mNotificationManager.cancel(NOTIFICATION_ID);
            Toast.makeText(this, "Уведомление отменено", Toast.LENGTH_SHORT).show();
        });
    }

    // ПРОВЕРКА РАЗРЕШЕНИЯ ПЕРЕД ОТПРАВКОЙ
    private void checkAndShowNotification(Runnable showAction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission();
                Toast.makeText(this, "Сначала дайте разрешение на уведомления", Toast.LENGTH_LONG).show();
                return;
            }
        }
        showAction.run();
    }

    private PendingIntent getMainPendingIntent() {
        Intent intent = new Intent(this, NotificationDemoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void showSimpleNotification() {
        PendingIntent pendingIntent = getMainPendingIntent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Простое уведомление")
                .setContentText("Это пример простого уведомления")
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // HIGH для гарантии
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        sendNotification(builder.build());
    }

    private void showActionNotification() {
        Intent okIntent = new Intent(this, NotificationDemoActivity.class);
        okIntent.putExtra("action", "OK");
        PendingIntent okPendingIntent = PendingIntent.getActivity(
                this, 1, okIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent cancelIntent = new Intent(this, NotificationDemoActivity.class);
        cancelIntent.putExtra("action", "CANCEL");
        PendingIntent cancelPendingIntent = PendingIntent.getActivity(
                this, 2, cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Уведомление с действиями")
                .setContentText("Выберите действие")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(android.R.drawable.ic_input_add, "OK", okPendingIntent)
                .addAction(android.R.drawable.ic_delete, "Отмена", cancelPendingIntent)
                .setAutoCancel(true);

        sendNotification(builder.build());
    }

    private void showProgressNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Загрузка файла")
                .setContentText("Идёт загрузка...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(100, 0, false);

        sendNotification(builder.build());

        mHandler.postDelayed(() -> updateProgress(30), 1000);
        mHandler.postDelayed(() -> updateProgress(60), 2000);
        mHandler.postDelayed(() -> {
            updateProgress(100);
            showProgressComplete();
        }, 3000);
    }

    private void updateProgress(int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Загрузка файла")
                .setContentText("Загружено " + progress + "%")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(100, progress, false);

        sendNotification(builder.build());
    }

    private void showProgressComplete() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Загрузка завершена")
                .setContentText("Файл успешно загружен")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(0, 0, false)
                .setAutoCancel(true);

        sendNotification(builder.build());
    }

    private void showBigTextNotification() {
        String longText = "Это очень длинный текст уведомления. " +
                "Обычное уведомление показывает только первую строку, " +
                "но если использовать BigTextStyle, можно показать весь текст. " +
                "Пользователь может развернуть уведомление, чтобы увидеть полное содержимое. " +
                "Это полезно для отображения подробных сообщений, логов или ошибок.\n\n" +
                "Вторая часть длинного текста для демонстрации возможностей BigTextStyle.";

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(longText)
                .setBigContentTitle("Большое уведомление")
                .setSummaryText("Дополнительная информация");

        PendingIntent pendingIntent = getMainPendingIntent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Уведомление с большим текстом")
                .setContentText(longText.substring(0, 50) + "...")
                .setStyle(bigTextStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        sendNotification(builder.build());
    }

    private void showBigPictureNotification() {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery);
        Bitmap picture = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_camera);

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
                .bigPicture(picture)
                .bigLargeIcon(largeIcon)
                .setBigContentTitle("Уведомление с изображением")
                .setSummaryText("Разверните, чтобы увидеть изображение");

        PendingIntent pendingIntent = getMainPendingIntent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Уведомление с картинкой")
                .setContentText("Нажмите, чтобы развернуть")
                .setStyle(bigPictureStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        sendNotification(builder.build());
    }

    // МЕТОД ОТПРАВКИ УВЕДОМЛЕНИЯ С ПРОВЕРКОЙ
    private void sendNotification(android.app.Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
                Toast.makeText(this, "Уведомление отправлено!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Нет разрешения на уведомления", Toast.LENGTH_LONG).show();
            }
        } else {
            mNotificationManager.notify(NOTIFICATION_ID, notification);
            Toast.makeText(this, "Уведомление отправлено!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}