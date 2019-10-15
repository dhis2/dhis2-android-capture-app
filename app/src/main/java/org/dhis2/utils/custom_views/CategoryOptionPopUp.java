package org.dhis2.utils.custom_views;

import android.content.Context;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;

import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;

import java.util.ArrayList;
import java.util.Date;
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
    private Date date;
    private String categoryName;

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

    public CategoryOptionPopUp setCatOptions(List<CategoryOption> catOptions){
        this.options = catOptions;
        return this;
    }

    public CategoryOptionPopUp setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        return this;
    }

    public CategoryOptionPopUp setDate(Date date){
        this.date = date;
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
        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, category != null ? category.displayName() : categoryName);
        for (CategoryOption option : options) {
            if(date == null || ((option.startDate() == null || date.after(option.startDate())) && (option.endDate() == null || date.before(option.endDate()))))
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
