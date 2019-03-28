package org.dhis2.utils.custom_views;

import android.content.Context;
import android.view.Menu;
import android.view.View;

import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;

import androidx.appcompat.widget.PopupMenu;

/**
 * QUADRAM. Created by ppajuelo on 20/02/2019.
 */
public class CategoryOptionPopUp {

    private static CategoryOptionPopUp instance;
    private OnCatOptionClick listener;
    private Category category;

    public static CategoryOptionPopUp getInstance() {
        if (instance == null)
            instance = new CategoryOptionPopUp();
        return instance;
    }


    public CategoryOptionPopUp setCategory(Category category) {
        this.category = category;

        return this;
    }


    public void show(Context context, View anchor) {
        PopupMenu menu = new PopupMenu(context, anchor);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getOrder() == 0) {
                listener.onCategoryOptionClick(null);
            } else {
                listener.onCategoryOptionClick(category.categoryOptions().get(item.getOrder() - 1));
            }
            return false;
        });
        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, category.displayName());
        for (CategoryOption option : category.categoryOptions()) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, category.categoryOptions().indexOf(option) + 1, option.displayName());
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
