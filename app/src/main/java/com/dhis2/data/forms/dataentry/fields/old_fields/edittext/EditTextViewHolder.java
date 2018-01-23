package com.dhis2.data.forms.dataentry.fields.old_fields.edittext;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.commons.tuples.Pair;
import org.hisp.dhis.android.dataentry.commons.utils.OnErrorHandler;
import org.hisp.dhis.android.dataentry.commons.utils.Preconditions;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.RowAction;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

import static org.hisp.dhis.android.dataentry.commons.utils.StringUtils.isEmpty;

final class EditTextViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.textview_row_label)
    TextView textViewLabel;

    @BindView(R.id.edittext_row_textinputlayout)
    TextInputLayout textInputLayout;

    @BindView(R.id.edittext_row_edittext)
    EditText editText;

    @NonNull
    BehaviorProcessor<EditTextModel> model;

    @SuppressWarnings("CheckReturnValue")
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    EditTextViewHolder(@NonNull ViewGroup parent, @NonNull View itemView,
                       @NonNull FlowableProcessor<RowAction> processor) {
        super(itemView);

        // bind views
        ButterKnife.bind(this, itemView);

        // source of data for this view
        model = BehaviorProcessor.create();
        model.subscribe(editTextModel -> {
            editText.setText(editTextModel.value() == null ?
                    null : valueOf(editTextModel.value()));

            if (!isEmpty(editTextModel.warning())) { // NOPMD
                textInputLayout.setError(editTextModel.warning());
                textInputLayout.setErrorTextAppearance(R.style.textInputLayoutWarningTextAppearance);
            } else if (!isEmpty(editTextModel.error())) { // NOPMD
                textInputLayout.setError(editTextModel.error());
                textInputLayout.setErrorTextAppearance(R.style.textInputLayoutErrorTextAppearance);
            } else {
                textInputLayout.setError(null);
            }

            editText.setInputType(editTextModel.inputType());
            editText.setMaxLines(editTextModel.maxLines());
            editText.setSelection(editText.getText() == null ?
                    0 : editText.getText().length());

            textViewLabel.setText(editTextModel.label());
            textInputLayout.setHint(isEmpty(editText.getText()) ? editTextModel.hint() : "");
        });

        // show and hide hint
        ConnectableObservable<Boolean> editTextObservable = RxView.focusChanges(editText)
                .takeUntil(RxView.detaches(parent))
                .publish();

        editTextObservable
                .map(hasFocus -> (hasFocus || isEmpty(editText.getText()))
                        && model.hasValue() ? model.getValue().hint() : "")
                .subscribe(hint -> textInputLayout.setHint(hint), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                });

        // persist value on focus change
        editTextObservable
                .scan(Pair.create(false, false), (state, hasFocus) ->
                        Pair.create(state.val1() && !hasFocus, hasFocus))
                .filter(state -> state.val0() && model.hasValue())
                .filter(valueHasChangedPredicate())
                .map(event -> RowAction.create(model.getValue().uid(),
                        editText.getText().toString()))
                .subscribe(action -> processor.onNext(action), OnErrorHandler.create(), () -> {
                    // this is necessary for persisting last value in the form
                    if (valueHasChanged()) {
                        processor.onNext(RowAction.create(model.getValue().uid(),
                                editText.getText().toString()));
                    }
                });

        editTextObservable.connect();
    }

    @NonNull
    private Predicate<Pair<Boolean, Boolean>> valueHasChangedPredicate() {
        return state -> valueHasChanged();
    }

    @NonNull
    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(editText.getText()) ? "" : editText.getText().toString(),
                model.getValue().value() == null ? "" : valueOf(model.getValue().value()));
    }

    void update(@NonNull EditTextModel editTextModel) {
        model.onNext(editTextModel);
    }
}