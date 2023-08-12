package org.dhis2.utils

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "small font",
    group = "font scales",
    fontScale = 0.5f
)
@Preview(
    name = "large font",
    group = "font scales",
    fontScale = 1.5f
)
annotation class FontScalePreviews

@Preview(
    name = "Phone (Compact)",
    group = "Devices",
    showSystemUi = true,
    showBackground = true,
    device = "spec:shape=Normal,width=320,height=427,unit=dp,dpi=480"
)
@Preview(
    name = "Phone (Medium)",
    group = "Devices",
    showSystemUi = true,
    showBackground = true,
    device = Devices.PIXEL_4
)
@Preview(
    name = "Tablet",
    group = "Devices",
    showSystemUi = true,
    showBackground = true,
    device = Devices.PIXEL_C
)
@Preview(
    name = "Foldable",
    group = "Devices",
    showSystemUi = true,
    showBackground = true,
    device = Devices.FOLDABLE
)
annotation class DevicePreviews

@Preview(
    name = "Phone (Medium)",
    group = "Devices",
    showSystemUi = true,
    showBackground = true,
    device = Devices.PIXEL_4
)
annotation class SampleDevicePreview

@Preview(
    name = "light-theme",
    group = "themes",
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "dark-theme",
    group = "themes",
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class ThemePreviews

@FontScalePreviews
@DevicePreviews
@ThemePreviews
annotation class CompletePreviews
