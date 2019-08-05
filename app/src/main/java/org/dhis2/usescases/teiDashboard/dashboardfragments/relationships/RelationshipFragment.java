package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;
import com.wangjie.rapidfloatingactionbutton.util.RFABTextUtil;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentRelationshipsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class RelationshipFragment extends FragmentGlobalAbstract implements RelationshipContracts.View {

    @Inject
    RelationshipContracts.Presenter presenter;

    private FragmentRelationshipsBinding binding;

    private RelationshipAdapter relationshipAdapter;
    private RapidFloatingActionHelper rfaHelper;
    private RelationshipType relationshipType;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        TeiDashboardMobileActivity activity = (TeiDashboardMobileActivity) context;
        if (((App) context.getApplicationContext()).dashboardComponent() != null)
            ((App) context.getApplicationContext())
                    .dashboardComponent()
                    .plus(new RelationshipModule(activity.getProgramUid(), activity.getTeiUid()))
                    .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_relationships, container, false);
        relationshipAdapter = new RelationshipAdapter(presenter);
        binding.relationshipRecycler.setAdapter(relationshipAdapter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);

    }

    @Override
    public void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public Consumer<List<RelationshipViewModel>> setRelationships() {
        return relationships -> {
            if (relationshipAdapter != null) {
                relationshipAdapter.addItems(relationships);
            }
            if (relationships != null && !relationships.isEmpty()) {
                binding.emptyRelationships.setVisibility(View.GONE);
            } else {
                binding.emptyRelationships.setVisibility(View.VISIBLE);
            }
        };
    }

    @Override
    public Consumer<List<Trio<RelationshipType, String, Integer>>> setRelationshipTypes() {
        return this::initFab;
    }

    @Override
    public void goToAddRelationship(Intent intent) {
        ((TeiDashboardMobileActivity) getActivity()).toRelationships();
        this.startActivityForResult(intent, Constants.REQ_ADD_RELATIONSHIP);
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

    private void initFab(List<Trio<RelationshipType, String, Integer>> relationshipTypes) {

        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(getAbstracContext());
        rfaContent.setOnRapidFloatingActionContentLabelListListener(new RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener() {
            @Override
            public void onRFACItemLabelClick(int position, RFACLabelItem item) {
                Pair<RelationshipType, String> pair = (Pair<RelationshipType, String>) item.getWrapper();
                goToRelationShip(pair.val0(), pair.val1());
            }

            @Override
            public void onRFACItemIconClick(int position, RFACLabelItem item) {
                Pair<RelationshipType, String> pair = (Pair<RelationshipType, String>) item.getWrapper();
                goToRelationShip(pair.val0(), pair.val1());
            }
        });
        List<RFACLabelItem> items = new ArrayList<>();
        for (Trio<RelationshipType, String, Integer> trio : relationshipTypes) {
            RelationshipType relationshipType = trio.val0();
            int resource = trio.val2();
            items.add(new RFACLabelItem<Pair<RelationshipType, String>>()
                    .setLabel(relationshipType.displayName())
                    .setResId(resource)
                    .setLabelTextBold(true)
                    .setLabelBackgroundDrawable(AppCompatResources.getDrawable(getAbstracContext(), R.drawable.bg_chip))
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

    private void goToRelationShip(@NonNull RelationshipType relationshipTypeModel,
                                  @NonNull String teiTypeUid) {
        rfaHelper.toggleContent();
        relationshipType = relationshipTypeModel;
        presenter.goToAddRelationship(teiTypeUid);
    }
}
