package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dhis2.data.forms.dataentry.DataEntryArguments;
import com.dhis2.data.forms.dataentry.DataEntryFragment;

import java.util.ArrayList;
import java.util.List;

class EventInfoSectionsViewPagerAdapter extends FragmentStatePagerAdapter {

    @NonNull
    private final List<EventInfoSectionsViewModel> eventInfoSectionsViewModelList;

    EventInfoSectionsViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.eventInfoSectionsViewModelList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        EventInfoSectionsViewModel viewModel = eventInfoSectionsViewModelList.get(position);
        return DataEntryFragment.create(DataEntryArguments.forEventSection(viewModel.uid(), viewModel.sectionUid()));
    }

    @Override
    public int getCount() {
        return eventInfoSectionsViewModelList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position + " of " + eventInfoSectionsViewModelList.size() + " " + eventInfoSectionsViewModelList.get(position).sectionName();
    }

    void swapData(List<EventInfoSectionsViewModel> models) {
        eventInfoSectionsViewModelList.clear();
        eventInfoSectionsViewModelList.addAll(models);
        notifyDataSetChanged();
    }
}