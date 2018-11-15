package org.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;
import com.wangjie.rapidfloatingactionbutton.util.RFABTextUtil;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentRelationshipsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.adapters.RelationshipAdapter;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class RelationshipFragment extends FragmentGlobalAbstract {

    FragmentRelationshipsBinding binding;
    TeiDashboardContracts.Presenter presenter;

    private DashboardProgramModel dashboardProgramModel;
    static RelationshipFragment instance;
    private RelationshipAdapter relationshipAdapter;
    private RapidFloatingActionHelper rfaHelper;
    private RelationshipTypeModel relationshipType;

    static public RelationshipFragment getInstance() {
        if (instance == null) {
            instance = new RelationshipFragment();
        }
        return instance;
    }

    public static RelationshipFragment createInstance() {
        return instance = new RelationshipFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_relationships, container, false);
        binding.setPresenter(presenter);
        relationshipAdapter = new RelationshipAdapter(presenter);
        binding.relationshipRecycler.setAdapter(relationshipAdapter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        setData(presenter.getDashBoardData());
    }

    public void setData(DashboardProgramModel dashboardProgramModel) {
        this.dashboardProgramModel = dashboardProgramModel;

        binding.executePendingBindings();

        presenter.subscribeToRelationships(this);
        presenter.subscribeToRelationshipTypes(this);

    }

    public Consumer<List<Pair<Relationship, RelationshipType>>> setRelationships() {
        return relationships -> relationshipAdapter.addItems(relationships);
    }

    public Consumer<List<Trio<RelationshipTypeModel, String, Integer>>> setRelationshipTypes() {
        return this::initFab;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_ADD_RELATIONSHIP) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String tei_a = data.getStringExtra("TEI_A_UID");
                    presenter.addRelationship(tei_a, relationshipType.uid());
                }
            }
        }
    }

    private void initFab(List<Trio<RelationshipTypeModel, String, Integer>> relationshipTypes) {

        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(getAbstracContext());
        rfaContent.setOnRapidFloatingActionContentLabelListListener(new RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener() {
            @Override
            public void onRFACItemLabelClick(int position, RFACLabelItem item) {
                rfaHelper.toggleContent();
                relationshipType = (RelationshipTypeModel) item.getWrapper();
                presenter.goToAddRelationship(relationshipType.uid());
            }

            @Override
            public void onRFACItemIconClick(int position, RFACLabelItem item) {
                rfaHelper.toggleContent();
                relationshipType = ((Pair<RelationshipTypeModel, String>) item.getWrapper()).val0();
                presenter.goToAddRelationship(((Pair<RelationshipTypeModel, String>) item.getWrapper()).val1());
            }
        });
        List<RFACLabelItem> items = new ArrayList<>();
        for (Trio<RelationshipTypeModel, String, Integer> trio : relationshipTypes) {
            RelationshipTypeModel relationshipType = trio.val0();
            int resource = trio.val2();
            items.add(new RFACLabelItem<Pair<RelationshipTypeModel, String>>()
                    .setLabel(relationshipType.displayName())
                    .setResId(resource)
                    .setLabelTextBold(true)
                    .setLabelBackgroundDrawable(ContextCompat.getDrawable(getAbstracContext(), R.drawable.bg_chip))
                    .setIconNormalColor(ColorUtils.getPrimaryColor(getAbstracContext(), ColorUtils.ColorType.PRIMARY_DARK))
                    .setWrapper(Pair.create(relationshipType, trio.val1()))
            );
        }

        if (!items.isEmpty()) {
            rfaContent.setItems(items)
                    .setItems(items)
                    .setIconShadowRadius(RFABTextUtil.dip2px(getAbstracContext(), 5))
                    .setIconShadowColor(0xff888888)
                    .setIconShadowDy(RFABTextUtil.dip2px(getAbstracContext(), 5))
                    .setIconShadowColor(0xff888888);

            rfaHelper = new RapidFloatingActionHelper(getAbstracContext(), binding.rfabLayout, binding.rfab, rfaContent).build();
        }
    }
}
