package org.dhis2.commons.filters.workingLists

import androidx.compose.runtime.saveable.Saver

private const val TYPE_KEY = "type"
private const val UID_KEY = "uid"
private const val LABEL_KEY = "label"

private const val TYPE_PROGRAM_STAGE = "ProgramStageWorkingList"
private const val TYPE_TRACKED_ENTITY = "TrackedEntityInstanceWorkingList"
private const val TYPE_EVENT = "EventWorkingList"

internal val WorkingListItemSaver =
    Saver<WorkingListItem?, Map<String, String>>(
        save = { workingListItem ->
            workingListItem?.let {
                val map =
                    mutableMapOf(
                        UID_KEY to it.uid,
                        LABEL_KEY to it.label,
                    )

                when (it) {
                    is ProgramStageWorkingList -> map[TYPE_KEY] = TYPE_PROGRAM_STAGE
                    is TrackedEntityInstanceWorkingList -> map[TYPE_KEY] = TYPE_TRACKED_ENTITY
                    is EventWorkingList -> map[TYPE_KEY] = TYPE_EVENT
                }
                map
            }
        },
        restore = { map ->
            val type = map[TYPE_KEY]
            val uid = map[UID_KEY]
            val label = map[LABEL_KEY]

            if (type != null && uid != null && label != null) {
                when (type) {
                    TYPE_PROGRAM_STAGE -> ProgramStageWorkingList(uid, label)
                    TYPE_TRACKED_ENTITY -> TrackedEntityInstanceWorkingList(uid, label)
                    TYPE_EVENT -> EventWorkingList(uid, label)
                    else -> null
                }
            } else {
                null
            }
        },
    )
