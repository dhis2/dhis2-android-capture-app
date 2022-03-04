package org.dhis2.usescases.searchTrackEntity.listView

import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.databinding.ResultSearchListBinding
import org.dhis2.usescases.searchTrackEntity.SearchResult

class SearchResultHolder(
    val binding: ResultSearchListBinding,
    private val onSearchOutsideProgram: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    fun bind(item: SearchResult) {
        binding.composeView.setContent {
            MdcTheme {
                SearchResult(
                    searchResult = item,
                    onSearchOutsideClick = onSearchOutsideProgram
                )
            }
        }
    }
}
