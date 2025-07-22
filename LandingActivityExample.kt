package com.securnyx360.technician.landing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securnyx360.technician.R

/**
 * Example of how LandingActivity should be implemented to work with the fixed SplashFragment
 */
class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // DON'T set edge-to-edge here - let SplashFragment handle the transition
        // The SplashFragment will manage the transition from full-screen to edge-to-edge
        
        setContentView(R.layout.activity_landing)
        
        // Optional: Set up any activity-specific initialization
        initializeActivity()
    }
    
    private fun initializeActivity() {
        // Any landing activity specific setup
        // Window insets will be handled by individual fragments
    }
    
    override fun onResume() {
        super.onResume()
        // Don't interfere with fragment's system UI management
    }
}

/**
 * Example of how MainActivity should be implemented to receive edge-to-edge from splash
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge should already be enabled by SplashFragment
        // Just set up window insets handling for your content
        setupWindowInsets()
        
        setContentView(R.layout.activity_main)
        
        // Set up your navigation, toolbar, etc.
        setupNavigation()
    }
    
    private fun setupWindowInsets() {
        // Use the window insets utilities from the previous fixes
        val rootView = findViewById<android.view.View>(android.R.id.content) as? android.view.ViewGroup
        rootView?.let { root ->
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
                val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                val displayCutout = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.displayCutout())
                
                // Store insets for child views/fragments to use
                view.tag = WindowInsetsData(
                    left = maxOf(systemBars.left, displayCutout.left),
                    top = maxOf(systemBars.top, displayCutout.top),
                    right = maxOf(systemBars.right, displayCutout.right),
                    bottom = maxOf(systemBars.bottom, displayCutout.bottom)
                )
                
                // Don't consume insets - let fragments handle them
                insets
            }
            androidx.core.view.ViewCompat.requestApplyInsets(root)
        }
    }
    
    private fun setupNavigation() {
        // Your navigation setup here
        // Fragments will handle their own insets using the base fragment fixes
    }
    
    data class WindowInsetsData(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}
