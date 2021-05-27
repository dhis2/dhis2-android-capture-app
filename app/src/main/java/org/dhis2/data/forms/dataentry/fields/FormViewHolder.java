package org.dhis2.data.forms.dataentry.fields;

import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import org.dhis2.BR;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.FormIntent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class FormViewHolder extends RecyclerView.ViewHolder {

    private final ViewDataBinding binding;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        ImageView fieldSelected = binding.getRoot().findViewById(R.id.fieldSelected);
        if (fieldSelected != null) {
            ViewExtensionsKt.clipWithAllRoundedCorners(fieldSelected, ExtensionsKt.getDp(2));
        }
    }

    public void bind(FieldUiModel uiModel, FieldItemCallback callback) {
        FieldUiModel.Callback itemCallback = new FieldUiModel.Callback() {
            /*    @Override
                public void showYearMonthDayPicker(String uid, int year, int month, int day) {
                    callback.showYearMonthDayPicker(uid, year, month, day);
                }

                @Override
                public void showCustomCalendar(String uid, String label, Date date) {
                    callback.showCustomCalendar(uid, label, date);
                } */
            @Override
            public void intent(@NotNull FormIntent intent) {
                callback.intent(intent);
            }

            @Override
            public void onNext() {
                callback.onNext(getLayoutPosition());
            }

            @Override
            public void showDialog(@NotNull String title, @Nullable String message) {
                callback.onShowDialog(title, message);
            }

            @Override
            public void onItemAction(@NotNull RowAction action) {
                callback.onAction(action);
            }
        };
        uiModel.setCallback(itemCallback);

        binding.setVariable(BR.item, uiModel);
        binding.executePendingBindings();
    }

    public interface FieldItemCallback {
        void onShowDialog(String title, @Nullable String message);

        void intent(@NotNull FormIntent intent);

    //    void showCustomCalendar(String uid, String label, Date date);

    //    void showYearMonthDayPicker(String uid, int year, int month, int day);

        void onNext(int layoutPosition);

        void onAction(RowAction action);
    }
}
