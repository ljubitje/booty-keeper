package org.totschnig.myexpenses.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.totschnig.myexpenses.model.ContribFeature
import org.totschnig.myexpenses.util.ShortcutHelper

// Booty: all features are free — this activity immediately grants access and finishes.
// Kept as a shell so that intent-based callers (notifications, shortcuts, activity results) still work.
class ContribInfoDialogActivity : ProtectedFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        grantAndFinish()
    }

    private fun grantAndFinish() {
        val featureStringFromExtra = intent.getStringExtra(KEY_FEATURE)
        if (featureStringFromExtra != null) {
            val feature = ContribFeature.valueOf(featureStringFromExtra)
            if (callerIsContribIface()) {
                val i = Intent()
                i.putExtra(KEY_FEATURE, featureStringFromExtra)
                i.putExtra(KEY_TAG, intent.getSerializableExtra(KEY_TAG))
                setResult(RESULT_OK, i)
            } else {
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
