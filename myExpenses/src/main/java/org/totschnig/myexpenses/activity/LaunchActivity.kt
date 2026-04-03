package org.totschnig.myexpenses.activity

import android.annotation.SuppressLint
import com.vmadalin.easypermissions.EasyPermissions.somePermissionPermanentlyDenied
import org.totschnig.myexpenses.provider.PlannerUtils
import org.totschnig.myexpenses.util.PermissionHelper
import javax.inject.Inject

// Booty: simplified — no billing, no licence expiration checks, no IAP
@SuppressLint("CustomSplashScreen")
abstract class LaunchActivity : ProtectedFragmentActivity() {

    @Inject
    lateinit var plannerUtils: PlannerUtils

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        super.onPermissionsDenied(requestCode, perms)
        if (requestCode == PermissionHelper.PERMISSIONS_REQUEST_WRITE_CALENDAR &&
            (PermissionHelper.PermissionGroup.CALENDAR.androidPermissions.any { perms.contains(it) }) &&
            somePermissionPermanentlyDenied(
                this,
                PermissionHelper.PermissionGroup.CALENDAR.androidPermissions
            )
        ) {
            plannerUtils.removePlanner(prefHandler)
        }
    }
}
