package org.dhis2.usescases.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.android.material.composethemeadapter.MdcTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.dhis2.BuildConfig
import org.dhis2.R

class CrashActivity : AppCompatActivity() {
    var config: CaocConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = CustomActivityOnCrash.getConfigFromIntent(intent)
        if (config == null) {
            finish()
            return
        }
        setContent {
            MdcTheme {
                Scaffold(
                    floatingActionButton = {
                        CrashGoBackButton {
                            goBack()
                        }
                    }
                ) {
                    CrashScreen(
                        crashReport = loadCrashReport(),
                        onCopy = { copyTextToClipboard(it) }
                    )
                }
            }
        }
    }

    private fun loadCrashReport() = CrashReport(
        buildVersion = BuildConfig.VERSION_NAME,
        buildDate = BuildConfig.BUILD_DATE,
        currentDate = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.getDefault()
        ).format(Date()),
        device = "%s %s".format(Build.MANUFACTURER, Build.MODEL),
        osVersion = Build.VERSION.RELEASE,
        stackTrace = CustomActivityOnCrash.getStackTraceFromIntent(intent) ?: "-"
    )

    private fun copyTextToClipboard(textToCopy: String) {
        val clipboard =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (textToCopy.isNotEmpty()) {
            val clip = ClipData.newPlainText("copy", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                this,
                getString(R.string.copied_text),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun goBack() {
        config?.let {
            CustomActivityOnCrash.restartApplication(this, it)
        }
    }
}

data class CrashReport(
    val buildVersion: String,
    val buildDate: String,
    val currentDate: String,
    val device: String,
    val osVersion: String,
    val stackTrace: String
)

@Composable
fun CrashScreen(crashReport: CrashReport, onCopy: (textToCopy: String) -> Unit) {
    Column(modifier = Modifier.fillMaxHeight()) {
        CrashHeader()
        CrashDeviceInfo(crashReport)
        CrashStackTraceInfo(crashReport.stackTrace) {
            onCopy(it)
        }
    }
}

@Composable
fun CrashHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = R.drawable.ic_dhis),
            contentDescription = "dhis2"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(
                id = R.string.customactivityoncrash_error_activity_error_occurred_explanation
            ),
            color = colorResource(id = R.color.textPrimary)
        )
    }
}

@Composable
fun CrashDeviceInfo(crashReport: CrashReport) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            fontSize = 14.sp,
            text = stringResource(R.string.customactivityoncrash_error_activity_build_version)
                .format(crashReport.buildVersion)
        )
        Text(
            fontSize = 14.sp,
            text = stringResource(R.string.customactivityoncrash_error_activity_buid_date)
                .format(crashReport.buildDate)
        )
        Text(
            fontSize = 14.sp,
            text = stringResource(R.string.customactivityoncrash_error_activity_current_date)
                .format(crashReport.currentDate)
        )
        Text(
            fontSize = 14.sp,
            text = stringResource(R.string.customactivityoncrash_error_activity_device)
                .format(crashReport.device)
        )
        Text(
            fontSize = 14.sp,
            text = stringResource(R.string.customactivityoncrash_error_activity_os_version)
                .format(crashReport.osVersion)
        )
    }
}

@Composable
fun CrashStackTraceInfo(stackTrace: String, onCopy: (textToCopy: String) -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 8.dp)

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
                .verticalScroll(state = scrollState)
        ) {
            Text(
                modifier = Modifier.fillMaxSize(),
                text = stackTrace,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
        TextButton(onClick = { onCopy(stackTrace) }) {
            Text(
                text = stringResource(
                    id = R.string.customactivityoncrash_error_activity_error_details_copy
                ).uppercase(),
                color = colorResource(id = R.color.colorPrimary)
            )
        }
    }
}

@Composable
fun CrashGoBackButton(onGoBack: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { onGoBack() },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.colorPrimary)
            )
        ) {
            Text(
                text = stringResource(
                    id = R.string.customactivityoncrash_error_activity_restart_app
                ).uppercase(),
                color = colorResource(id = R.color.primaryBgTextColor)
            )
        }
    }
}

@Preview
@Composable
fun ScreenPreview() {
    CrashScreen(
        crashReport = CrashReport(
            buildVersion = BuildConfig.VERSION_NAME,
            buildDate = "2020-01-01",
            currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            device = "%s %s".format(Build.MANUFACTURER, Build.MODEL),
            osVersion = Build.VERSION.RELEASE,
            stackTrace = "Error, Error,Error,\n Error, Error, Error,\nError, Error"
        )
    ) {}
}
