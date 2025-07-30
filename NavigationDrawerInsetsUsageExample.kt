import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Example usage of NavigationDrawerInsetsUtils.kt functions
 * This demonstrates how to properly handle window insets in various navigation drawer scenarios.
 */
class NavigationDrawerInsetsUsageExample : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var mainContent: FrameLayout
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge for the activity
        enableEdgeToEdge()
        
        setupViews()
        setupInsets()
    }

    private fun setupViews() {
        // Initialize your views here
        // drawerLayout = findViewById(R.id.drawer_layout)
        // navigationView = findViewById(R.id.navigation_view)
        // toolbar = findViewById(R.id.toolbar)
        // mainContent = findViewById(R.id.main_content)
        // fab = findViewById(R.id.fab)
    }

    private fun setupInsets() {
        // 1. Setup DrawerLayout insets - this should be done first
        drawerLayout.setDrawerLayoutInsets(propagateInsets = true)

        // 2. Setup main content insets - respects all system insets
        mainContent.setDrawerMainContentInsets()

        // 3. Setup navigation view insets - typically for left drawer
        navigationView.setLeftDrawerInsets()
        
        // Alternative: For right drawer, use:
        // navigationView.setRightDrawerInsets()

        // 4. Setup toolbar insets - ensures it doesn't overlap with status bar
        toolbar.setDrawerToolbarInsets()

        // 5. Setup FAB insets - positions it correctly with navigation bars
        fab.setDrawerFabInsets()

        // 6. Example of custom drawer header handling
        val headerView = navigationView.getHeaderView(0)
        headerView.setDrawerHeaderInsets()

        // 7. Example of drawer menu content
        val menuContainer = navigationView.findViewById<LinearLayout>(R.id.menu_container)
        menuContainer?.setDrawerMenuInsets()

        // 8. Example of conditional insets based on drawer state
        val conditionalView = findViewById<FrameLayout>(R.id.conditional_content)
        conditionalView?.setConditionalDrawerInsets(
            drawerLayout = drawerLayout,
            drawerGravity = GravityCompat.START
        )

        // 9. Example of animated insets for smooth transitions
        val animatedView = findViewById<FrameLayout>(R.id.animated_content)
        animatedView?.setAnimatedDrawerInsets(
            drawerLayout = drawerLayout,
            drawerGravity = GravityCompat.START,
            animationDuration = 300L
        )
    }

    /**
     * Example of handling different drawer configurations
     */
    private fun setupDifferentDrawerConfigurations() {
        
        // For a drawer that should extend to all edges (like a backdrop)
        val backdropDrawer = findViewById<NavigationView>(R.id.backdrop_drawer)
        backdropDrawer?.setDrawerSideOnlyInsets()

        // For drawer content that needs full edge-to-edge
        val fullScreenDrawer = findViewById<NavigationView>(R.id.fullscreen_drawer)
        fullScreenDrawer?.setDrawerContentInsets()

        // For bottom navigation in drawer layout
        val bottomNav = findViewById<LinearLayout>(R.id.bottom_navigation)
        bottomNav?.setDrawerBottomInsets()

        // For scrim/overlay views
        val scrimView = findViewById<FrameLayout>(R.id.scrim_overlay)
        scrimView?.setDrawerScrimInsets()
    }

    /**
     * Example of using the base inset functions for custom scenarios
     */
    private fun setupCustomInsetScenarios() {
        
        // Custom view that only needs side insets
        val sideOnlyView = findViewById<FrameLayout>(R.id.side_only_view)
        sideOnlyView?.setSideInsets()

        // Custom view that needs top and side insets
        val topAndSideView = findViewById<FrameLayout>(R.id.top_and_side_view)
        topAndSideView?.setTopAndSideInsets()

        // Custom view that needs bottom and side insets
        val bottomAndSideView = findViewById<FrameLayout>(R.id.bottom_and_side_view)
        bottomAndSideView?.setBottomAndSideInsets()

        // Custom inset handling with lambda
        val customView = findViewById<FrameLayout>(R.id.custom_view)
        customView?.setEdgeToEdgeInsets { insets ->
            // Custom logic for applying insets
            leftMargin = insets.left / 2  // Half the left inset
            topMargin = insets.top
            rightMargin = insets.right / 2  // Half the right inset
            bottomMargin = 0  // No bottom margin
        }
    }

    /**
     * Example of handling different drawer states dynamically
     */
    private fun handleDrawerStateChanges() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) {
                // Handle slide animation if needed
            }

            override fun onDrawerOpened(drawerView: android.view.View) {
                // Reapply insets when drawer opens
                val dynamicContent = findViewById<FrameLayout>(R.id.dynamic_content)
                dynamicContent?.setConditionalDrawerInsets(
                    drawerLayout = drawerLayout,
                    drawerGravity = GravityCompat.START
                )
            }

            override fun onDrawerClosed(drawerView: android.view.View) {
                // Reapply insets when drawer closes
                val dynamicContent = findViewById<FrameLayout>(R.id.dynamic_content)
                dynamicContent?.setConditionalDrawerInsets(
                    drawerLayout = drawerLayout,
                    drawerGravity = GravityCompat.START
                )
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Handle state changes if needed
            }
        })
    }

    /**
     * Example of handling multiple drawers (left and right)
     */
    private fun setupMultipleDrawers() {
        val leftDrawer = findViewById<NavigationView>(R.id.left_drawer)
        val rightDrawer = findViewById<NavigationView>(R.id.right_drawer)

        // Left drawer insets
        leftDrawer?.setLeftDrawerInsets()

        // Right drawer insets
        rightDrawer?.setRightDrawerInsets()

        // Main content that works with both drawers
        mainContent.setDrawerMainContentInsets()

        // Handle content differently based on which drawer is open
        val adaptiveContent = findViewById<FrameLayout>(R.id.adaptive_content)
        adaptiveContent?.setEdgeToEdgeInsets { insets ->
            val isLeftOpen = drawerLayout.isDrawerOpen(GravityCompat.START)
            val isRightOpen = drawerLayout.isDrawerOpen(GravityCompat.END)

            when {
                isLeftOpen -> {
                    // Left drawer is open
                    rightMargin = insets.right
                    topMargin = insets.top
                    bottomMargin = insets.bottom
                }
                isRightOpen -> {
                    // Right drawer is open
                    leftMargin = insets.left
                    topMargin = insets.top
                    bottomMargin = insets.bottom
                }
                else -> {
                    // Both drawers closed
                    leftMargin = insets.left
                    topMargin = insets.top
                    rightMargin = insets.right
                    bottomMargin = insets.bottom
                }
            }
        }
    }
}