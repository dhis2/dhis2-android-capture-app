package org.dhis2.usescases.searchTrackEntity.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import org.dhis2.R;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.dhis2.utils.customviews.ImageDetailBottomDialog;

import java.io.File;
import java.util.Objects;

import kotlin.Unit;

public class SearchTeiLiveAdapter extends PagedListAdapter<SearchTeiModel, SearchTEViewHolder> {

    private static final DiffUtil.ItemCallback<SearchTeiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<SearchTeiModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull SearchTeiModel oldItem, @NonNull SearchTeiModel newItem) {
            return oldItem.getTei().uid().equals(newItem.getTei().uid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SearchTeiModel oldItem, @NonNull SearchTeiModel newItem) {
            if (oldItem.isOnline() && oldItem.getTei().state() == null) {
                return oldItem.getTei().uid().equals(newItem.getTei().uid()) &&
                        (oldItem.getTei().state() == null && newItem.getTei().state() == null) &&
                        oldItem.getAttributeValues().equals(newItem.getAttributeValues()) &&
                        oldItem.getProfilePicturePath().equals(newItem.getProfilePicturePath()) &&
                        oldItem.isAttributeListOpen() == newItem.isAttributeListOpen() &&
                        Objects.equals(oldItem.getSortingKey(), newItem.getSortingKey()) &&
                        Objects.equals(oldItem.getSortingValue(), newItem.getSortingValue()) &&
                        Objects.equals(oldItem.getEnrolledOrgUnit(), newItem.getEnrolledOrgUnit());
            } else {
                return oldItem.getTei().uid().equals(newItem.getTei().uid()) &&
                        Objects.equals(oldItem.getTei().state(), newItem.getTei().state()) &&
                        oldItem.getAttributeValues().equals(newItem.getAttributeValues()) &&
                        oldItem.getEnrollments().equals(newItem.getEnrollments()) &&
                        oldItem.getProfilePicturePath().equals(newItem.getProfilePicturePath()) &&
                        oldItem.isAttributeListOpen() == newItem.isAttributeListOpen() &&
                        Objects.equals(oldItem.getSortingKey(), newItem.getSortingKey()) &&
                        Objects.equals(oldItem.getSortingValue(), newItem.getSortingValue()) &&
                        Objects.equals(oldItem.getEnrolledOrgUnit(), newItem.getEnrolledOrgUnit());
            }
        }
    };
    private final FragmentManager fm;
    private SearchTEContractsModule.Presenter presenter;

    public SearchTeiLiveAdapter(SearchTEContractsModule.Presenter presenter, FragmentManager fm) {
        super(DIFF_CALLBACK);
        this.presenter = presenter;
        this.fm = fm;
    }

    @NonNull
    @Override
    public SearchTEViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchTrackedEntityBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_tracked_entity, parent, false);
        return new SearchTEViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchTEViewHolder holder, int position) {
        holder.bind(
                presenter,
                getItem(position),
                () -> {
                    getItem(holder.getAdapterPosition()).toggleAttributeList();
                    notifyItemChanged(holder.getAdapterPosition());
                    return Unit.INSTANCE;
                },
                path -> {
                    new ImageDetailBottomDialog(
                            null,
                            new File(path)
                    ).show(
                            fm,
                            ImageDetailBottomDialog.TAG
                    );
                    return Unit.INSTANCE;
                }
        );
    }

    public void clearList() {
        submitList(null);
        notifyDataSetChanged();
    }
}
