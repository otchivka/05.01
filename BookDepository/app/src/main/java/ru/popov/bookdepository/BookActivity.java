package ru.popov.bookdepository;

import androidx.fragment.app.Fragment;

public class BookActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new BookFragment();
    }
}