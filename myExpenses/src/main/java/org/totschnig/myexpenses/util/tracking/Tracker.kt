package org.totschnig.myexpenses.util.tracking

import android.content.Context
import android.os.Bundle
import org.totschnig.myexpenses.util.licence.LicenceStatus

interface Tracker {
    fun init(context: Context, licenceStatus: LicenceStatus?)
    fun logEvent(eventName: String, params: Bundle?)
    fun setEnabled(enabled: Boolean)

    fun trackCommand(command: String) {
        logEvent(EVENT_DISPATCH_COMMAND, Bundle().apply {
            putString(EVENT_PARAM_ITEM_ID, command)
        })
    }

    companion object {
        const val EVENT_DISPATCH_COMMAND = "dispatch_command"
        const val EVENT_SELECT_OPERATION_TYPE = "select_operation_type"
        const val EVENT_CONTRIB_DIALOG_CANCEL = "contrib_dialog_cancel"
        const val EVENT_PREFERENCE_CLICK = "preference_click"
        const val EVENT_RATING_DIALOG = "rating_dialog"
        const val EVENT_BACKUP_PERFORMED = "backup_performed"
        const val EVENT_BACKUP_SKIPPED = "backup_skipped"
        const val EVENT_RESTORE_FINISHED = "restore_finished"

        const val EVENT_PARAM_PACKAGE = "package"
        const val EVENT_PARAM_OPERATION_TYPE = "operation_type"
        const val EVENT_PARAM_ITEM_ID = "item_id"
        const val EVENT_PARAM_BUTTON_ID = "button_id"
    }
}