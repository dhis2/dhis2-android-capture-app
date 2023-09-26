package org.dhis2.usescases.searchTrackEntity.listView

import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.databinding.ResultSearchListBinding
import org.dhis2.usescases.searchTrackEntity.ui.SearchResultUi

class SearchResultHolder(
    val binding: ResultSearchListBinding,
    private val onSearchOutsideProgram: () -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
    }

    fun bind(item: SearchResult) {
        binding.composeView.apply {
            updateLayoutParams {
                height = if (item.shouldDisplayInFullSize()) {
                    ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
        }.setContent {
            MdcTheme {
                SearchResultUi(
                    searchResult = item,
                    onSearchOutsideClick = onSearchOutsideProgram,
                )
            }
        }
    }
}
