package com.securnyx360.a360towerguard.baseUi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ImageButton
import com.securnyx360.a360towerguard.BuildConfig
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.securnyx360.a360towerguard.R
import com.securnyx360.a360towerguard.databinding.ActivityMainBinding
import com.securnyx360.a360towerguard.home.view.HomeFragment
import com.securnyx360.a360towerguard.landing.LandingActivity
import com.securnyx360.a360towerguard.util.changeStatusBar
import com.securnyx360.a360towerguard.util.hideKeyboard
import com.securnyx360.network.KioskPref
import com.securnyx360.network.model.UserPref
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import timber.log.Timber
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.securnyx360.a360towerguard.util.makeToast
import setBottomAndSideInsets
import setEdgeToEdgeInsets
import setSideInsets
import setTopAndSideInsets

class MainActivity : BaseActivity(), View.OnClickListener, DrawerLayout.DrawerListener {

    private val navHost by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerView: View
    private lateinit var menu: View
    lateinit var headerText: TextView
    lateinit var btnBack: ImageButton
    private lateinit var hamburgerIcon: ImageButton
    lateinit var dropDownIcon: ImageView
    lateinit var name: TextView
    lateinit var drawerLayout: DuoDrawerLayout
    val Tag = "MainActivity"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        navController = navHost.navController
        UpdateHelper.checkUpdate(this)

        // Setup the bottom navigation view with navController
        setUpNavigationDrawer()
        setupBottomNavigation(navController)
        // Apply insets to the root view after setContentView
//        applyWindowInsetsToRoot()
        setupWindowInsetsHandling()
        binding.menuItem.apply {
            findViewById<TextView>(R.id.versionCodeTxt).text =
                "Version Code: ${BuildConfig.VERSION_NAME}"
        }
        setUserNameValue(UserPref.propertyName)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestination =
                    findNavController(R.id.nav_host_fragment).currentDestination?.id

                if (currentDestination == R.id.homeFragment) {
                    if (!KioskPref.lockStatus) {
                        isEnabled = false // allow back to propagate
                        onBackPressedDispatcher.onBackPressed()
                    }
                    // else: do nothing, block back
                } else {
                    isEnabled = false // allow back to propagate
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    /**
     * Applies system window insets as padding to the activity's root content view.
     * This pushes content away from the system bars (status bar, navigation bar).
     */
    private fun applyWindowInsetsToRoot() {
        val rootView = findViewById<View>(android.R.id.content) as? ViewGroup
        rootView ?: return

        rootView.setOnApplyWindowInsetsListener { view, insets ->
            val effectiveInsets = insets.getSystemBarInsets()
            view.setPadding(
                effectiveInsets.left,
                effectiveInsets.top,
                effectiveInsets.right,
                effectiveInsets.bottom
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsets.CONSUMED
            } else {
                @Suppress("DEPRECATION")
                insets.consumeSystemWindowInsets()
            }
        }
        rootView.requestApplyInsets()
    }

    fun WindowInsets.getSystemBarInsets(): CustomInsets {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bars = getInsets(WindowInsets.Type.systemBars())
            CustomInsets(bars.left, bars.top, bars.right, bars.bottom)
        } else {
            @Suppress("DEPRECATION")
            CustomInsets(
                systemWindowInsetLeft,
                systemWindowInsetTop,
                systemWindowInsetRight,
                systemWindowInsetBottom
            )
        }
    }

    data class CustomInsets(val left: Int, val top: Int, val right: Int, val bottom: Int)

    override fun onResume() {
        super.onResume()
        UpdateHelper.checkUpdate(this)
    }


    private fun setupBottomNavigation(navController: NavController) {
        // Setup the bottom navigation view with navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNavigationView.setupWithNavController(navController)
    }

