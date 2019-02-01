package org.dhis2.utils.custom_views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.dhis2.R;
import org.dhis2.databinding.FormBottomDialogBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

/**
 * QUADRAM. Created by ppajuelo on 17/01/2019.
 */
public class FormBottomDialog extends BottomSheetDialogFragment {
    OnFormBottomDialogItemSelection listener;
    private boolean canComplete = false;
    private boolean reopen = false;
    private boolean skip = false;
    private boolean reschedule = false;
    private boolean isEnrollmentOpen = true;
    private boolean accessDataWrite = true;
    private boolean hasExpired = false;

    public FormBottomDialog setAccessDataWrite(boolean canWrite) {
        this.accessDataWrite = canWrite;
        return this;
    }

    public FormBottomDialog setCanComplete(boolean canComplete) {
        this.canComplete = canComplete;
        return this;
    }

    public FormBottomDialog setReopen(boolean reopen) {
        this.reopen = reopen;
        return this;
    }

    public FormBottomDialog setSkip(boolean skip) {
        this.skip = skip;
        return this;
    }

    public FormBottomDialog setReschedule(boolean reschedule) {
        this.reschedule = reschedule;
        return this;
    }

    public FormBottomDialog setIsEnrollmentOpen(boolean isEnrollmentOpen) {
        this.isEnrollmentOpen = isEnrollmentOpen;
        return this;
    }

    public FormBottomDialog setIsExpired(boolean hasExpired) {
        this.hasExpired = hasExpired;
        return this;
    }

    public enum ActionType {
        FINISH_ADD_NEW,
        SKIP,
        REOPEN,
        RESCHEDULE,
        FINISH,
        COMPLETE_ADD_NEW,
        COMPLETE
    }

    public static FormBottomDialog getInstance() {
        return new FormBottomDialog();
    }

    public FormBottomDialog setListener(OnFormBottomDialogItemSelection listener) {
        this.listener = listener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FormBottomDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_bottom_dialog, container, false);
        binding.setCanWrite(accessDataWrite);
        binding.setIsEnrollmentOpen(isEnrollmentOpen);
        binding.setHasExpired(hasExpired);
        binding.setListener(listener);
        binding.setCanComplete(canComplete);
        binding.setReopen(reopen);
        binding.setSkip(skip);
        binding.setReschedule(reschedule);
        return binding.getRoot();
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if (listener == null)
            throw new IllegalArgumentException("Call this method after setting listener");
        super.show(manager, tag);
    }
}
