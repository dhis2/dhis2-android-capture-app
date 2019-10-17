package org.dhis2.utils.customviews;

import android.app.Dialog;
import android.content.Context;
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
import androidx.fragment.app.FragmentManager;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.databinding.DialogOptionSetBinding;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.option.OptionCollectionRepository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

public class OptionSetDialog extends DialogFragment {

    public static final String TAG = OptionSetDialog.class.getName();
    private final List<String> optionsToHide;
    private final List<String> optionGroupsToHide;
    private final List<String> optionGroupsToShow;
    private CompositeDisposable disposable;
    private OptionSetAdapter adapter;
    private SpinnerViewModel optionSet;

    private OptionSetOnClickListener listener;
    private View.OnClickListener clearListener;
    private D2 d2;

    private boolean isDialogShown = false;

    public OptionSetDialog(SpinnerViewModel view, OptionSetOnClickListener optionSetListener,
                           View.OnClickListener clearListener) {
        this.optionSet = view;
        this.listener = optionSetListener;
        this.clearListener = clearListener;
        this.optionsToHide = view.getOptionsToHide() != null ? view.getOptionsToHide() : new ArrayList<>();
        this.optionGroupsToHide = view.getOptionGroupsToHide() != null ? view.getOptionGroupsToHide() : new ArrayList<>();
        this.optionGroupsToShow = view.getOptionGroupsToShow() != null ? view.getOptionGroupsToShow() : new ArrayList<>();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        d2 = ((App) context.getApplicationContext()).serverComponent().userManager().getD2();
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
        disposable.clear();
        super.onCancel(dialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        DialogOptionSetBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_option_set, container, false);
        binding.title.setText(optionSet.label());

        disposable = new CompositeDisposable();
        adapter = new OptionSetAdapter((option) -> {
            listener.onSelectOption(option);
            this.dismiss();
        });
        binding.recycler.setAdapter(adapter);

        binding.clearButton.setOnClickListener((view) -> {
            clearListener.onClick(view);
            this.dismiss();
        });
        binding.cancelButton.setOnClickListener(view ->
                this.dismiss());

        disposable.add(RxTextView.textChanges(binding.txtSearch)
                .startWith("")
                .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
                .map(textToSearch ->
                {
                    OptionCollectionRepository optionRepository = d2.optionModule().options
                            .byOptionSetUid().eq(optionSet.optionSet());

                    List<String> finalOptionsToHide = new ArrayList<>();
                    List<String> finalOptionsToShow = new ArrayList<>();

                    if (!optionsToHide.isEmpty())
                        finalOptionsToHide.addAll(optionsToHide);

                    if(!optionGroupsToShow.isEmpty()){
                        for(String groupUid: optionGroupsToShow){
                            finalOptionsToShow.addAll(
                              UidsHelper.getUidsList(d2.optionModule().optionGroups.withOptions().uid(groupUid).blockingGet().options())
                            );
                        }
                    }

                    if (!optionGroupsToHide.isEmpty()) {
                        for (String groupUid : optionGroupsToHide) {
                            finalOptionsToHide.addAll(
                                    UidsHelper.getUidsList(d2.optionModule().optionGroups.withOptions().uid(groupUid).blockingGet().options())
                            );
                        }
                    }

                    if(!finalOptionsToShow.isEmpty())
                        optionRepository = optionRepository
                                .byUid().in(finalOptionsToShow);

                    if (!finalOptionsToHide.isEmpty())
                        optionRepository = optionRepository
                                .byUid().notIn(finalOptionsToHide);

                    if (!isEmpty(textToSearch))
                        optionRepository = optionRepository
                                .byDisplayName().like("%" + textToSearch + "%");
                    return optionRepository.getPaged(20);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(options ->
                                options.observe(this, optionList ->
                                        adapter.submitList(optionList)),
                        Timber::e
                ));

        return binding.getRoot();

    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        isDialogShown = true;
        super.show(manager, tag);
    }

    @Override
    public void dismiss() {
        disposable.clear();
        isDialogShown = false;
        super.dismiss();
    }

    public boolean isDialogShown() { return isDialogShown; }

    public interface OnOptionSetDialogButtonClickListener {
        void onCancelClick();

        void onClearClick();

        void onOptionSelected(Option option);
    }
}
