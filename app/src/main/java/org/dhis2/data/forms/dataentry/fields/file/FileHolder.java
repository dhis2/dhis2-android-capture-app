package org.dhis2.data.forms.dataentry.fields.file;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.databinding.FormButtonBinding;

import static android.view.View.FOCUS_DOWN;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class FileHolder extends FormViewHolder {

    public FileHolder(FormButtonBinding binding, MutableLiveData<String> currentSelection) {
        super(binding);
        currentUid = currentSelection;
        Button button = binding.formButton;
        button.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                View nextView;
                if ((nextView = v.focusSearch(FOCUS_DOWN)) != null)
                    nextView.requestFocus();
            }else
                itemView.setBackgroundColor(Color.WHITE);

        });
    }
}
