package com.securnyx360.technician.splash

import android.os.Build
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.securnyx360.technician.R
import com.securnyx360.technician.baseui.BaseFragment
import com.securnyx360.technician.splash.viewmodel.SplashViewModel
import com.securnyx360.technician.util.finishActivity
import com.securnyx360.technician.util.isAuthenticated
import com.securnyx360.technician.util.navigate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment(R.layout.layout_splash_fragment) {

    private val viewModel by viewModels<SplashViewModel>()

    override fun onResume() {
        super.onResume()
        // BUG FIX 1: Enable full-screen immersive mode with proper API level handling
        enableFullScreenImmersiveMode()
    }

    override fun onPause() {
        super.onPause()
        // BUG FIX 2: Don't revert to normal view - let the next activity/fragment handle it
        // The original code was interfering with edge-to-edge in subsequent screens
        // requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    override fun onStop() {
        super.onStop()
        // BUG FIX 3: Properly restore system UI when fragment is stopped
        restoreSystemUI()
    }

    override fun init(view: View) {
        // BUG FIX 4: Use viewLifecycleOwner instead of 'this' to prevent crashes
        viewModel.uiState.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                handleNavigation()
            } else {
                navigate(SplashFragmentDirections.actionSplashFragmentToOnBoardingScreenFragment())
            }
        }
    }

    /**
     * BUG FIX 5: Separate navigation logic and add delay for smooth transition
     */
    private fun handleNavigation() {
        // Add a small delay to ensure smooth transition
        lifecycleScope.launch {
            delay(300) // Small delay for better UX
            
            if (isAuthenticated()) {
                // BUG FIX 6: Enable edge-to-edge before navigating to MainActivity
                enableEdgeToEdgeForNextActivity()
                navigate(SplashFragmentDirections.actionSplashFragmentToMainActivity())
                finishActivity()
            } else {
                // BUG FIX 7: Enable edge-to-edge before navigating to phone fragment
                enableEdgeToEdgeForNextActivity()
                navigate(SplashFragmentDirections.actionSplashFragmentToPhoneFragment())
            }
        }
    }

    /**
     * BUG FIX 8: Proper full-screen immersive mode with API level handling
     */
    private fun enableFullScreenImmersiveMode() {
        val window = requireActivity().window
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ approach
            window.setDecorFitsSystemWindows(false)
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        } else {
            // Pre-Android 11 approach
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
    }

    /**
     * BUG FIX 9: Prepare edge-to-edge mode for next activity/fragment
     */
    private fun enableEdgeToEdgeForNextActivity() {
        val window = requireActivity().window
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ edge-to-edge
            window.setDecorFitsSystemWindows(false)
            // Clear immersive flags but keep layout flags for edge-to-edge
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        } else {
            // Pre-Android 11 edge-to-edge
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
    }

    /**
     * BUG FIX 10: Properly restore system UI when fragment is stopped
     */
    private fun restoreSystemUI() {
        // Only restore if we're not navigating to another screen
        if (!isNavigatingAway()) {
            val window = requireActivity().window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(true)
            }
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    /**
     * Helper method to check if we're navigating away
     */
    private fun isNavigatingAway(): Boolean {
        return viewModel.uiState.value == true
    }
}
