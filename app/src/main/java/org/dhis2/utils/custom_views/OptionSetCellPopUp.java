package org.dhis2.utils.custom_views;

import android.content.Context;
import android.view.Menu;
import android.view.View;

import org.dhis2.App;
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.option.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.widget.PopupMenu;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 20/02/2019.
 */
public class OptionSetCellPopUp extends PopupMenu {

    private final D2 d2;
    private final CompositeDisposable disposable;
    private final List<String> optionsToHide;
    private final List<String> optionGroupsToHide;
    private HashMap<String, Option> optionsMap;

    public OptionSetCellPopUp(Context context, View anchor, SpinnerViewModel model,
                          OptionSetView optionSetView) {
        super(context, anchor);
        d2 = ((App) context.getApplicationContext()).serverComponent().userManager().getD2();
        this.optionsToHide = model.getOptionsToHide();
        this.optionGroupsToHide = model.getOptionGroupsToHide();
        setOnDismissListener(menu -> dismiss());
        setOnMenuItemClickListener(item -> {
            Option selectedOption = optionsMap.get(item.getTitle().toString());
            optionSetView.onSelectOption(selectedOption);
            return false;
        });
        disposable = new CompositeDisposable();

        disposable.add(
                Single.fromCallable(() -> d2.optionModule().options
                        .byOptionSetUid().eq(model.optionSet()))
                        .map(optionRepository -> {
                            List<String> finalOptionsToHide = new ArrayList<>();
                            if (optionsToHide!=null && !optionsToHide.isEmpty())
                                finalOptionsToHide.addAll(optionsToHide);

                            if (optionGroupsToHide!=null && !optionGroupsToHide.isEmpty()) {
                                for (String groupUid : optionGroupsToHide) {
                                    finalOptionsToHide.addAll(
                                            UidsHelper.getUidsList(d2.optionModule().optionGroups.withOptions().uid(groupUid).blockingGet().options())
                                    );
                                }
                            }
                            if (!finalOptionsToHide.isEmpty())
                                optionRepository = optionRepository
                                        .byUid().notIn(finalOptionsToHide);

                            return optionRepository.blockingGet();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::setOptions,
                                Timber::e
                        )
        );

    }

    public void setOptions(List<Option> options) {
        optionsMap = new HashMap<>();
        for (Option option : options) {
            optionsMap.put(option.displayName(), option);
            getMenu().add(Menu.NONE, Menu.NONE, options.indexOf(option) + 1, option.displayName());
        }
        show();
    }

    @Override
    public void dismiss() {
        disposable.clear();
        super.dismiss();
    }
}
