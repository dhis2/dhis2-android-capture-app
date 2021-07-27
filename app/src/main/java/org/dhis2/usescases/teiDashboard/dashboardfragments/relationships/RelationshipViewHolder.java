package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.databinding.ItemCarouselRelationshipBinding;
import org.dhis2.databinding.ItemRelationshipBinding;
import org.hisp.dhis.android.core.relationship.Relationship;

import java.io.File;
import java.util.List;

import kotlin.Pair;

public class RelationshipViewHolder extends RecyclerView.ViewHolder {

    private final ItemRelationshipBinding binding;

    public RelationshipViewHolder(ItemRelationshipBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(RelationshipPresenter presenter, RelationshipViewModel relationships) {

        Relationship relationship = relationships.getRelationship();

        boolean from = relationships.getDirection() == RelationshipDirection.FROM;

        binding.relationshipCard.setOnClickListener(view ->
                presenter.onRelationshipClicked(relationships.getOwnerType(), relationships.getOwnerUid())
        );
        binding.clearButton.setOnClickListener(view -> presenter.deleteRelationship(relationship.uid()));

        String relationshipNameText = from ? relationships.getRelationshipType().toFromName() : relationships.getRelationshipType().fromToName();
        binding.relationshipTypeName.setText(relationshipNameText != null ? relationshipNameText : relationships.getRelationshipType().displayName());

        binding.fromRelationshipName.setText(getPrimaryAttributes(relationships.getFromValues()));
        binding.toRelationshipName.setText(getPrimaryAttributes(relationships.getToValues()));
        setImage(relationships.getFromImage(), relationships.getFromDefaultImageResource(), binding.fromTeiImage);
        setImage(relationships.getToImage(), relationships.getToDefaultImageResource(), binding.toTeiImage);

    }

    private String getPrimaryAttributes(List<Pair<String, String>> values){
        if(values.size()>1) {
            return String.format("%s %s", values.get(0).getSecond(), values.get(1).getSecond());
        }else if(values.size() == 1){
            return String.format("%s", values.get(0).getSecond());
        }else{
            return "-";
        }
    }

    private void setImage(String imagePath, int defaultImage, ImageView target) {
        RequestBuilder<Drawable> glideRequest;
        if(imagePath == null){
            glideRequest = Glide.with(itemView.getContext()).load(defaultImage)
                    .transform(new RoundedCorners(ExtensionsKt.getDp(6)));
        }else{
            glideRequest = Glide.with(itemView.getContext()).load(new File(imagePath))
                    .transform(new CircleCrop());
        }
        glideRequest
                .placeholder(defaultImage)
                .error(defaultImage)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .skipMemoryCache(true)
                .into(target);
    }
}
