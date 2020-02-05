package org.dhis2.Bindings

val String?.initials: String
    get() {
        val userNames = this
            ?.split(" ".toRegex())
            ?.dropLastWhile { it.isEmpty() }
            ?.toTypedArray()

        var userInit = ""
        userNames?.forEachIndexed { index, word ->
            if (index > 1) return@forEachIndexed
            userInit += word.first()
        }
        return userInit
    }
