package com.dhis2.data.forms.dataentry.fields.old_fields.daterow;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;
import org.hisp.dhis.android.dataentry.form.section.viewmodels.date.DatePickerDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.processors.FlowableProcessor;

final class DateViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG_DATE_DIALOG_FRAGMENT = "tag:dateDialogFragment";

    @BindView(R.id.textview_row_label)
    TextView rowLabel;

    @BindView(R.id.recyclerview_row_filter_edittext)
    TextView rowFilter;

    @BindView(R.id.button_dropdown)
    ImageButton dropDownButton;

    @BindView(R.id.button_clear)
    ImageButton clearButton;

    @NonNull
    FlowableProcessor<RowAction> flowableProcessor;

    @NonNull
    FragmentManager fragmentManager;

    @NonNull
    SimpleDateFormat dateFormat;

    @Nullable
    DateViewModel viewModel;

    @SuppressWarnings("CheckReturnValue")
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    DateViewHolder(@NonNull FragmentManager manager,
                   @NonNull View itemView, @NonNull ViewGroup parent,
                   @NonNull FlowableProcessor<RowAction> processor) {
        super(itemView);

        dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.US);
        fragmentManager = manager;
        flowableProcessor = processor;
        ButterKnife.bind(this, itemView);

        RxView.clicks(dropDownButton)
                .takeUntil(RxView.detaches(parent))
                .filter(click -> viewModel != null)
                .subscribe(click -> showDialogFragment(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                });

        RxView.clicks(clearButton)
                .takeUntil(RxView.detaches(parent))
                .filter(click -> viewModel != null)
                .map(click -> RowAction.create(viewModel.uid(), null))
                .subscribe(action -> processor.onNext(action), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                });

        RxView.clicks(rowFilter)
                .takeUntil(RxView.detaches(parent))
                .filter(click -> viewModel != null)
                .subscribe(click -> showDialogFragment(), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                });
    }

    private void showDialogFragment() {
        DatePickerDialogFragment dialogFragment = DatePickerDialogFragment.create(true);
        dialogFragment.setFormattedOnDateSetListener(date ->
                flowableProcessor.onNext(RowAction.create(viewModel.uid(),
                        format(date))));
        dialogFragment.show(fragmentManager, TAG_DATE_DIALOG_FRAGMENT);
    }

    @NonNull
    String format(@NonNull Date date) {
        if (viewModel.isDateTime()) {
            return BaseIdentifiableObject.DATE_FORMAT.format(date);
        } else {
            return dateFormat.format(date);
        }
    }

    void update(@NonNull DateViewModel dateViewModel) {
        rowLabel.setText(dateViewModel.label());
        rowFilter.setText(dateViewModel.value());
        viewModel = dateViewModel;
    }
}
