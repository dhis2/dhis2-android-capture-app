package org.dhis2.usescases.development

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import org.dhis2.R
import org.dhis2.commons.featureconfig.ui.FeatureConfigView
import org.dhis2.databinding.DevelopmentActivityBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.fileresource.FileResourceDomain
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer

class DevelopmentActivity : ActivityGlobalAbstract() {
    private var count = 0
    private var iconNames: List<String>? = null
    private var binding: DevelopmentActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.development_activity)

        loadIconsDevTools()
        loadCrashControl()
        loadFeatureConfig()
        loadConflicts()
        loadMultiText()
        loadCustomIcons()
    }

    private fun loadCustomIcons() {
        binding!!.forceCustomIcon.setOnClickListener { _: View? ->
            val d2: D2 = D2Manager.getD2()
            val fileResource =
                d2
                    .fileResourceModule()
                    .fileResources()
                    .byDomain()
                    .eq(FileResourceDomain.DATA_VALUE)
                    .one()
                    .blockingGet()
            if (fileResource != null) {
                val uidToInsert = fileResource.uid()
                runBlocking {
                    d2.databaseAdapter().execSQL(
                        String.format(
                            "INSERT INTO CustomIcon (\"key\", \"fileResourceUid\", \"href\") VALUES (\"%s\",\"%s\",\"%s\")",
                            uidToInsert,
                            uidToInsert,
                            uidToInsert,
                        ),
                    )
                    d2.databaseAdapter().execSQL(
                        String.format("UPDATE Program SET icon = \"%s\"", uidToInsert),
                    )
                    d2.databaseAdapter().execSQL(
                        String.format("UPDATE DataSet SET icon = \"%s\"", uidToInsert),
                    )
                    d2.databaseAdapter().execSQL(
                        String.format("UPDATE ProgramStage SET icon = \"%s\"", uidToInsert),
                    )
                    d2.databaseAdapter().execSQL(
                        String.format("UPDATE Option SET icon = \"%s\"", uidToInsert),
                    )
                }
            } else {
                Toast
                    .makeText(
                        this,
                        "No file resource found. Add an image in a form and retry",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    private fun loadMultiText() {
        val d2: D2 = D2Manager.getD2()
        val hasMultiText =
            !d2
                .dataElementModule()
                .dataElements()
                .byValueType()
                .eq(ValueType.MULTI_TEXT)
                .blockingIsEmpty()
        binding!!.multitext.text = if (hasMultiText) "REVERT" else "FORCE MULTITEXT"
        binding!!.multitext.setOnClickListener { _: View? ->
            if (hasMultiText) {
                runBlocking {
                    d2.databaseAdapter().execSQL(
                        "UPDATE DataElement SET valueType = \"TEXT\" WHERE valueType = \"MULTI_TEXT\" AND optionSet IS NOT null",
                    )
                }
            } else {
                runBlocking {
                    d2.databaseAdapter().execSQL(
                        "UPDATE DataElement SET valueType = \"MULTI_TEXT\" WHERE valueType = \"TEXT\" AND optionSet IS NOT null",
                    )
                }
            }
        }
    }

    private fun loadConflicts() {
        binding!!.addConflicts.setOnClickListener { _: View? ->
            val d2: D2 = D2Manager.getD2()
            ConflictGenerator(d2).generate()
        }
        binding!!.clearConflicts.setOnClickListener { _: View? ->
            val d2: D2 = D2Manager.getD2()
            ConflictGenerator(d2).clear()
        }
    }

    private fun loadIconsDevTools() {
        val `is` = resources.openRawResource(R.raw.icon_names)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        try {
            val reader: Reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            var n: Int
            while ((reader.read(buffer).also { n = it }) != -1) {
                writer.write(buffer, 0, n)
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                Timber.e(e)
            }
        }

        val json = writer.toString()
        iconNames =
            Gson().fromJson(
                json,
                object : TypeToken<List<String?>?>() {
                }.type,
            )
        count = 0

        binding!!.iconButton.setOnClickListener { _: View? ->
            nextDrawable()
        }

        binding!!.automaticErrorCheck.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) nextDrawable()
        }

        renderIconForPosition(count)
    }

    private fun renderIconForPosition(position: Int) {
        val iconName = iconNames!![position]

        binding!!.iconInput.setText(iconName)

        val iconResourceNegative =
            resources.getIdentifier(
                iconName + "_negative",
                "drawable",
                packageName,
            )
        val iconResourceOutline =
            resources.getIdentifier(
                iconName + "_outline",
                "drawable",
                packageName,
            )
        val iconResourcePositive =
            resources.getIdentifier(
                iconName + "_positive",
                "drawable",
                packageName,
            )
        binding!!.iconInput.error = null

        binding!!.iconImagePossitive.setImageDrawable(null)
        binding!!.iconImageOutline.setImageDrawable(null)
        binding!!.iconImageNegative.setImageDrawable(null)

        binding!!.iconImagePossitiveTint.setImageDrawable(null)
        binding!!.iconImageOutlineTint.setImageDrawable(null)
        binding!!.iconImageNegativeTint.setImageDrawable(null)

        var hasError = false
        try {
            binding!!.iconImagePossitive.setImageResource(iconResourcePositive)
        } catch (e: Exception) {
            Timber.e(e)
            hasError = true
        }

        try {
            binding!!.iconImageOutline.setImageResource(iconResourceOutline)
        } catch (e: Exception) {
            Timber.e(e)
            hasError = true
        }

        try {
            binding!!.iconImageNegative.setImageResource(iconResourceNegative)
        } catch (e: Exception) {
            Timber.e(e)
            hasError = true
        }

        try {
            binding!!.iconImagePossitiveTint.setImageResource(iconResourcePositive)
        } catch (e: Exception) {
            Timber.e(e)
            hasError = true
        }

        try {
            binding!!.iconImageOutlineTint.setImageResource(iconResourceOutline)
        } catch (e: Exception) {
            Timber.e(e)
            hasError = true
        }

        try {
            binding!!.iconImageNegativeTint.setImageResource(iconResourceNegative)
        } catch (e: Exception) {
            Timber.e(e)
            hasError = true
        }

        if (hasError) {
            binding!!.iconInput.error = "This drawable has errors"
        } else if (binding!!.automaticErrorCheck.isChecked) {
            nextDrawable()
        }
    }

    private fun nextDrawable() {
        count++
        if (count == iconNames!!.size) {
            count = 0
            binding!!.automaticErrorCheck.isChecked = false
            return
        }
        renderIconForPosition(count)
    }

    private fun loadCrashControl() {
        binding!!.crashButton.setOnClickListener { _: View? ->
            throw IllegalArgumentException("KA BOOOOOM!")
        }
    }

    private fun loadFeatureConfig() {
        binding!!.featureConfigButton.setOnClickListener { _: View? ->
            startActivity(
                FeatureConfigView::class.java,
                null,
                false,
                false,
                null,
            )
        }
    }
}
