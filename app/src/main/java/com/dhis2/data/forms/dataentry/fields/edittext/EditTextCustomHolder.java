package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.tuples.Pair;
import com.dhis2.utils.OnErrorHandler;
import com.dhis2.utils.Preconditions;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.hisp.dhis.android.core.common.ValueType;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018..
 */

final class EditTextCustomHolder extends RecyclerView.ViewHolder {

    private final TextInputLayout inputLayout;
    private EditText editText;
    @NonNull
    private BehaviorProcessor<EditTextModel> model;

    private CompositeDisposable disposable;

    EditTextCustomHolder(ViewGroup parent, ViewDataBinding binding, FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        super(binding.getRoot());

        editText = binding.getRoot().findViewById(R.id.input_editText);
        inputLayout = binding.getRoot().findViewById(R.id.input_layout);

        this.disposable = new CompositeDisposable();

        model = BehaviorProcessor.create();
        disposable.add(model.subscribe(editTextModel -> {

                    editText.setText(editTextModel.value() == null ?
                            null : valueOf(editTextModel.value()));

                    setInputType(editTextModel.valueType());


                    if (!isEmpty(editTextModel.warning())) {
                        inputLayout.setError(editTextModel.warning());
                    } else if (!isEmpty(editTextModel.error())) {
                        inputLayout.setError(editTextModel.error());
                    } else
                        inputLayout.setError(null);


                    editText.setSelection(editText.getText() == null ?
                            0 : editText.getText().length());
                    if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(editTextModel.label()))
                        inputLayout.setHint(editTextModel.label());

                }
                , t -> Log.d("DHIS_ERROR", t.getMessage())));


        // show and hide hint
        ConnectableObservable<Boolean> editTextObservable = RxView.focusChanges(editText)
                .takeUntil(RxView.detaches(parent))
                .publish();
/*
        disposable.add(editTextObservable
                .map(hasFocus -> (hasFocus || isEmpty(editText.getText()))
                        && model.hasValue() ? model.getValue().label() : "")
                .subscribe(
                        hint -> inputLayout.setHint(hint),
                        Timber::d));*/

        disposable.add(RxTextView.textChanges(editText)
                .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .filter(data -> model.getValue() != null)
                .map(text -> RowAction.create(model.getValue().uid(), text.toString()))
                .subscribe(processor::onNext,
                        OnErrorHandler.create(), () ->
                        {
                            if (valueHasChanged()) {
                                processor.onNext(RowAction.create(model.getValue().uid(),
                                        editText.getText().toString()));
                            }
                        }));

        editTextObservable.connect();
    }

    private void setInputType(ValueType valueType) {
        switch (valueType) {
            case PHONE_NUMBER:
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case EMAIL:
                editText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case TEXT:
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setLines(1);
                editText.setEllipsize(TextUtils.TruncateAt.END);
                break;
            case LETTER:
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                break;
            case NUMBER:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                break;
            case INTEGER_NEGATIVE:
            case INTEGER:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                break;
            case INTEGER_ZERO_OR_POSITIVE:
            case INTEGER_POSITIVE:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setKeyListener(DigitsKeyListener.getInstance(false, false));
                break;
            case UNIT_INTERVAL:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            default:
                break;
        }
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
