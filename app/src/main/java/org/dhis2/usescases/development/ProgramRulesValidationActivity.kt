package org.dhis2.usescases.development

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.commons.animations.hide
import org.dhis2.commons.animations.show
import org.dhis2.data.forms.dataentry.FormView
import org.dhis2.databinding.ActivityProgramRulesValidationBinding
import org.dhis2.form.model.RowAction
import org.dhis2.usescases.general.ActivityGlobalAbstract
import javax.inject.Inject

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

    override fun onCreate(savedInstanceState: Bundle?) {
        app().serverComponent()?.plus(ProgramRulesValidationsModule(this))?.inject(this)
        super.onCreate(savedInstanceState)
        binding.progress.show()
        binding.apply {
            backButton.setOnClickListener { onBackPressed() }
            ruleValidations.adapter = rulesValidationsAdapter
            runRuleValidation.setOnClickListener { rulesValidationsModel.runCurrentValidation() }
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
            this, {
                formView.processItems(it)
            }
        )

        rulesValidationsModel.expressionValidationResult.observe(
            this,{
                if(it) {
                    Toast.makeText(
                        this,
                        "Expression is correct",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
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
