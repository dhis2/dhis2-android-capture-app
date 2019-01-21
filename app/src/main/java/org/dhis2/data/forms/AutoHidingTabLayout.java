package org.dhis2.data.forms;

import android.content.Context;
import android.database.DataSetObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;

import static org.dhis2.utils.Preconditions.isNull;

/**
 * A TabLayout that automatically hides when the attached adapter has less than 2 elements
 */
public class AutoHidingTabLayout extends TabLayout {
    private ViewPager viewPager;

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
        this.viewPager = isNull(viewPager, "viewPager == null");
        isNull(viewPager.getAdapter(), "viewPager.getAdapter == null. You must set " +
                "an adapter on the ViewPager before setting up the AutoHidingTabLayout");

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
            if (viewPager == null || viewPager.getAdapter() == null) {
                setVisibility(GONE);
            } else {
                toggleVisibility(viewPager.getAdapter());
            }
        }
    }
}