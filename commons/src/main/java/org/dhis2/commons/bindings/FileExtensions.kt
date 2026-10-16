package org.dhis2.commons.bindings

import java.io.File

fun isFilePathValid(filePath: String): Boolean = filePath.isNotEmpty() && File(filePath).exists()
