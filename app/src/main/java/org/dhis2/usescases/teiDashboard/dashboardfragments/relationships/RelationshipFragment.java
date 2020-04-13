package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;

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
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OnDialogClickListener;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class RelationshipFragment extends FragmentGlobalAbstract implements RelationshipView {

    @Inject
    RelationshipPresenter presenter;

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
                    .plus(new RelationshipModule(this, activity.getProgramUid(), activity.getTeiUid()))
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
        presenter.init();

    }

    @Override
    public void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setRelationships(List<RelationshipViewModel> relationships) {
        if (relationshipAdapter != null) {
            relationshipAdapter.addItems(relationships);
        }
        if (relationships != null && !relationships.isEmpty()) {
            binding.emptyRelationships.setVisibility(View.GONE);
        } else {
            binding.emptyRelationships.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void goToAddRelationship(String teiUid, String teiTypeToAdd) {

        Intent intent = new Intent(getContext(), SearchTEActivity.class);
        Bundle extras = new Bundle();
        extras.putBoolean("FROM_RELATIONSHIP", true);
        extras.putString("FROM_RELATIONSHIP_TEI", teiUid);
        extras.putString("TRACKED_ENTITY_UID", teiTypeToAdd);
        extras.putString("PROGRAM_UID", null);
        intent.putExtras(extras);

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

    @Override
    public void initFab(List<Trio<RelationshipType, String, Integer>> relationshipTypes) {

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
            rfaContent
                    .setItems(items)
                    .setIconShadowRadius(RFABTextUtil.dip2px(getAbstracContext(), 5))
                    .setIconShadowColor(0xff888888)
                    .setIconShadowDy(RFABTextUtil.dip2px(getAbstracContext(), 1));

            rfaHelper = new RapidFloatingActionHelper(getAbstracContext(), binding.rfabLayout, binding.rfab, rfaContent).build();
        }
    }

    private void goToRelationShip(@NonNull RelationshipType relationshipTypeModel,
                                  @NonNull String teiTypeUid) {
        rfaHelper.toggleContent();
        relationshipType = relationshipTypeModel;
        presenter.goToAddRelationship(teiTypeUid);
    }

    @Override
    public void showPermissionError() {
        displayMessage(getString(R.string.search_access_error));
    }

    @Override
    public void openDashboardFor(@NotNull String teiUid) {
        getActivity().startActivity(TeiDashboardMobileActivity.intent(getContext(),teiUid,null,null));
    }

    @Override
    public void showTeiWithoutEnrollmentError(@NotNull String teiTypeName) {
        showInfoDialog(
                String.format(
                        getString(R.string.resource_not_found),
                        teiTypeName),
                getString(R.string.relationship_without_enrollment),
                getString(R.string.ok),
                getString(R.string.no),
                new OnDialogClickListener() {
                    @Override
                    public void onPossitiveClick(AlertDialog alertDialog) {
                        //NotUsed
                    }

                    @Override
                    public void onNegativeClick(AlertDialog alertDialog) {
                        //NotUsed
                    }
                })
                .show();
    }

    @Override
    public void showRelationshipNotFoundError(@NotNull String teiTypeName) {
        showInfoDialog(
                String.format(
                        getString(R.string.resource_not_found),
                        teiTypeName),
                getString(R.string.relationship_not_found_message),
                getString(R.string.ok),
                getString(R.string.no),
                new OnDialogClickListener() {
                    @Override
                    public void onPossitiveClick(AlertDialog alertDialog) {
                        //NotUsed
                    }

                    @Override
                    public void onNegativeClick(AlertDialog alertDialog) {
                        //NotUsed
                    }
                })
                .show();
    }
}
