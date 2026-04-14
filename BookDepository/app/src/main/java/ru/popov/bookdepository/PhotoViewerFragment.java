package ru.popov.bookdepository;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

public class PhotoViewerFragment extends DialogFragment {

    private static final String ARG_PHOTO_PATH = "photo_path";
    private ImageView mPhotoView;
    private String mPhotoPath;
    public static PhotoViewerFragment newInstance(String photoPath) {
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_PATH, photoPath);
        PhotoViewerFragment fragment = new PhotoViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoPath = getArguments().getString(ARG_PHOTO_PATH);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_photo_viewer, null);

        mPhotoView = v.findViewById(R.id.photo_fullscreen);
        if (mPhotoPath != null && !mPhotoPath.isEmpty()) {
            File photoFile = new File(mPhotoPath);
            if (photoFile.exists()) {
                Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoPath, getActivity());
                mPhotoView.setImageBitmap(bitmap);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);

        builder.setPositiveButton("Закрыть", (dialog, which) -> dismiss());

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
    }
}