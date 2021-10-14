package org.dhis2.data.forms.dataentry.tablefields;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.dhis2.R;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.utils.Constants;

/**
 * QUADRAM. Created by ppajuelo on 06/11/2017.
 */

public abstract class FormViewHolder extends AbstractViewHolder {

    public ViewDataBinding binding;
    public ImageView description;
    public StringBuilder label;
    public String descriptionText;
    public TextView textView;
    public boolean accessDataWrite;
    public FieldViewModel fieldViewModel;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        label = new StringBuilder();
        this.binding = binding;
        this.description = binding.getRoot().findViewById(R.id.descriptionLabel);
        if (description != null) {
            description.setOnClickListener(v ->
                    new CustomDialog(
                            itemView.getContext(),
                            label.toString(),
                            descriptionText != null ? descriptionText : itemView.getContext().getString(R.string.empty_description),
                            itemView.getContext().getString(R.string.action_close),
                            null,
                            Constants.DESCRIPTION_DIALOG,
                            null
                    ).show());
        }
    }

    public abstract void dispose();

    public void openKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void update(FieldViewModel fieldViewModel) {
        this.fieldViewModel = fieldViewModel;
    }

    @Override
    public void setBackgroundColor(int p_nColor) {
        super.setBackgroundColor(p_nColor);
        setBackground();
    }

    public void setBackground() {

        if(canBeEdited() && isSelected()){
            setSelectedEditableColors();
        }else if(canBeEdited()){
            setNoSelectedEditableColors();
        }else if(isSelected()){
            setSelectedNoEditableColors();
        }else{
            setNoSelectedEditableColors();
        }
    }

    public boolean canBeEdited() {
        return accessDataWrite && (fieldViewModel == null || fieldViewModel.editable());
    }

    public void setTextColor(@ColorInt int color) {
        if (textView != null) textView.setTextColor(color);
    }

    private void setSelectedEditableColors(){
        int bgColor = ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary);
        int textColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
        itemView.setBackgroundColor(bgColor);
        setTextColor(textColor);
    }

    private void setNoSelectedEditableColors(){
        int bgColor = android.R.color.transparent;
        itemView.setBackgroundResource(bgColor);
        int textColor = ContextCompat.getColor(itemView.getContext(), R.color.textPrimary);
        setTextColor(textColor);
    }

    private void setSelectedNoEditableColors(){
        int bgColor = ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary);
        int textColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
        int noEditTextColor = ColorUtils.withAlpha(textColor, 50);
        itemView.setBackgroundColor(bgColor);
        setTextColor(noEditTextColor);
    }

    private void setNoSelectedNoEditableColors(){
        int bgColor = ContextCompat.getColor(itemView.getContext(), R.color.gray_9b9);
        int noEditBgColor = ColorUtils.withAlpha(bgColor, 50);
        int textColor = ContextCompat.getColor(itemView.getContext(), R.color.textPrimary);
        int noEditTextColor = ColorUtils.withAlpha(textColor, 100);
        itemView.setBackgroundColor(noEditBgColor);
        setTextColor(noEditTextColor);
    }
}
