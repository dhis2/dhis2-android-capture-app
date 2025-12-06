package org.dhis2.usescases.videoGuide.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dhis2.usescases.videoGuide.VideoGuideViewModel
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType

@Composable
fun VideoGuideScreen(
    viewModel: VideoGuideViewModel,
    onVideoClick: (String) -> Unit,
) {
    val videoList by viewModel.videoList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            ProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                type = ProgressIndicatorType.CIRCULAR,
            )
        } else {
            if (videoList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No videos available")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(videoList) { video ->
                        VideoItemCard(
                            video = video,
                            onClick = { onVideoClick(video.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItemCard(
    video: VideoItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Text(
            text = video.title,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = "ID: ${video.id}",
            modifier = Modifier.padding(bottom = 4.dp),
        )
        if (video.description.isNotEmpty()) {
            Text(
                text = video.description,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        if (video.thumbnailUrl != null) {
            Text(
                text = "Thumbnail: ${video.thumbnailUrl}",
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        if (video.tag != null) {
            Text(
                text = "Tag: ${video.tag}",
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        if (video.category != null) {
            Text(
                text = "Category: ${video.category}",
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Text(
            text = "URL: ${video.videoUrl}",
            modifier = Modifier.padding(bottom = 4.dp),
        )
        if (video.duration != null) {
            Text(text = "Duration: ${video.duration}")
        }
    }
}

