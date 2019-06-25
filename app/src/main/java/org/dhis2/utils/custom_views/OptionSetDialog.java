package org.dhis2.utils.custom_views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.DialogOptionSetBinding;
import org.dhis2.utils.EndlessRecyclerViewScrollListener;
import org.hisp.dhis.android.core.option.OptionModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class OptionSetDialog extends DialogFragment {

    private static OptionSetDialog instace;
    private DialogOptionSetBinding binding;
    private CompositeDisposable disposable;
    //1st param is text to search, 2nd param is uid of optionSet,3rd param is page
    private FlowableProcessor<Trio<String, String, Integer>> processor;
    private OptionSetAdapter adapter;
    private SpinnerViewModel optionSet;

    private OptionSetOnClickListener listener;
    private View.OnClickListener cancelListener;
    private View.OnClickListener clearListener;

    private EndlessRecyclerViewScrollListener endlessScrollListener;

    public static OptionSetDialog newInstance() {
        if (instace == null) {
            instace = new OptionSetDialog();
        }
        return instace;
    }

    public static Boolean isCreated() {
        return instace != null;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        instace = null;
        disposable.clear();
        super.onCancel(dialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_option_set, container, false);


        binding.title.setText(optionSet.label());
        disposable = new CompositeDisposable();

        endlessScrollListener = new EndlessRecyclerViewScrollListener(binding.recycler.getLayoutManager(), 2, 0) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                processor.onNext(Trio.Companion.create(binding.txtSearch.getText().toString(), optionSet.optionSet(), page));
            }
        };
        binding.recycler.addOnScrollListener(endlessScrollListener);
        adapter = new OptionSetAdapter(listener);
        adapter.setOptions(new ArrayList<>(), endlessScrollListener.getCurrentPage());
        binding.recycler.setAdapter(adapter);

        processor.onNext(Trio.Companion.create("", optionSet.optionSet(), 0));

        binding.clearButton.setOnClickListener(clearListener);
        binding.cancelButton.setOnClickListener(cancelListener);
        disposable.add(RxTextView.textChanges(binding.txtSearch)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            endlessScrollListener.resetState();
                            processor.onNext(Trio.Companion.create(data.toString(), optionSet.optionSet(), 0));
                        },
                        Timber::e
                ));

        return binding.getRoot();

    }

    @Override
    public void dismiss() {
        instace = null;
        disposable.clear();
        super.dismiss();
    }

    public OptionSetDialog setOnClick(OptionSetOnClickListener listener) {
        this.listener = listener;
        return this;
    }

    public OptionSetDialog setOptions(List<OptionModel> options) {
        if (options.isEmpty() && adapter.isLastItemLoading(adapter.getItemCount())) {
            adapter.remove();
        } else {
            adapter.setOptions(options, endlessScrollListener.getCurrentPage());
        }
        return this;
    }

    public OptionSetDialog setOptionSetUid(SpinnerViewModel view) {
        this.optionSet = view;
        return this;
    }

    public OptionSetDialog setProcessor(FlowableProcessor<Trio<String, String, Integer>> processor) {
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
