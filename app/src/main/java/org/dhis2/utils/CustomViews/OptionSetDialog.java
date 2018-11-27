package org.dhis2.utils.CustomViews;

import android.app.Dialog;
import android.arch.lifecycle.ViewModel;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.databinding.DialogOptionSetBinding;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class OptionSetDialog extends DialogFragment {

    static OptionSetDialog instace;
    private DialogOptionSetBinding binding;
    private CompositeDisposable disposable;
    //1º param is text to search, 2º param is uid of optionSet
    private FlowableProcessor<Pair<String,String>> processor;
    private OptionSetAdapter adapter;
    private SpinnerViewModel optionSet;

    private OptionSetOnClickListener listener;
    private View.OnClickListener cancelListener;
    private View.OnClickListener clearListener;
    public static OptionSetDialog newInstance() {
        if (instace == null) {
            instace = new OptionSetDialog();
        }
        return instace;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_option_set, container, false);
        adapter = new OptionSetAdapter(listener);
        binding.recycler.setAdapter(adapter);
        binding.title.setText(optionSet.description());
        disposable = new CompositeDisposable();

        processor.onNext( Pair.create("", optionSet.optionSet()));

        binding.clearButton.setOnClickListener(clearListener);
        binding.cancelButton.setOnClickListener(cancelListener);
        disposable.add(RxTextView.textChanges(binding.txtSearch)
                .skipInitialValue()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> startProcessor( Pair.create(data.toString(), optionSet.optionSet())),
                        Timber::e
                ));

        return binding.getRoot();

    }

    public OptionSetDialog setOnClick(OptionSetOnClickListener listener){
        this.listener = listener;
        return this;
    }

    private OptionSetDialog startProcessor(Pair<String,String> text){
        if(processor != null)
            processor.onNext(text);
        return this;
    }

    public FlowableProcessor<Pair<String,String>> getProcessor() {
        if(processor == null)
            processor = PublishProcessor.create();

        return processor;
    }

    public OptionSetDialog setOptions(List<String> options){
        adapter.setOptions(options);
        return this;
    }

    public void setOptionSetUid(SpinnerViewModel view){
        this.optionSet = view;
    }

    public OptionSetDialog setProcessor(FlowableProcessor<Pair<String, String>> processor) {
        this.processor = processor;
        return this;
    }

    public OptionSetDialog setCancelListener(View.OnClickListener cancelListener) {
        this.cancelListener = cancelListener;
        return this;
    }

    public OptionSetDialog setClearListener(View.OnClickListener clearListener) {
        this.clearListener = clearListener;
        return this;
    }
}
