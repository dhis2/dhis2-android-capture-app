package org.dhis2.utils.customviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.databinding.FormBottomDialogBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * QUADRAM. Created by ppajuelo on 17/01/2019.
 */
public class FormBottomDialog extends BottomSheetDialogFragment {
    OnFormBottomDialogItemSelection listener;
    private FormBottomDialogBinding binding;
    private boolean canComplete = false;
    private boolean reopen = false;
    private boolean skip = false;
    private boolean reschedule = false;
    private boolean isEnrollmentOpen = true;
    private boolean accessDataWrite = true;
    private boolean hasExpired = false;
    private boolean mandatoryFields = false;
    private String messageOnComplete = null;
    private Boolean fieldsWithErrors = false;
    private Map<String, FieldViewModel> emptyMandatoryFields = new HashMap<>();

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

    public FormBottomDialog setMandatoryFields(boolean mandatoryFields) {
        this.mandatoryFields = mandatoryFields;
        return this;
    }

    public FormBottomDialog setFieldsWithErrors(boolean fieldsWithErrors) {
        this.fieldsWithErrors = fieldsWithErrors;
        return this;
    }

    public FormBottomDialog setMessageOnComplete(String messageOnComplete) {
        this.messageOnComplete = messageOnComplete;
        return this;
    }

    public FormBottomDialog setEmptyMandatoryFields(Map<String, FieldViewModel> emptyMandatoryFields){
        this.emptyMandatoryFields = emptyMandatoryFields;
        return this;
    }

    public enum ActionType {
        FINISH_ADD_NEW,
        SKIP,
        REOPEN,
        RESCHEDULE,
        FINISH,
        COMPLETE_ADD_NEW,
        COMPLETE,
        CHECK_FIELDS
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
        binding = DataBindingUtil.inflate(inflater, R.layout.form_bottom_dialog, container, false);
        binding.setCanWrite(accessDataWrite);
        binding.setIsEnrollmentOpen(isEnrollmentOpen);
        binding.setHasExpired(hasExpired);
        binding.setListener(actionType -> {
            listener.onActionSelected(actionType);
            dismiss();
        });
        binding.setCanComplete(canComplete);
        binding.setReopen(reopen);
        binding.setSkip(skip);
        binding.setReschedule(reschedule);
        binding.setMandatoryFields(mandatoryFields);
        binding.setFieldsWithErrors(fieldsWithErrors);
        binding.setMessageOnComplete(messageOnComplete);

        showMissingMandatoryFields();
        return binding.getRoot();
    }

    //This is necessary to show the bottomSheet dialog with full height on landscape
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(0);
        });
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if (listener == null)
            throw new IllegalArgumentException("Call this method after setting listener");
        super.show(manager, tag);
    }

    private void showMissingMandatoryFields(){
        if(mandatoryFields){
            String fields = "";
            for(Map.Entry<String, FieldViewModel> viewModel: emptyMandatoryFields.entrySet()){
                fields = fields + "\n " + viewModel.getValue().label();
            }
            String textMandatory = binding.txtMandatoryFields.getText().toString() + "\n " + fields;

            binding.txtMandatoryFields.setText(textMandatory);
        }
    }
}