    private fun initialize() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        headerView = binding.root.findViewById(R.id.headerLayout)
        menu = binding.root.findViewById(R.id.menuItem)
        drawerLayout = binding.drawerLayout
        btnBack = headerView.findViewById(R.id.ib_back_btn)
        headerText = headerView.findViewById(R.id.tv_header)
        hamburgerIcon = headerView.findViewById(R.id.ib_hamburger)
        dropDownIcon = headerView.findViewById(R.id.dropDownIcon)
        name = menu.findViewById(R.id.tvUserName)
        binding.bottomNav.itemIconTintList = null
        setBottomNavigationVisibility(View.GONE)
        drawerLayout = binding.drawerLayout
        drawerLayout.setDrawerListener(this)
    }

    private fun setUpNavigationDrawer() {
//        binding.headerLayout.apply {
//            tvHeader.setTopAndSideInsets()
//            dropDownIcon.setTopAndSideInsets()
//            ibBackBtn.setTopAndSideInsets()
//        }
        binding.headerLayout.apply {
            findViewById<ImageButton>(R.id.ib_hamburger).setOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
        }

        val menuView: View = drawerLayout.menuView
        val ll_Home: LinearLayout = menuView.findViewById(R.id.home)
        val ll_Resident_Directory: LinearLayout = menuView.findViewById(R.id.resident_Directory)
        val ll_Kiosk: LinearLayout = menuView.findViewById(R.id.kiosk)
        val ll_Reports: LinearLayout = menuView.findViewById(R.id.reports)
        val ll_Logout: LinearLayout = menuView.findViewById(R.id.logout)
        ll_Home.setOnClickListener(this)
        ll_Resident_Directory.setOnClickListener(this)
        ll_Kiosk.setOnClickListener(this)
        ll_Reports.setOnClickListener(this)
        ll_Logout.setOnClickListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
    
    /**
     * FIXED: Proper window insets handling for DuoDrawerLayout
     */
    private fun setupWindowInsetsHandling() {
        // CRITICAL: Enable edge-to-edge FIRST
        enableEdgeToEdge()
        
        // Apply insets only to views that actually exist in your layout
        
        // Header needs top and side insets to avoid status bar overlap
        try {
            findViewById<View>(R.id.headerLayout).setTopAndSideInsets(propagateInsets = true)
        } catch (e: Exception) {
            Log.w(Tag, "headerLayout not found for insets: ${e.message}")
        }

        // Bottom navigation needs bottom and side insets
        try {
            findViewById<BottomNavigationView>(R.id.bottomNav).setBottomAndSideInsets(propagateInsets = true)
        } catch (e: Exception) {
            Log.w(Tag, "bottomNav not found for insets: ${e.message}")
        }

        // The main content area (LinearLayout with id="content") should handle side insets only
        try {
            findViewById<LinearLayout>(R.id.content).setEdgeToEdgeInsets(propagateInsets = true) { insets ->
                // Apply only side margins to content, let header and bottom nav handle top/bottom
                leftMargin = insets.left
                rightMargin = insets.right
            }
        } catch (e: Exception) {
            Log.w(Tag, "content layout not found for insets: ${e.message}")
        }
        
        // DON'T apply insets to DuoDrawerLayout - it manages its own layout
        // This was causing the drawer to always be visible
        
        // Apply insets to the DuoMenuView (the actual drawer menu content) if it exists
        try {
            findViewById<View>(R.id.duoMenuView).setSideInsets(propagateInsets = true)
        } catch (e: Exception) {
            Log.w(Tag, "duoMenuView not found for insets: ${e.message}")
        }
        
        // If you have a regular NavigationView, apply insets to it
        try {
            findViewById<NavigationView>(R.id.nav_view)?.setSideInsets(propagateInsets = true)
        } catch (e: Exception) {
            Log.w(Tag, "nav_view not found for insets: ${e.message}")
        }
    }
    
    /**
     * Enable edge-to-edge display
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

    fun setBottomNavigationVisibility(visibility: Int) {
        binding.bottomNav.visibility = visibility
    }

    private fun setUpNavGraph(navGraphId: Int) {
        navHost.findNavController().setGraph(navGraphId)
        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
    }

//    private fun setUpNavGraph(navGraphId: Int) {
//        val navHostFragment: NavHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val inflater = navHostFragment.navController.navInflater
//        val startDestinationId = inflater.inflate(navGraphId).startDestinationId
//        NavigationUtils.addGraphDestinations(findNavController(R.id.nav_host_fragment), navGraphId)
//        navController.navigate(startDestinationId)
//        overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
//    }

    fun switchTab(@IdRes tabId: Int) {
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = tabId
    }

    fun setHeaderText(text: CharSequence?) {
        headerText.text = text
    }

    fun setUserNameValue(nameValue: String) {
        Timber.d("Setting User Name $nameValue")
        name.text = nameValue
    }

    fun setHeaderViewVisibility(visibility: Int) {
        headerView.visibility = visibility
    }

    fun setBackButtonVisibility(visibility: Int) {
        btnBack.visibility = visibility
    }

    fun setHamburgerIconVisibility(visibility: Int) {
        hamburgerIcon.visibility = visibility
    }

    fun setDropDownIconVisibility(visibility: Int) {
        dropDownIcon.visibility = visibility
    }

    fun requestHeaderLayout() {
        headerView.requestLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UpdateHelper.handleActivityResult(requestCode, resultCode, data, this)
    }
//    override fun onBackPressed() {
//        when (findNavController(R.id.nav_host_fragment).currentDestination?.id) {
//            R.id.homeFragment -> {
//                if (!KioskPref.lockStatus) {
//                    super.onBackPressed()
//                }
//            }
//            else -> super.onBackPressed()
//        }
//    }


    private fun finishAndRestartActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    fun moveToLoginFragment() {
        val intent = Intent(this, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onClick(view: View) {
        val activeFragment = findNavController(R.id.nav_host_fragment).currentDestination?.id
        when (view.id) {
            R.id.home -> {
                if (activeFragment != R.id.homeFragment) navController.navigate(R.id.toHomeNavigation)
            }

            R.id.resident_Directory -> {
                if (activeFragment != R.id.residentDirectoryFragment) navController.navigate(R.id.toResidentDirectoryNavigation)
            }

            R.id.kiosk -> {
                if (activeFragment != R.id.kioskFragment) navController.navigate(R.id.toKioskNavigation)
            }

            R.id.reports -> {
                if (activeFragment != R.id.reportFragment) navController.navigate(R.id.toReportNavigation)
            }

            R.id.logout -> {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val currentFragment = navHostFragment.childFragmentManager.fragments[0]
                if (currentFragment is HomeFragment) {
                    currentFragment.logout()
                }
            }
        }
        drawerLayout.closeDrawer()
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerOpened(drawerView: View) {
        binding.content.background =
            ContextCompat.getDrawable(this@MainActivity, R.drawable.rounded_closed_view)
        changeStatusBar(true)
    }

    override fun onDrawerClosed(drawerView: View) {
        binding.content.background =
            ContextCompat.getDrawable(this@MainActivity, R.drawable.normal_view)
        changeStatusBar(false)
    }

    override fun onDrawerStateChanged(newState: Int) {
        hideKeyboard()
    }
}