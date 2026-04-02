package org.totschnig.myexpenses.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import org.totschnig.myexpenses.R
import org.totschnig.myexpenses.databinding.OnboardingBinding
import org.totschnig.myexpenses.dialog.ConfirmationDialogFragment
import org.totschnig.myexpenses.fragment.OnBoardingPrivacyFragment
import org.totschnig.myexpenses.fragment.OnboardingDataFragment
import org.totschnig.myexpenses.fragment.OnboardingUiFragment
import org.totschnig.myexpenses.preference.PrefKey
import org.totschnig.myexpenses.util.crashreporting.CrashHandler
import org.totschnig.myexpenses.util.distrib.DistributionHelper.versionNumber

class OnboardingActivity : RestoreActivity() {
    private lateinit var binding: OnboardingBinding
    private lateinit var pagerAdapter: MyPagerAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override val drawToTopEdge = true

    override val drawToBottomEdge = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            try {
                prefHandler.setDefaultValues(this)
            } catch (e: Exception) {
                //According to report, setDefaultValues fails in some scenario
                //where there is a value of the wrong type already present
                //java.lang.ClassCastException: java.lang.Boolean cannot be cast to java.lang.String
                //maybe when data is restored via Play Store app backup
                CrashHandler.report(e)
            }
        }
        super.onCreate(savedInstanceState)
        binding = OnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pagerAdapter = MyPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = 2
        ViewCompat.setOnApplyWindowInsetsListener(binding.pageIndicatorView) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updateLayoutParams<MarginLayoutParams> {
                topMargin = bars.top
            }
            insets
        }
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val currentItem = binding.viewPager.currentItem
                if (currentItem > 0) {
                    binding.viewPager.currentItem = currentItem - 1
                } else {
                    onBackPressedDispatcher.onBackPressed()

                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onBackPressedCallback.isEnabled = position > 0
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu) = false //skip help

    fun navigateNext() {
        val currentItem = binding.viewPager.currentItem
        binding.viewPager.setCurrentItem(currentItem + 1, true)
    }

    private val privacyFragment: OnBoardingPrivacyFragment?
        get() = getFragmentAtPosition(1) as? OnBoardingPrivacyFragment

    private fun getFragmentAtPosition(pos: Int) =
        supportFragmentManager.findFragmentByTag(pagerAdapter.getFragmentName(pos))

    fun start() {
        prefHandler.putInt(PrefKey.CURRENT_VERSION, versionNumber)
        prefHandler.putInt(PrefKey.FIRST_INSTALL_VERSION, versionNumber)
        val intent = Intent(this, MyExpensesV2::class.java)
        startActivity(intent)
        finish()
    }

    override fun onPostRestoreTask(result: Result<Unit>) {
        super.onPostRestoreTask(result)
        result.onSuccess {
            restartAfterRestore()
        }
    }

    private class MyPagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {
        fun getFragmentName(currentPosition: Int): String {
            //https://stackoverflow.com/a/61178226/1199911
            return "f" + getItemId(currentPosition)
        }


        override fun createFragment(position: Int) = when (position) {
            0 -> OnboardingUiFragment.newInstance()
            1 -> OnBoardingPrivacyFragment.newInstance()
            else -> OnboardingDataFragment.newInstance()
        }

        override fun getItemCount() = 3
    }

    override fun onNegative(args: Bundle) {
        if (args.getInt(ConfirmationDialogFragment.KEY_COMMAND_NEGATIVE) == R.id.ENCRYPT_CANCEL_COMMAND) {
            prefHandler.putBoolean(PrefKey.ENCRYPT_DATABASE, false)
            privacyFragment?.setupMenu()
        }
    }

    override val snackBarContainerId: Int
        get() {
            return binding.viewPager.id
        }
}
