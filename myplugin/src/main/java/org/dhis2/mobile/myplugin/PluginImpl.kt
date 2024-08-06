package org.dhis2.mobile.myplugin

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.processor.internal.definecomponent.codegen._dagger_hilt_android_components_ViewModelComponent
import org.dhis2.commons.extensions.serializable

import org.dhis2.commons.plugin.PluginInterface
import org.dhis2.mobile.myplugin.di.MainViewmodelFactory
import org.dhis2.mobile.myplugin.ui.theme.MainViewModel
import org.dhis2.mobile.myplugin.ui.theme.MainViewModel_Factory
import org.dhis2.mobile.myplugin.ui.theme.ProgramItem
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.CardDetail
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel

class PluginImpl : PluginInterface {

    @Composable
    override fun Show(context: Context) {
        MainScreen()
    }



    @Composable
    fun MainScreen(
    ) {

        val viewModel = MainViewModel(D2Manager.getD2())
        val programs = viewModel.programList.observeAsState(listOf())

        Scaffold(modifier = Modifier.fillMaxSize()) {
                innerPadding ->
            LazyColumn(
                Modifier.padding(top = 16.dp),
            ) {
                items(programs.value) {
                    ProgramCard(program = it.program)
                }
            }
            Column(modifier = Modifier.padding(innerPadding)) {
                CardDetail(
                    title = "This is  new content with activity lifecycle and hopefully sdk requests!!!",
                    additionalInfoList = listOf(
                        AdditionalInfoItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Android,
                                    contentDescription = "This is my amazing plugin!!!",
                                    tint = AdditionalInfoItemColor.SUCCESS.color,
                                )
                            },
                            value = "Lets Rock",
                            color = AdditionalInfoItemColor.ERROR.color,
                            isConstantItem = true,
                        ),
                    ),
                )
            }
        }
    }


    @Composable
    fun ProgramCard(program: Program) {
        ListCard(title = ListCardTitleModel(text =  program.displayName() ?: "-"),
            additionalInfoList = getAdditionalInfoList(program),
            onCardClick = { navigateToProgramDetailScreen(program) })
    }

    private fun navigateToProgramDetailScreen(program: Program) {

    }

    private fun getAdditionalInfoList(program: Program): List<AdditionalInfoItem> {
        val detailsList: MutableList<AdditionalInfoItem> = mutableListOf()
        program.programType()?.name.let {
            detailsList.add(AdditionalInfoItem(key = "Type", value = it?: "-"))
        }

        program.trackedEntityType()?.name().let {
            detailsList.add(AdditionalInfoItem(key = "Tracked Entity Type", value = it?: "-"))
        }

        program.displayEventLabel().let {
            detailsList.add(AdditionalInfoItem(key = "Event Label", value = it?: "-"))
        }



        return detailsList
    }
}


