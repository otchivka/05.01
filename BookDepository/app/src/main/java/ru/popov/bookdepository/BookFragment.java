package ru.popov.bookdepository;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BookFragment extends Fragment {
    private static final String TAG = "BookFragment";
    private static final String ARG_BOOK_ID = "book_id";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PHOTO = 1;                      // Day 15: Код запроса для камеры
    private static final int REQUEST_CAMERA_PERMISSION = 2;          // Day 15: Код запроса разрешения
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_PHOTO_VIEWER = "DialogPhotoViewer";  // Day 15, Задание 1: Тег для диалога просмотра фото

    private Book mBook;
    private EditText mTitleField;
    private CheckBox mReadedCheckBox;
    private Button mDateButton;
    private Button mReportButton;      // Day 14: Кнопка отправки отчёта
    private Button mSearchButton;      // Day 14, Задание 1: Кнопка поиска в браузере
    private ImageView mPhotoView;      // Day 15: ImageView для отображения фото обложки
    private Button mCameraButton;      // Day 15: Кнопка вызова камеры
    private File mPhotoFile;           // Day 15: Файл для хранения фотографии

    // Day 11, Шаг 2: Статический метод newInstance для передачи ID книги
    public static BookFragment newInstance(UUID bookId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK_ID, bookId);
        BookFragment fragment = new BookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Day 7, Шаг 7: Жизненный цикл фрагмента
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() вызван");

        // Day 12, Задание 1: Включаем меню для фрагмента
        setHasOptionsMenu(true);

        UUID bookId = (UUID) getArguments().getSerializable(ARG_BOOK_ID);
        mBook = BookLab.get(getActivity()).getBook(bookId);

        // Day 15, Шаг 8: Получаем файл для фотографии
        mPhotoFile = BookLab.get(getActivity()).getPhotoFile(mBook);
    }

    // Day 12, Задание 1: Создание меню (кнопка удаления)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_book, menu);
    }

    // Day 12, Задание 1: Обработка нажатия на кнопку удаления
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete_book) {
            BookLab.get(getActivity()).deleteBook(mBook);
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Day 7, Шаг 7: Создание представления фрагмента
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() вызван");
        View v = inflater.inflate(R.layout.fragment_book, container, false);

        // Day 15, Шаг 8: Находим элементы камеры
        mTitleField = v.findViewById(R.id.book_title);
        mPhotoView = v.findViewById(R.id.book_photo);
        mCameraButton = v.findViewById(R.id.book_camera);

        // Day 7, Шаг 7: Поле для ввода названия книги
        mTitleField.setText(mBook.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBook.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Day 15, Шаг 8: Настройка кнопки камеры
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверяем разрешение на камеру (для Android 6+)
                if (ContextCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    launchCamera();
                }
            }
        });

        // Day 15, Задание 1: Обработчик нажатия на фото для увеличения
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile != null && mPhotoFile.exists()) {
                    FragmentManager manager = getFragmentManager();
                    PhotoViewerFragment dialog = PhotoViewerFragment.newInstance(mPhotoFile.getPath());
                    dialog.show(manager, DIALOG_PHOTO_VIEWER);
                } else {
                    Toast.makeText(getActivity(), "Фото отсутствует", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Day 11, Шаг 8: Кнопка для выбора даты
        mDateButton = v.findViewById(R.id.book_date_button);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mBook.getDate());
                dialog.setTargetFragment(BookFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        // Day 7, Шаг 7: CheckBox для отметки "Прочитана"
        mReadedCheckBox = v.findViewById(R.id.book_readed);
        mReadedCheckBox.setChecked(mBook.isReaded());
        mReadedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBook.setReaded(isChecked);
            }
        });

        // Day 14, Шаг 5: Кнопка отправки отчёта
        mReportButton = v.findViewById(R.id.book_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBookReport();
            }
        });

        // Day 14, Задание 1: Кнопка поиска в браузере
        mSearchButton = v.findViewById(R.id.book_search);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBookInBrowser();
            }
        });

        // Day 15, Шаг 13: Обновляем отображение фотографии
        updatePhotoView();

        return v;
    }

    // Day 15, Шаг 14: Метод запроса разрешения на камеру
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            Toast.makeText(getActivity(), "Для фото обложки нужно разрешение на камеру", Toast.LENGTH_SHORT).show();
        }
        requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    // Day 15, Шаг 14: Обработка результата запроса разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(getActivity(), "Нет разрешения на использование камеры", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Day 15, Шаг 8-9: Метод запуска камеры
    private void launchCamera() {
        Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Проверяем, можно ли сделать фото
        PackageManager packageManager = getActivity().getPackageManager();
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;

        if (canTakePhoto) {
            Uri uri;
            if (Build.VERSION.SDK_INT >= 24) {
                // Android 7+ используем FileProvider
                uri = FileProvider.getUriForFile(getActivity(),
                        "ru.popov.bookdepository.provider", mPhotoFile);
            } else {
                // Android 6 и ниже
                uri = Uri.fromFile(mPhotoFile);
            }
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(captureImage, REQUEST_PHOTO);
        } else {
            Toast.makeText(getActivity(), "Не удалось открыть камеру", Toast.LENGTH_SHORT).show();
        }
    }

    // Day 15, Шаг 12: Обновление отображения фотографии
    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            // Масштабируем изображение под размер ImageView
            android.graphics.Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    // Day 11, Шаг 13: Обновление текста на кнопке с датой
    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        mDateButton.setText(sdf.format(mBook.getDate()));
    }

    // Day 14, Шаг 4: Метод формирования текста отчёта
    private String getBookReport() {
        // Определяем статус прочтения
        String readedString;
        if (mBook.isReaded()) {
            readedString = getString(R.string.book_report_readed);
        } else {
            readedString = getString(R.string.book_report_unreaded);
        }

        // Форматируем дату в удобочитаемый вид
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String dateString = df.format("dd.MM.yyyy", mBook.getDate()).toString();

        // Формируем отчёт с использованием форматной строки
        String report = getString(R.string.book_report,
                mBook.getTitle(), dateString, readedString);

        return report;
    }

    // Day 14, Шаг 5-6: Метод отправки отчёта через неявный интент
    private void sendBookReport() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getBookReport());
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.book_report_subject));

        PackageManager packageManager = getActivity().getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            Intent chooserIntent = Intent.createChooser(intent, getString(R.string.send_report));
            startActivity(chooserIntent);
        } else {
            Toast.makeText(getActivity(), "Нет приложений для отправки сообщений", Toast.LENGTH_SHORT).show();
        }
    }

    // Day 14, Задание 1: Метод поиска книги в браузере
    private void searchBookInBrowser() {
        String bookTitle = mBook.getTitle();
        if (bookTitle == null || bookTitle.isEmpty()) {
            Toast.makeText(getActivity(), "Сначала введите название книги", Toast.LENGTH_SHORT).show();
            return;
        }

        String searchQuery = "https://www.google.com/search?q=" + Uri.encode(bookTitle + " книга");
        Uri webPage = Uri.parse(searchQuery);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);

        PackageManager packageManager = getActivity().getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Нет браузера для открытия ссылки", Toast.LENGTH_SHORT).show();
        }
    }

    // Day 13, Шаг 7: Сохранение изменений в БД при уходе с экрана
    @Override
    public void onPause() {
        super.onPause();
        BookLab.get(getActivity()).updateBook(mBook);
    }

    // Day 11, Шаг 12: Получение результата из диалога выбора даты
    // Day 15, Шаг 13: Обработка результата от камеры
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mBook.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_PHOTO) {
            // Обновляем отображение фотографии после съёмки
            updatePhotoView();
        }
    }

    // Day 7, Шаг 11: Жизненный цикл для логирования
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach()");
    }
}