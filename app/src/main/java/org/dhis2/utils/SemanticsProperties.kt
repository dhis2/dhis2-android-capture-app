package org.dhis2.utils

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

val AdapterItemPosition = SemanticsPropertyKey<Int>("AdapterItemPosition")
var SemanticsPropertyReceiver.adapterItemPosition by AdapterItemPosition
val AdapterItemTitle = SemanticsPropertyKey<String>("AdapterItemTitle")
var SemanticsPropertyReceiver.adapterItemTitle by AdapterItemTitle
