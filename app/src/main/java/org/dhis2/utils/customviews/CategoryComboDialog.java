package org.dhis2.utils.customviews;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;

import androidx.databinding.DataBindingUtil;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.CatComboDialogBinding;
import org.dhis2.databinding.CatComboDialogNewBinding;
import org.dhis2.databinding.CategorySelectorBinding;
import org.dhis2.utils.CatComboAdapter2;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.D2Manager;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QUADRAM. Created by frodriguez on 5/4/2018.
 */

public class CategoryComboDialog extends AlertDialog {

    private final List<CategoryOptionCombo> options;
    private final String catComboName;
    private final CategoryCombo categoryCombo;
    private final OnCatOptionComboSelected listenerNew;
    private D2 d2;
    private Context context;
    private AlertDialog dialog;
    private int requestCode;
    private OnCatOptionSelected listener;
    private String title;

    private Map<String, CategoryOption> selectedCatOption = new HashMap<>();

    public CategoryComboDialog(Context context, CategoryCombo categoryCombo, List<CategoryOptionCombo> categoryOptionCombos, int requestCode, OnCatOptionComboSelected listener, String title) {
        super(context);
        if (((App) context.getApplicationContext()).serverComponent() != null) {
            this.d2 = ((App) context.getApplicationContext()).serverComponent().userManager().getD2();
        }
        this.options = null;
        this.catComboName = categoryCombo.displayName();
        this.context = context;
        this.categoryCombo = categoryCombo;
        this.requestCode = requestCode;
        this.listener = null;
        this.listenerNew = listener;
        this.title = title;
    }

    @Override
    public void show() {
        if (categoryCombo == null)
            setLegacyDialog();
        else
            setDialog();

        dialog.show();
    }

    private void setDialog() {
        Builder builder = new Builder(context);
        CatComboDialogNewBinding binding = CatComboDialogNewBinding.inflate(LayoutInflater.from(context), null, false);
        builder.setCancelable(false);
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding.titleDialog.setText(categoryCombo.displayName());
        binding.categoryLayout.removeAllViews();
        for (Category category : categoryCombo.categories()) {
            CategorySelectorBinding catSelectorBinding = CategorySelectorBinding.inflate(LayoutInflater.from(context));
            catSelectorBinding.catCombLayout.setHint(category.displayName());
            catSelectorBinding.catCombo.setOnClickListener(
                    view ->
                            CategoryOptionPopUp.getInstance()
                                    .setCategory(category)
                                    .setOnClick(item -> {
                                        if (item != null)
                                            selectedCatOption.put(category.uid(), item);
                                        else
                                            selectedCatOption.remove(category.uid());
                                        catSelectorBinding.catCombo.setText(item != null ? item.displayName() : null);
                                        if (selectedCatOption.size() == categoryCombo.categories().size()) {
                                            listenerNew.onCatOptionComboSelected(getCatOptionCombo(new ArrayList<>(selectedCatOption.values())));
                                            dismiss();
                                        }
                                    })
                                    .show(context, catSelectorBinding.getRoot())
            );

            binding.categoryLayout.addView(catSelectorBinding.getRoot());
        }

    }

    private String getCatOptionCombo(List<CategoryOption> values) {
        CategoryOptionCombo catOptCombo =
                d2.categoryModule().categoryOptionCombos()
                        .byCategoryOptions(UidsHelper.getUidsList(values)).one().blockingGet();
        if (catOptCombo != null)
            return catOptCombo.uid();
        else
            return "";
    }

    private void setLegacyDialog() {
        Builder builder = new Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        CatComboDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.cat_combo_dialog, null, false);
        builder.setCancelable(false);
        builder.setView(binding.getRoot());

        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding.titleDialog.setText(title);

        CatComboAdapter2 adapter = new CatComboAdapter2(context,
                R.layout.spinner_layout,
                R.id.spinner_text,
                options,
                catComboName != null ? catComboName : context.getString(R.string.category_option));

        binding.catCombo.setAdapter(adapter);

        binding.catCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (options.size() > position - 1 && position > 0) {
                    listener.onCatOptionSelected(options.get(position - 1));
                    dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void dismiss() {
        if (dialog != null)
            dialog.dismiss();
    }

    public int getRequestCode() {
        return requestCode;
    }

    public interface OnCatOptionSelected {
        void onCatOptionSelected(CategoryOptionCombo selectedOption);
    }

    public interface OnCatOptionComboSelected {
        void onCatOptionComboSelected(String categoryOptionComboUid);
    }
}
