package com.securnyx360.technician.baseui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.securnyx360.technician.R
import com.securnyx360.technician.contract.BackPressedListener
import com.securnyx360.technician.landing.LandingActivity
import com.securnyx360.technician.main.MainActivity
import com.securnyx360.technician.state.observeEvent
import com.securnyx360.technician.util.*
import com.securnyx360.technician.util.permissionUtil.PermissionContract
import com.securnyx360.technician.util.permissionUtil.PermissionUtil.Companion.RC_PARAM_STORAGE_READ
import com.securnyx360.technician.util.permissionUtil.PermissionUtil.Companion.RC_PARAM_STORAGE_WRITE
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.File

/**
 * Complete fix for BaseFragment with proper insets handling
 */
abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes),
    BaseView, BackPressedListener, PhotoContract {

    val permissionUtil: PermissionContract by inject { parametersOf(requireActivity()) }
    private val photoUtil: PhotoUtil by inject { parametersOf(this, permissionUtil) }
    private val viewModels = mutableListOf<BaseViewModel>()
    private var backPressedListener: (() -> Unit)? = null
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object : OnBackPressedCallback(isEnableBackPress()) {
            override fun handleOnBackPressed() {
                onBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up header view first
        setUpHeaderView()
        
        // Register with MainActivity
        (activity as? MainActivity)?.setChildFragment(this)
        
        // Set default header color
        setHeaderColor(R.color.colorBase)
        
        // Set up UI
        slideUpNavBar()
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        
        // CRITICAL FIX: Set up insets handling for this fragment
        setupFragmentInsets(view)
        
        // Initialize fragment
        init(view)
        handleUnauthenticatedState()
        handleErrorState()
        initAppUpdater()
    }
    
    /**
     * CRITICAL FIX: Proper fragment-level insets handling
     */
    private fun setupFragmentInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { fragmentView, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            
            // Calculate effective insets
            val effectiveInsets = FragmentInsets(
                left = maxOf(systemBars.left, displayCutout.left),
                top = maxOf(systemBars.top, displayCutout.top),
                right = maxOf(systemBars.right, displayCutout.right),
                bottom = maxOf(systemBars.bottom, displayCutout.bottom)
            )
            
            // Apply insets to fragment content (excluding header)
            applyInsetsToFragmentContent(fragmentView, effectiveInsets)
            
            // Don't consume insets
            insets
        }
    }
    
    /**
     * CRITICAL FIX: Apply insets to fragment content, not header
     */
    private fun applyInsetsToFragmentContent(view: View, insets: FragmentInsets) {
        // Find the main content area (usually the first child of fragment root)
        val contentView = findFragmentContentView(view)
        
        contentView?.apply {
            setPadding(
                insets.left,
                0, // Don't apply top padding - header handles this
                insets.right,
                insets.bottom
            )
        }
        
        // Handle any scrolling views specially
        handleScrollingViews(view, insets)
    }
    
    /**
     * Find the main content view in fragment
     */
    private fun findFragmentContentView(view: View): View? {
        // Look for common content view IDs
        return view.findViewById(R.id.fragment_content) 
            ?: view.findViewById(R.id.content_container)
            ?: view.findViewById(R.id.main_content)
            ?: if (view is android.view.ViewGroup && view.childCount > 0) view.getChildAt(0) else null
    }
    
    /**
     * Handle scrolling views like RecyclerView, ScrollView, etc.
     */
    private fun handleScrollingViews(view: View, insets: FragmentInsets) {
        // Find RecyclerViews and set up edge-to-edge scrolling
        findViewsByType<androidx.recyclerview.widget.RecyclerView>(view).forEach { recyclerView ->
            recyclerView.clipToPadding = false
            recyclerView.setPadding(
                insets.left,
                0, // Let content scroll under header
                insets.right,
                insets.bottom
            )
        }
        
        // Find ScrollViews
        findViewsByType<android.widget.ScrollView>(view).forEach { scrollView ->
            scrollView.clipToPadding = false
            scrollView.setPadding(
                insets.left,
                0, // Let content scroll under header
                insets.right,
                insets.bottom
            )
        }
    }
    
    /**
     * Helper function to find views by type
     */
    private inline fun <reified T : View> findViewsByType(parent: View): List<T> {
        val result = mutableListOf<T>()
        if (parent is T) {
            result.add(parent)
        }
        if (parent is android.view.ViewGroup) {
            for (i in 0 until parent.childCount) {
                result.addAll(findViewsByType<T>(parent.getChildAt(i)))
            }
        }
        return result
    }

    override fun initAppUpdater() {
        (activity as? MainActivity)?.initAppUpdater(requireActivity())
    }

    /**
     * FIXED: Improved header color setting with proper top padding preservation
     */
    override fun setHeaderColor(@ColorRes headerColor: Int) {
        (activity as? MainActivity)?.apply {
            headerView.apply {
                setBackgroundColor(ContextCompat.getColor(context, headerColor))
                // Preserve the top padding that was set for status bar
                // Don't reset padding here
            }
            window.statusBarColor = ContextCompat.getColor(this@apply, headerColor)
        }
    }

    fun setStatusBarColor(@ColorRes color: Int) {
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), color)
    }

    fun setupWithVM(vararg viewModel: BaseViewModel) {
        viewModels.clear()
        viewModels.addAll(viewModel.toList())
    }

    private fun handleUnauthenticatedState() {
        viewModels.forEach { viewModel ->
            viewModel.authenticatedEvent.forEach {
                it.observeEvent(viewLifecycleOwner) { authenticated ->
                    (activity as? MainActivity)?.let {
                        clearPersistentData()
                        startActivity(Intent(context, LandingActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        finishActivity()
                    }
                }
            }
        }
    }

    private fun handleErrorState() {
        viewModels.forEach { viewModel ->
            viewModel.errorEvent.forEach {
                it.observeEvent(viewLifecycleOwner) { str ->
                    val errorMessage = when (activity) {
                        is MainActivity, is LandingActivity -> str
                        else -> str.split(":").lastOrNull() ?: str
                    }
                    toast(errorMessage)
                }
            }
        }
    }

    // Photo handling methods (unchanged from previous fix)
    override fun takePhoto(aspectRatio: PhotoUtil.AspectRatio?, onPickPhotoComplete: (uri: Uri) -> Unit) {
        photoUtil.dispatchTakePictureIntent(aspectRatio, onPickPhotoComplete)
    }

    override fun takePhotoFromGallery(aspectRatio: PhotoUtil.AspectRatio?, onPickPhotoComplete: (uri: Uri) -> Unit) {
        photoUtil.dispatchPhotoGalleryIntent(aspectRatio, onPickPhotoComplete)
    }

    override fun pickPhoto(aspectRatio: PhotoUtil.AspectRatio?, onPickPhotoComplete: (uri: Uri) -> Unit) {
        context?.let {
            BottomDialog.create(
                it, getString(R.string.bs_label_add_photo),
                resources.getStringArray(R.array.array_option_add_photo)
            ) { pos ->
                when (pos) {
                    0 -> takePhotoFromGallery(aspectRatio, onPickPhotoComplete)
                    1 -> takePhoto(aspectRatio, onPickPhotoComplete)
                }
            }.show()
        }
    }

    override fun makeToast(msg: String?) {
        activity?.makeToast(msg)
    }

    override fun makeToast(res: Int?) {
        activity?.makeToast(res)
    }

    override fun setHeaderTitle(resId: Int, func: (TextView.() -> Unit)?) {
        (activity as? MainActivity)?.apply {
            headerText.apply {
                func?.invoke(this)
                text = getString(resId)
            }
        }
    }

    override fun setHeaderTitle(title: String, func: (TextView.() -> Unit)?) {
        (activity as? MainActivity)?.apply {
            headerText.apply {
                func?.invoke(this)
                text = title
            }
        }
    }

    override fun setSoftInputMode(inputMode: Int) {
        activity?.window?.setSoftInputMode(inputMode)
    }

    /**
     * FIXED: Improved header view setup
     */
    private fun setUpHeaderView() {
        (activity as? MainActivity)?.apply {
            headerView.visibility = View.VISIBLE
            btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            
            try {
                findNavController().currentDestination?.let { dest ->
                    val isTopDestination = hasNestedParent(dest) && 
                        dest.parent?.startDestinationId == dest.id
                    headerText.text = dest.label
                    backBtnVisibility(!isTopDestination)
                }
            } catch (e: IllegalStateException) {
                backBtnVisibility(true)
            }
        }
    }

    private fun backBtnVisibility(isVisible: Boolean) {
        (activity as? MainActivity)?.apply {
            btnBack.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    private fun hasNestedParent(dest: NavDestination) = dest.parent?.parent == null

    fun slideUpNavBar() {
        (activity as? MainActivity)?.slideUpNavBar()
    }

    fun slideDownNavBar() {
        (activity as? MainActivity)?.slideDownNavBar()
    }

    fun hideHeaderView() {
        (activity as? MainActivity)?.headerView?.visibility = View.GONE
    }

    fun navigateTo(menuRes: Int) {
        (activity as? MainActivity)?.navigateTo(menuRes)
    }

    // Back press handling (unchanged)
    override fun isEnableBackPress(): Boolean = backPressedListener != null

    override fun addBackPressListener(backPressedListener: () -> Unit) {
        if (this.backPressedListener == null) {
            this.backPressedListener = backPressedListener
            onBackPressedCallback.isEnabled = isEnableBackPress()
        }
    }

    override fun removeBackPressListener() {
        backPressedListener = null
        onBackPressedCallback.isEnabled = isEnableBackPress()
    }

    override fun onBack() {
        backPressedListener?.invoke()
    }

    override fun onBackPressed() {
        hideKeyboard(requireActivity())
        (activity as? MainActivity)?.onBackPressedDispatcher?.onBackPressed()
    }

    data class FragmentInsets(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}
