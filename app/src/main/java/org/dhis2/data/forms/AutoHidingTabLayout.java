package org.dhis2.data.forms;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * A TabLayout that automatically hides when the attached adapter has less than 2 elements
 */
public class AutoHidingTabLayout extends TabLayout {
    private ViewPager mViewPager;

    public AutoHidingTabLayout(Context context) {
        super(context);
    }

    public AutoHidingTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        if (viewPager != null && viewPager.getAdapter() != null) {
            mViewPager = viewPager;
            AdapterChangeObserver adapterChangeObserver = new AdapterChangeObserver();
            viewPager.getAdapter().registerDataSetObserver(adapterChangeObserver);
            viewPager.addOnAdapterChangeListener((pager, oldAdapter, newAdapter) -> {
                if (oldAdapter != null) {
                    oldAdapter.unregisterDataSetObserver(adapterChangeObserver);
                }
                if (newAdapter != null) {
                    newAdapter.registerDataSetObserver(adapterChangeObserver);
                }
            });

            toggleVisibility(viewPager.getAdapter());
        }
        super.setupWithViewPager(viewPager);
    }

    private void toggleVisibility(@NonNull PagerAdapter adapter) {
        if (adapter.getCount() < 2) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    private class AdapterChangeObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            if (mViewPager == null || mViewPager.getAdapter() == null) {
                setVisibility(GONE);
            } else {
                toggleVisibility(mViewPager.getAdapter());
            }
        }
    }
}