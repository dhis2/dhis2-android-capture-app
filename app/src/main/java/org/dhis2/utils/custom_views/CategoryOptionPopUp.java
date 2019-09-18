package org.dhis2.utils.custom_views;

import android.content.Context;
import android.view.Menu;
import android.view.View;

import org.dhis2.R;
import org.dhis2.utils.DialogClickListener;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.PopupMenu;

/**
 * QUADRAM. Created by ppajuelo on 20/02/2019.
 */
public class CategoryOptionPopUp {

    private static CategoryOptionPopUp instance;
    private OnCatOptionClick listener;
    private Category category;
    private List<CategoryOption> options;

    public static CategoryOptionPopUp getInstance() {
        if (instance == null)
            instance = new CategoryOptionPopUp();
        return instance;
    }


    public CategoryOptionPopUp setCategory(Category category) {
        this.category = category;
        this.options = new ArrayList<>();

        for (CategoryOption option : category.categoryOptions())
            if (option.access().data().write())
                options.add(option);

        return this;
    }

    public CategoryOptionPopUp haveAnyCatOption(Context context){
        if(options.size() == 0)
            new CustomDialog(
                    context,
                    context.getString(R.string.blank_category),
                    context.getString(R.string.no_permission_category_options),
                    context.getString(R.string.action_accept),
                    "",
                    0,
                    new DialogClickListener() {
                        @Override
                        public void onPositive() {
                            dismiss();
                        }

                        @Override
                        public void onNegative() {
                            // dismiss
                        }
                    }
            ).show();

        return this;
    }


    public void show(Context context, View anchor) {
        PopupMenu menu = new PopupMenu(context, anchor);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getOrder() == 0) {
                listener.onCategoryOptionClick(null);
            } else {
                listener.onCategoryOptionClick(options.get(item.getOrder() - 1));
            }
            return false;
        });
        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, category.displayName());
        for (CategoryOption option : options) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, options.indexOf(option) + 1, option.displayName());
        }
        menu.show();
    }


    public CategoryOptionPopUp setOnClick(OnCatOptionClick listener) {
        this.listener = listener;
        return this;
    }

    public void dismiss() {
        instance = null;
    }

    public interface OnCatOptionClick {
        void onCategoryOptionClick(CategoryOption categoryOption);
    }
}
