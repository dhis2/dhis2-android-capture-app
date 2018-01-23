package com.dhis2.data.forms.dataentry.fields.old_fields.optionsrow;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.form.dataentry.DataEntryArguments;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;
import org.hisp.dhis.android.dataentry.selection.OptionSelectionArgument;
import org.hisp.dhis.android.dataentry.selection.SelectionDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.processors.FlowableProcessor;

final class OptionsViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG_SELECTION_DIALOG_FRAGMENT = "tag:selectionDialogFragment";
    @BindView(R.id.textview_row_label)
    TextView rowLabel;

    @BindView(R.id.recyclerview_row_filter_edittext)
    TextView rowFilter;

    @BindView(R.id.button_dropdown)
    ImageButton dropDownButton;

    @BindView(R.id.button_clear)
    ImageButton clearButton;

    @Nullable
    OptionsViewModel viewModel;

    @SuppressWarnings("CheckReturnValue")
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    OptionsViewHolder(@NonNull FragmentManager manager,
                      @NonNull View itemView, @NonNull ViewGroup parent,
                      @NonNull FlowableProcessor<RowAction> processor,
                      @NonNull DataEntryArguments entryArguments) {
        super(itemView);

        ButterKnife.bind(this, itemView);
        RxView.clicks(dropDownButton)
                .takeUntil(RxView.detaches(parent))
                .filter(click -> viewModel != null)
                .subscribe(click -> showDialogFragment(manager, entryArguments), throwable -> {
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
                .subscribe(click -> showDialogFragment(manager, entryArguments), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                });
    }

    private void showDialogFragment(@NonNull FragmentManager manager,
            @NonNull DataEntryArguments arguments) {
        OptionSelectionArgument argument = OptionSelectionArgument.create(arguments,
                viewModel.optionSet(), viewModel.uid(), viewModel.label());
        SelectionDialogFragment.create(argument)
                .show(manager, TAG_SELECTION_DIALOG_FRAGMENT);
    }

    void update(@NonNull OptionsViewModel optionsViewModel) {
        rowLabel.setText(optionsViewModel.label());
        rowFilter.setText(optionsViewModel.value());
        viewModel = optionsViewModel;
    }
}
