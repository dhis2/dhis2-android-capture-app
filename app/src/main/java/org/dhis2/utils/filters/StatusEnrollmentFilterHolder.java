package org.dhis2.utils.filters;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterEnrollmentStatusBinding;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

public class StatusEnrollmentFilterHolder extends FilterHolder {

    StatusEnrollmentFilterHolder(@NonNull ItemFilterEnrollmentStatusBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem, ProgramType programType) {
        super(binding, openedFilter, sortingItem);
        filterType = Filters.ENROLLMENT_STATUS;
        this.programType = programType;
        filterTitle.setText(R.string.filters_title_enrollment_status);
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_enrollment_status_filter));
    }
}
