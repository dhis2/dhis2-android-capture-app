package org.dhis2.utils.filters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterStatusBinding;
import org.hisp.dhis.android.core.event.EventStatus;

public class StatusEventFilterHolder extends FilterHolder {

    StatusEventFilterHolder(@NonNull ItemFilterStatusBinding binding, ObservableField<Filters> openedFilter, ProgramType programType) {
        super(binding, openedFilter);
        filterType = Filters.EVENT_STATUS;
        this.programType = programType;
        filterTitle.setText(R.string.filters_title_event_status);
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_status));
        binding.setProgramType(programType);
    }
}
