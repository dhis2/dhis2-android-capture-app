package org.dhis2.usescases.searchTrackEntity.ui

import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.bindings.dp as inDp
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel

@ExperimentalAnimationApi
@Composable
fun SearchTEListScreen(viewModel: SearchTEIViewModel, listAdapter: ConcatAdapter) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        val (searchButton, createButton, content) = createRefs()

        val createButtonVisibility by viewModel.createButtonScrollVisibility.observeAsState(
            true
        )

        var isScrollingDown by remember { mutableStateOf(false) }

        AndroidView(
            modifier = Modifier
                .background(Color.White)
                .constrainAs(content) {
                    top.linkTo(parent.top)
                },
            factory = { context ->
                RecyclerView(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    layoutManager = LinearLayoutManager(context)
                    updatePaddingRelative(top = 80.inDp, bottom = 80.inDp)
                    clipToPadding = false
                    adapter = listAdapter
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            if (dy > 0) {
                                isScrollingDown = true
                            } else if (dy < 0) {
                                isScrollingDown = false
                            }
                        }
                    })
                }
            },
            update = {}
        )

        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            FullSearchButton(
                modifier = Modifier.constrainAs(searchButton) {
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    top.linkTo(parent.top, margin = 16.dp)
                    width = Dimension.fillToConstraints
                },
                visible = !isScrollingDown,
                onClick = { viewModel.setSearchScreen() }
            )
        }
        CreateNewButton(
            modifier = Modifier.constrainAs(createButton) {
                end.linkTo(parent.end, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 72.dp)
            },
            extended = createButtonVisibility and !isScrollingDown,
            onClick = viewModel::onEnrollClick
        )
    }
}
