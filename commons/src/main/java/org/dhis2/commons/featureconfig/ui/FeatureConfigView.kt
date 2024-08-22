package org.dhis2.commons.featureconfig.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.dhis2.commons.R
import org.dhis2.commons.featureconfig.di.FeatureConfigComponentProvider
import org.dhis2.commons.featureconfig.model.FeatureOptions
import javax.inject.Inject

class FeatureConfigView : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: FeatureConfigViewModelFactory

    private val viewModel: FeatureConfigViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (applicationContext as FeatureConfigComponentProvider)
            .provideFeatureConfigActivityComponent()
            ?.inject(this)

        setContent {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.feature_configuration)) },
                        navigationIcon = {
                            IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBackIosNew,
                                    contentDescription = "Back",
                                )
                            }
                        },
                    )
                },
            ) {
                val features by viewModel.featuresList.observeAsState(emptyList())
                LazyColumn(
                    modifier = Modifier.padding(it),
                    verticalArrangement = spacedBy(16.dp),
                ) {
                    items(items = features) { feature ->

                        var currentFeature by remember(feature) {
                            mutableStateOf(feature)
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacedBy(8.dp),
                        ) {
                            Text(modifier = Modifier.weight(1f), text = currentFeature.feature.description)
                            currentFeature.extras?.let { options ->
                                when (options) {
                                    is FeatureOptions.ResponsiveHome -> {
                                        var currentValue by remember {
                                            mutableStateOf(options.totalItems?.toString())
                                        }
                                        TextField(
                                            value = currentValue ?: "",
                                            maxLines = 1,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            onValueChange = { value ->
                                                currentValue = value
                                                currentFeature = currentFeature.copy(
                                                    extras = options.copy(totalItems = value.toIntOrNull()),
                                                    canBeEnabled = value.toIntOrNull() != null,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                            Switch(
                                checked = currentFeature.enable,
                                enabled = currentFeature.canBeEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.didUserTapOnItem(currentFeature.copy(enable = enabled))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
