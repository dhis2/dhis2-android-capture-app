package com.dhis2.usescases.searchTrackEntity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.dhis2.R;
import com.dhis2.databinding.ActivitySearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View {

    ActivitySearchBinding binding;
    @Inject
    SearchTEPresenter presenter;

    @Inject
    FormAdapter formAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init();

    }

    @Override
    public void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels) {

        TableRow tableRow = new TableRow(this);
        EditText editText;
        for (int i = 0; i < trackedEntityAttributeModels.size(); i++) {

            editText = new EditText(this);
            editText.setHint(trackedEntityAttributeModels.get(i).displayShortName());
            editText.setHintTextColor(ContextCompat.getColor(this, R.color.white_faf));
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            tableRow.addView(editText);

            if (i % 2 != 0) {
                ((TableLayout) binding.getRoot().findViewById(R.id.tablelayout)).addView(tableRow);
                tableRow = new TableRow(this);
            }

        }

    }
}
