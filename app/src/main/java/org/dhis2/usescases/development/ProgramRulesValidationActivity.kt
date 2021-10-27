package org.dhis2.usescases.development

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Dp
import androidx.databinding.DataBindingUtil
import com.google.android.material.composethemeadapter.MdcTheme
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.commons.animations.hide
import org.dhis2.commons.animations.show
import org.dhis2.data.forms.dataentry.FormView
import org.dhis2.databinding.ActivityProgramRulesValidationBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.program.Program

class ProgramRulesValidationActivity : ActivityGlobalAbstract() {

    private val binding: ActivityProgramRulesValidationBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_program_rules_validation)
    }
    private val rulesValidationsAdapter by lazy {
        RulesValidationsAdapter { link -> openInBrowser(link) }
    }

    @Inject
    lateinit var ruleValidationModelFactory: RulesValidationsModelFactory

    val rulesValidationsModel: RulesValidationsModel by viewModels { ruleValidationModelFactory }

    @Inject
    lateinit var formView: FormView

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        app().serverComponent()?.plus(ProgramRulesValidationsModule(this))?.inject(this)
        super.onCreate(savedInstanceState)

        binding.progress.show()
        binding.apply {
            backButton.setOnClickListener { onBackPressed() }
            ruleValidations.adapter = rulesValidationsAdapter
            runRuleValidation.setOnClickListener { rulesValidationsModel.runCurrentValidation() }
            composeView.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    MdcTheme {
                        ValidationList(rulesValidationsModel)
                    }
                }
            }
        }
        rulesValidationsModel.ruleValidations.observe(
            this,
            {
                rulesValidationsAdapter.submitList(it) {
                    binding.progress.hide()
                }
            }
        )

        rulesValidationsModel.programVariables.observe(
            this,
            {
                formView.processItems(it)
            }
        )

        rulesValidationsModel.expressionValidationResult.observe(
            this,
            {
                if (it) {
                    Toast.makeText(
                        this,
                        "Expression is correct",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Expression is wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.formViewContainer, formView).commit()
    }

    private fun openInBrowser(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(intent)
    }
}

@ExperimentalFoundationApi
@Composable
fun ValidationList(rulesValidationsModel: RulesValidationsModel) {
    val ruleValidations by rulesValidationsModel.ruleValidations.observeAsState(emptyList())
    LazyColumn {
        ruleValidations.groupBy { it.program }.forEach { (program, items) ->
            stickyHeader {
                RuleValidationSectionRow(program)
            }

            items(items) { ruleValidation ->
                RuleValidationRow(ruleValidation)
            }
        }
    }
}

@Composable
fun RuleValidationRow(ruleValidation: RuleValidation) {
    Column {
        Text(ruleValidation.title()!!)
        Text(ruleValidation.errors())
    }
}

@Composable
fun RuleValidationSectionRow(program: Program) {
    Column(
        Modifier
            .background(color = MaterialTheme.colors.primary)
            .fillMaxWidth()
            .padding(vertical = Dp(8f), horizontal = Dp(16f))
    ) {
        Text(program.displayName()!!, color = MaterialTheme.colors.onPrimary)
    }
}
