package org.totschnig.myexpenses.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.evernote.android.state.State
import org.totschnig.myexpenses.dialog.ContribDialogFragment
import org.totschnig.myexpenses.model.ContribFeature
import org.totschnig.myexpenses.util.ShortcutHelper

// Booty: simplified — no billing, no IAP, no payment flows.
// All features are free; this activity just grants access.
class ContribInfoDialogActivity : ProtectedFragmentActivity() {
    @State
    var doFinishAfterMessageDismiss = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            ContribDialogFragment.newInstance(
                intent.getStringExtra(KEY_FEATURE),
                intent.getSerializableExtra(KEY_TAG)
            )
                .show(supportFragmentManager, "CONTRIB")
            supportFragmentManager.executePendingTransactions()
        }
    }

    override fun onMessageDialogDismissOrCancel() {
        if (doFinishAfterMessageDismiss) {
            finish(true)
        }
    }

    fun finish(canceled: Boolean) {
        val featureStringFromExtra = intent.getStringExtra(KEY_FEATURE)
        if (featureStringFromExtra != null) {
            val feature = ContribFeature.valueOf(featureStringFromExtra)
            val shouldCallFeature = licenceHandler.hasAccessTo(feature)
            if (callerIsContribIface()) {
                val i = Intent()
                i.putExtra(KEY_FEATURE, featureStringFromExtra)
                i.putExtra(KEY_TAG, intent.getSerializableExtra(KEY_TAG))
                if (shouldCallFeature) {
                    setResult(RESULT_OK, i)
                } else {
                    setResult(RESULT_CANCELED, i)
                }
            } else if (shouldCallFeature) {
                callFeature(feature)
            }
        }
        super.finish()
    }

    private fun callFeature(feature: ContribFeature) {
        if (feature === ContribFeature.SPLIT_TRANSACTION) {
            startActivity(ShortcutHelper.createIntentForNewSplit(this))
        }
    }

    private fun callerIsContribIface(): Boolean {
        val callingActivity = callingActivity ?: return false
        return try {
            val caller = Class.forName(callingActivity.className)
            ContribIFace::class.java.isAssignableFrom(caller)
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    companion object {
        const val KEY_FEATURE = "feature"
        const val KEY_TAG = "tag"

        fun getIntentFor(context: Context?, feature: ContribFeature?) =
            Intent(context, ContribInfoDialogActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                if (feature != null) {
                    putExtra(KEY_FEATURE, feature.name)
                }
            }
    }
}
