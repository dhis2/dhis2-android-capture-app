package com.dhis2.data.forms.dataentry.fields.old_fields.checkbox;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.commons.utils.Preconditions;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import rx.exceptions.OnErrorNotImplementedException;

final class CheckBoxViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.checkbox_row_checkbox)
    CheckBox checkBox;

    @BindView(R.id.textview_row_label)
    TextView textViewLabel;

    @NonNull
    BehaviorProcessor<CheckBoxViewModel> model;

    @SuppressWarnings("CheckReturnValue")
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    CheckBoxViewHolder(@NonNull ViewGroup parent, @NonNull View itemView,
                       @NonNull FlowableProcessor<RowAction> processor) {
        super(itemView);

        model = BehaviorProcessor.create();
        model.subscribe(checkBoxViewModel -> {
            textViewLabel.setText(checkBoxViewModel.label());
            checkBox.setChecked(CheckBoxViewModel.Value.CHECKED
                    .equals(checkBoxViewModel.value()));
        });

        ButterKnife.bind(this, itemView);

        RxView.clicks(itemView)
                .subscribe(o -> checkBox.setChecked(!checkBox.isChecked()));

        RxCompoundButton.checkedChanges(checkBox)
                .takeUntil(RxView.detaches(parent))
                .map(isChecked -> isChecked ? CheckBoxViewModel.Value.CHECKED :
                        CheckBoxViewModel.Value.UNCHECKED)
                .filter(value -> model.hasValue())
                .filter(value -> !Preconditions.equals(
                        model.getValue().value(), value))
                .map(value -> RowAction.create(model.getValue().uid(), value.toString()))
                .subscribe(t -> processor.onNext(t), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                });
    }

    void update(@NonNull CheckBoxViewModel checkBoxViewModel) {
        model.onNext(checkBoxViewModel);
    }
}