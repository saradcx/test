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

/**
 * Complete fix for SplashFragment with proper full-screen to edge-to-edge transition
 */
class SplashFragment : BaseFragment(R.layout.layout_splash_fragment) {

    private val viewModel by viewModels<SplashViewModel>()
    private var hasNavigated = false

    override fun onResume() {
        super.onResume()
        // Enable full-screen immersive mode for splash
        enableFullScreenImmersiveMode()
    }

    override fun onPause() {
        super.onPause()
        // Don't interfere with system UI here - let navigation handle it
    }

    override fun onStop() {
        super.onStop()
        // Only restore system UI if we're not navigating away
        if (!hasNavigated) {
            restoreSystemUIIfNeeded()
        }
    }

    override fun init(view: View) {
        // Use viewLifecycleOwner for proper lifecycle management
        viewModel.uiState.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate && !hasNavigated) {
                hasNavigated = true
                handleNavigation()
            } else if (!hasNavigated) {
                navigate(SplashFragmentDirections.actionSplashFragmentToOnBoardingScreenFragment())
                hasNavigated = true
            }
        }
    }

    /**
     * COMPLETE FIX: Proper full-screen immersive mode
     */
    private fun enableFullScreenImmersiveMode() {
        val window = requireActivity().window
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ approach
            window.setDecorFitsSystemWindows(false)
            
            // Hide system bars with immersive sticky behavior
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
     * COMPLETE FIX: Prepare edge-to-edge mode before navigation
     */
    private fun enableEdgeToEdgeForNextActivity() {
        val window = requireActivity().window
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ edge-to-edge setup
            window.setDecorFitsSystemWindows(false)
            
            // Show system bars but keep them translucent/transparent
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
            
            // Make status bar and navigation bar translucent
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
        } else {
            // Pre-Android 11 edge-to-edge setup
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
            
            // Make system bars translucent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
            }
        }
    }

    /**
     * COMPLETE FIX: Handle navigation with proper transition
     */
    private fun handleNavigation() {
        lifecycleScope.launch {
            // Prepare edge-to-edge mode first
            enableEdgeToEdgeForNextActivity()
            
            // Small delay for smooth transition
            delay(200)
            
            // Navigate based on authentication status
            if (isAuthenticated()) {
                navigate(SplashFragmentDirections.actionSplashFragmentToMainActivity())
                finishActivity()
            } else {
                navigate(SplashFragmentDirections.actionSplashFragmentToPhoneFragment())
            }
        }
    }

    /**
     * Restore system UI only if we're not navigating
     */
    private fun restoreSystemUIIfNeeded() {
        if (!hasNavigated) {
            val window = requireActivity().window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(true)
            }
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
