package org.dhis2.usescases.syncManager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.gson.Gson;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ErrorDialogBinding;
import org.dhis2.utils.ErrorMessageModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 5/4/2018.
 */

public class ErrorDialog extends DialogFragment {

    private static ErrorDialog instace;
    private String title;
    private List<ErrorMessageModel> data;
    private DividerItemDecoration divider;
    public static String TAG = "FullScreenDialog";
    private String shareTitle;
    private String shareMessageTitle;
    private ObservableBoolean sharing = new ObservableBoolean(false);
    private CompositeDisposable disposable;
    private ObservableArrayList<ErrorMessageModel> shareData;

    public static ErrorDialog newInstace() {
        if (instace == null) {
            instace = new ErrorDialog();
        }
        return instace;
    }

    public ErrorDialog setData(List<ErrorMessageModel> data) {
        this.data = data;
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.title = context.getString(R.string.error_dialog_title);
        this.shareTitle = context.getString(R.string.share_with);
        this.shareMessageTitle = context.getString(R.string.sync_error_title);
        this.divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        this.shareData = new ObservableArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FulLScreenDialogStyle);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ErrorDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.error_dialog, container, false);

        binding.titleDialog.setText(title);
        ErrorAdapter errorAdapter = new ErrorAdapter(data, sharing);
        binding.errorRecycler.setAdapter(errorAdapter);
        binding.errorRecycler.addItemDecoration(divider);
        binding.possitive.setOnClickListener(view -> {
            if (sharing.get()) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, new Gson().toJson(data));
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, shareMessageTitle);
                sendIntent.setType("text/plain");
                startActivity((Intent.createChooser(sendIntent, shareTitle)));
            } else
                dismiss();
        });
        binding.setSharing(sharing);
        binding.setShareList(shareData);
        binding.shareButton.setOnClickListener(view -> sharing.set(!sharing.get()));
        subscribeToErrors(errorAdapter.asFlowable());
        return binding.getRoot();
    }

    private void subscribeToErrors(FlowableProcessor<Pair<Boolean, ErrorMessageModel>> pairFlowableProcessor) {
        if (disposable == null)
            disposable = new CompositeDisposable();
        disposable.add(pairFlowableProcessor
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> {
                            if (pair.val0())
                                shareData.add(pair.val1());
                            else
                                shareData.remove(pair.val1());
                        },
                        Timber::e
                ));
    }

    @Override
    public void dismiss() {
        instace = null;
        super.dismiss();
    }

}
