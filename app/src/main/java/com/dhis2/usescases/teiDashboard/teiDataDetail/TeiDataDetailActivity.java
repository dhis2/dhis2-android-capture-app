package com.dhis2.usescases.teiDashboard.teiDataDetail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityTeidataDetailBinding;
import com.dhis2.databinding.FormEditTextTeiDataBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.google.android.flexbox.FlexboxLayout;

import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import javax.inject.Inject;

public class TeiDataDetailActivity extends ActivityGlobalAbstract implements TeiDataDetailContracts.View {
    ActivityTeidataDetailBinding binding;

    @Inject
    TeiDataDetailContracts.Presenter presenter;
    private boolean isEditable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new TeiDataDetailModule(getIntent().getStringExtra("ENROLLMENT_UID"))).inject(this);
        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_teidata_detail);
        binding.setPresenter(presenter);
        init(getIntent().getStringExtra("TEI_UID"), getIntent().getStringExtra("PROGRAM_UID"));
        isEditable = getIntent().getBooleanExtra("IS_EDITABLE", false);

    }

    @Override
    public void init(String teiUid, String programUid) {
        presenter.init(this, teiUid, programUid);
    }

    @Override
    public void setData(DashboardProgramModel program) {
        binding.setDashboardModel(program);
        binding.setProgram(program.getCurrentProgram());
        binding.executePendingBindings();
        setUpAttr(program);
        supportStartPostponedEnterTransition();

    }

    private void setUpAttr(DashboardProgramModel programModel) {
        for (ProgramTrackedEntityAttributeModel programAttr : programModel.getTrackedEntityAttributesModel()) {

            FormEditTextTeiDataBinding editTextBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(this), R.layout.form_edit_text_tei_data, binding.dataLayout, false
            );
            editTextBinding.setAttrModel(programAttr);
            editTextBinding.setIsEditable(isEditable);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setFlexBasisPercent(1f);
            binding.dataLayout.addView(editTextBinding.getRoot(), params);

            for (TrackedEntityAttributeValueModel dataValueModel : programModel.getTrackedEntityAttributeValues()) {
                if (dataValueModel.trackedEntityAttribute().equals(programAttr.trackedEntityAttribute()))
                    editTextBinding.setAttr(dataValueModel);

            }

            editTextBinding.formEdittext.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    presenter.saveData(programAttr, s.toString());

                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        binding.dataLayout.invalidate();
    }
}