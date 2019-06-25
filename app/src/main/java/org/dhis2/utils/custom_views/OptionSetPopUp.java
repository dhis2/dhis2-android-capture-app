package org.dhis2.utils.custom_views;

import android.content.Context;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.option.OptionModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 20/02/2019.
 */
public class OptionSetPopUp {

    private static OptionSetPopUp instance;
    private HashMap<String, OptionModel> optionsMap;
    private FlowableProcessor<Trio<String, String, Integer>> processor;
    private SpinnerViewModel optionSet;
    private Context context;
    private View anchor;
    private PopupMenu.OnMenuItemClickListener listener;
    private PopupMenu menu;

    public static OptionSetPopUp getInstance() {
        if (instance == null)
            instance = new OptionSetPopUp();
        return instance;
    }

    public static Boolean isCreated() {
        return instance != null;
    }

    public void setOptions(List<OptionModel> options) {
        if (menu == null) {
            optionsMap = new HashMap<>();
            menu = new PopupMenu(context, anchor);
            menu.setOnMenuItemClickListener(listener);
            for (OptionModel optionModel : options) {
                optionsMap.put(optionModel.displayName(), optionModel);
                menu.getMenu().add(Menu.NONE, Menu.NONE, options.indexOf(optionModel) + 1, optionModel.displayName());
            }
            menu.setOnDismissListener(menu -> {
                dismiss();
            });
            menu.show();
        }
    }

    public Map<String, OptionModel> getOptions() {
        return optionsMap;
    }

    public OptionSetPopUp setOptionSetUid(SpinnerViewModel view) {
        this.optionSet = view;
        return this;
    }

    public OptionSetPopUp setProcessor(FlowableProcessor<Trio<String, String, Integer>> processor) {
        this.processor = processor;
        return this;
    }

    public void show(Context context, View anchor) {
        this.context = context;
        this.anchor = anchor;
        processor.onNext(Trio.Companion.create("", optionSet.optionSet(), 0));
    }


    public OptionSetPopUp setOnClick(PopupMenu.OnMenuItemClickListener listener) {
        this.listener = listener;
        return this;
    }

    public void dismiss() {
        instance = null;
    }
}
