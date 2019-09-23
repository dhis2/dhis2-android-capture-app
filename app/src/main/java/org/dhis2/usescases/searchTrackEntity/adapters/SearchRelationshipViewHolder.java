package org.dhis2.usescases.searchTrackEntity.adapters;

import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.dhis2.R;
import org.dhis2.databinding.ItemSearchRelationshipTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.utils.ObjectStyleUtils;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.io.File;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by frodriguez on 13/5/2019.
 */

public class SearchRelationshipViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchRelationshipTrackedEntityBinding binding;

    SearchRelationshipViewHolder(ItemSearchRelationshipTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(SearchTEContractsModule.Presenter presenter, SearchTeiModel teiModel) {
        binding.setPresenter(presenter);

        setTEIData(teiModel.getAttributeValues());
        binding.executePendingBindings();
        itemView.setOnClickListener(view -> presenter.addRelationship(teiModel.getTei().uid(), teiModel.isOnline()));

        binding.trackedEntityImage.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.photo_temp_gray));
        File file = new File(teiModel.getProfilePicturePath());
        Drawable placeHolderId = ObjectStyleUtils.getIconResource(itemView.getContext(), teiModel.getDefaultTypeIcon(), R.drawable.photo_temp_gray);
        Glide.with(itemView.getContext())
                .load(file)
                .placeholder(placeHolderId)
                .error(placeHolderId)
                .transition(withCrossFade())
                .transform(new CircleCrop())
                .into(binding.trackedEntityImage);
    }

    private void setTEIData(List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        binding.setAttribute(trackedEntityAttributeValues);
        binding.executePendingBindings();
    }


}
