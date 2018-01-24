package com.dhis2.data.forms.dataentry.fields.old_fields.radiobutton;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxRadioGroup;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.commons.utils.Preconditions;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import rx.exceptions.OnErrorNotImplementedException;

final class RadioButtonViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.textview_row_label)
    TextView label;

    @BindView(R.id.radiogroup_radiobutton_row)
    RadioGroup radioGroup;

    @BindView(R.id.radiobutton_row_radiobutton_first)
    RadioButton firstRadioButton;

    @BindView(R.id.radiobutton_row_radiobutton_second)
    RadioButton secondRadioButton;

    @BindView(R.id.radiobutton_row_radiobutton_third)
    RadioButton thirdRadioButton;

    @NonNull
    BehaviorProcessor<RadioButtonViewModel> model;

    @SuppressWarnings("CheckReturnValue")
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    RadioButtonViewHolder(@NonNull ViewGroup parent, @NonNull View itemView,
                          @NonNull FlowableProcessor<RowAction> processor) {
        super(itemView);

        model = BehaviorProcessor.create();
        model.subscribe(radioButtonViewModel -> {
            label.setText(radioButtonViewModel.label());

            if (radioButtonViewModel.value() == null) {
                radioGroup.clearCheck();
            } else {
                firstRadioButton.setChecked(RadioButtonViewModel.Value
                        .YES.equals(radioButtonViewModel.value()));
                secondRadioButton.setChecked(RadioButtonViewModel.Value
                        .NO.equals(radioButtonViewModel.value()));
                thirdRadioButton.setChecked(RadioButtonViewModel.Value
                        .NONE.equals(radioButtonViewModel.value()));
            }
        });

        ButterKnife.bind(this, itemView);
        RxRadioGroup.checkedChanges(radioGroup)
                .takeUntil(RxView.detaches(parent))
                .map(this::mapValue)
                .filter(value -> model.hasValue())
                .filter(value -> !Preconditions.equals(model.getValue().value(), value))
                .map(value -> RowAction.create(model.getValue().uid(), value.toString()))
                .subscribe(rowAction -> processor.onNext(rowAction), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                });
    }

    void update(@NonNull RadioButtonViewModel radioButtonViewModel) {
        model.onNext(radioButtonViewModel);
    }

    @NonNull
    private RadioButtonViewModel.Value mapValue(@NonNull Integer checkedId) {
        if (checkedId == R.id.radiobutton_row_radiobutton_first) {
            return RadioButtonViewModel.Value.YES;
        } else if (checkedId == R.id.radiobutton_row_radiobutton_second) {
            return RadioButtonViewModel.Value.NO;
        } else {
            return RadioButtonViewModel.Value.NONE;
        }
    }
}
