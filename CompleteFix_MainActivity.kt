package com.securnyx360.technician.main

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout
import com.securnyx360.technician.R
import com.securnyx360.technician.baseui.BaseFragment

/**
 * Fixed MainActivity with proper edge-to-edge and top padding handling
 */
class MainActivity : AppCompatActivity() {

    // Views that need insets handling
    lateinit var headerView: AppBarLayout
    lateinit var headerText: TextView
    lateinit var btnBack: View
    private var currentFragment: BaseFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // CRITICAL: Enable edge-to-edge FIRST
        enableEdgeToEdge()
        
        setContentView(R.layout.activity_main)
        
        // Initialize views
        initializeViews()
        
        // CRITICAL: Set up window insets handling AFTER views are initialized
        setupWindowInsetsHandling()
        
        // Initialize other components
        initializeActivity()
    }
    
    private fun initializeViews() {
        headerView = findViewById(R.id.header_view)
        headerText = findViewById(R.id.header_text)
        btnBack = findViewById(R.id.btn_back)
    }
    
    /**
     * CRITICAL FIX: Proper edge-to-edge implementation
     */
    private fun enableEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ approach
            window.setDecorFitsSystemWindows(false)
        } else {
            // Pre-Android 11 approach
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
    }
    
    /**
     * CRITICAL FIX: Proper window insets handling that fixes top padding issues
     */
    private fun setupWindowInsetsHandling() {
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            
            // Calculate effective insets
            val effectiveInsets = WindowInsetsData(
                left = maxOf(systemBars.left, displayCutout.left),
                top = maxOf(systemBars.top, displayCutout.top),
                right = maxOf(systemBars.right, displayCutout.right),
                bottom = maxOf(systemBars.bottom, displayCutout.bottom)
            )
            
            // Store insets for fragments to use
            view.tag = effectiveInsets
            
            // CRITICAL FIX: Apply top insets to header view immediately
            applyInsetsToHeaderView(effectiveInsets)
            
            // Don't consume insets - let fragments handle them for their content
            insets
        }
        
        // Request insets application
        ViewCompat.requestApplyInsets(rootView)
    }
    
    /**
     * CRITICAL FIX: Apply insets specifically to header view for proper top padding
     */
    private fun applyInsetsToHeaderView(insets: WindowInsetsData) {
        headerView.apply {
            // Apply top inset as padding to push header below status bar
            setPadding(
                paddingLeft,
                insets.top, // This fixes the top padding issue
                paddingRight,
                paddingBottom
            )
        }
        
        // Also handle any other top-level views that need insets
        applyInsetsToTopLevelViews(insets)
    }
    
    /**
     * Apply insets to other top-level views if needed
     */
    private fun applyInsetsToTopLevelViews(insets: WindowInsetsData) {
        // Apply to any other views that need system bar insets
        // For example, if you have a bottom navigation:
        // bottomNavigation?.setPadding(0, 0, 0, insets.bottom)
    }
    
    private fun initializeActivity() {
        // Any other initialization
        setupNavigation()
        setupToolbar()
    }
    
    private fun setupNavigation() {
        // Your navigation setup
    }
    
    private fun setupToolbar() {
        // Your toolbar setup
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    
    /**
     * FIXED: Improved fragment management
     */
    fun setChildFragment(childFragment: BaseFragment?) {
        currentFragment = childFragment
        
        // Re-apply insets when fragment changes
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        (rootView.tag as? WindowInsetsData)?.let { insets ->
            applyInsetsToHeaderView(insets)
        }
    }
    
    /**
     * Navigation methods
     */
    fun slideUpNavBar() {
        // Your navigation animation
    }
    
    fun slideDownNavBar() {
        // Your navigation animation
    }
    
    fun navigateTo(menuRes: Int) {
        // Your navigation logic
    }
    
    /**
     * IMPROVED: Better insets data management
     */
    fun getWindowInsets(): WindowInsetsData? {
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        return rootView.tag as? WindowInsetsData
    }
    
    data class WindowInsetsData(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}
