package ru.popov.bookdepository;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;
import java.util.UUID;

/**
 * ДЕНЬ 10: Активность с ViewPager для пролистывания книг
 * Заменяет BookActivity
 */
public class BookPagerActivity extends FragmentActivity {


    private static final String EXTRA_BOOK_ID = "ru.popov.bookdepository.book_id";

    private ViewPager mViewPager;
    private List<Book> mBooks;
    private UUID mCurrentBookId;

    public static Intent newIntent(Context packageContext, UUID bookId) {
        Intent intent = new Intent(packageContext, BookPagerActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_pager);

        mCurrentBookId = (UUID) getIntent().getSerializableExtra(EXTRA_BOOK_ID);

        mBooks = BookLab.get(this).getBooks();

        mViewPager = findViewById(R.id.activity_book_pager_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();


        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                Book book = mBooks.get(position);

                return BookFragment.newInstance(book.getId());
            }

            @Override
            public int getCount() {
                return mBooks.size();
            }
        });


        for (int i = 0; i < mBooks.size(); i++) {
            if (mBooks.get(i).getId().equals(mCurrentBookId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
}