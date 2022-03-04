package org.dhis2.usescases.settings

import java.io.File

fun deleteCache(dir: File?): Boolean{
    return if (dir != null && dir.isDirectory) {
        val children: Array<String> = dir.list()
        for (aChildren in children) {
            val success = deleteCache(File(dir, aChildren))
            if (!success) {
                return false
            }
        }
        dir.delete()
    } else if (dir != null && dir.isFile) {
        dir.delete()
    } else {
        false
    }
}