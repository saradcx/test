package com.securnyx360.technician.baseui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Created by Subhankar on August'28 2020
 * Fixed version addressing critical bugs
 */

abstract class BaseActivity : AppCompatActivity() {

    // BUG 1 FIX: Use correct onCreate signature - the original was never called!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // BUG 2 FIX: Enable edge-to-edge support for modern Android versions
        enableEdgeToEdge()
        
        // Initialize activity-specific setup
        initializeActivity()
    }
    
    /**
     * BUG 3 FIX: Added proper edge-to-edge support
     */
    private fun enableEdgeToEdge() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }
    
    /**
     * BUG 4 FIX: Added proper window insets handling
     */
    protected fun setupWindowInsets() {
        val rootView = findViewById<android.view.View>(android.R.id.content) as? android.view.ViewGroup
        rootView?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
                
                // Store insets for child views to use
                view.tag = WindowInsetsData(
                    left = maxOf(systemBars.left, displayCutout.left),
                    top = maxOf(systemBars.top, displayCutout.top),
                    right = maxOf(systemBars.right, displayCutout.right),
                    bottom = maxOf(systemBars.bottom, displayCutout.bottom)
                )
                
                // Don't consume insets - let child views handle them
                insets
            }
            ViewCompat.requestApplyInsets(root)
        }
    }
    
    /**
     * Override this method to perform activity-specific initialization
     */
    protected open fun initializeActivity() {
        // Default implementation - can be overridden by subclasses
    }
    
    /**
     * BUG 5 FIX: Made abstract method more robust with null safety
     */
    abstract fun setChildFragment(childFragment: BaseFragment?)
    
    /**
     * Helper data class for storing window insets
     */
    data class WindowInsetsData(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}
