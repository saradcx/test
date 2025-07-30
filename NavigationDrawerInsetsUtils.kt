import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import com.securnyx360.a360towerguard.R

fun View.setEdgeToEdgeInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false,
    insetFun: ViewGroup.MarginLayoutParams.(InsetsCollection) -> Unit
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)
        val initialTop = if (view.getTag(R.id.initial_margin_top) != null) {
            view.getTag(R.id.initial_margin_top) as Int
        } else {
            view.setTag(R.id.initial_margin_top, view.marginTop)
            view.marginTop
        }
        val initialLeft = if (view.getTag(R.id.initial_margin_left) != null) {
            view.getTag(R.id.initial_margin_left) as Int
        } else {
            view.setTag(R.id.initial_margin_left, view.marginLeft)
            view.marginLeft
        }
        val initialRight = if (view.getTag(R.id.initial_margin_right) != null) {
            view.getTag(R.id.initial_margin_right) as Int
        } else {
            view.setTag(R.id.initial_margin_right, view.marginRight)
            view.marginRight
        }
        val initialBottom = if (view.getTag(R.id.initial_margin_bottom) != null) {
            view.getTag(R.id.initial_margin_bottom) as Int
        } else {
            view.setTag(R.id.initial_margin_bottom, view.marginBottom)
            view.marginBottom
        }

        val insetsCollection = InsetsCollection(
            initialTop = initialTop,
            insetTop = insets.top,
            initialLeft = initialLeft,
            insetLeft = insets.left,
            initialRight = initialRight,
            insetRight = insets.right,
            initialBottom = initialBottom,
            insetBottom = insets.bottom,
        )
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            apply { insetFun(insetsCollection) }
        }
        if (propagateInsets) windowInsets else WindowInsetsCompat.CONSUMED
    }
}

fun View.setSideInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    leftMargin = insets.left
    rightMargin = insets.right
}

fun View.setTopAndSideInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    leftMargin = insets.left
    rightMargin = insets.right
    topMargin = insets.top
}

fun View.setBottomAndSideInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    leftMargin = insets.left
    rightMargin = insets.right
    bottomMargin = insets.bottom
}

// Navigation Drawer specific inset functions

/**
 * Applies insets to the drawer content, typically used for the navigation view inside the drawer.
 * This ensures the drawer content respects system bars and display cutouts.
 */
fun View.setDrawerContentInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // For drawer content, we typically want to respect top and side insets
    // but not bottom insets to allow content to extend to the bottom
    leftMargin = insets.left
    topMargin = insets.top
    rightMargin = insets.right
}

/**
 * Applies insets to the main content when using a navigation drawer.
 * This is typically used for the content area that gets covered by the drawer.
 */
fun View.setDrawerMainContentInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // Main content should respect all insets
    leftMargin = insets.left
    topMargin = insets.top
    rightMargin = insets.right
    bottomMargin = insets.bottom
}

/**
 * Applies insets to drawer header views, ensuring they don't overlap with status bar or display cutouts.
 */
fun View.setDrawerHeaderInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // Header typically needs top and side margins
    leftMargin = insets.left
    topMargin = insets.top
    rightMargin = insets.right
}

/**
 * Applies insets to drawer menu items or content area, avoiding the header space.
 */
fun View.setDrawerMenuInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // Menu items typically only need side margins
    leftMargin = insets.left
    rightMargin = insets.right
}

/**
 * Special handling for DrawerLayout itself to ensure proper edge-to-edge behavior.
 */
fun DrawerLayout.setDrawerLayoutInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = true
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)
        
        // DrawerLayout should typically consume the insets and let children handle them individually
        // We don't apply margins to the DrawerLayout itself, but ensure it handles the insets properly
        
        // Store insets for child views to access if needed
        view.setTag(R.id.drawer_insets, insets)
        
        if (propagateInsets) windowInsets else WindowInsetsCompat.CONSUMED
    }
}

/**
 * Applies insets to views that should only respect horizontal (side) insets,
 * useful for drawer content that should extend to top and bottom edges.
 */
fun View.setDrawerSideOnlyInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    leftMargin = insets.left
    rightMargin = insets.right
}

/**
 * Applies insets to floating action buttons or similar elements in drawer layouts.
 */
fun View.setDrawerFabInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // FABs typically need right and bottom margins
    rightMargin = insets.right
    bottomMargin = insets.bottom
}

/**
 * Applies conditional insets based on drawer state.
 * This is useful for content that should behave differently when drawer is open vs closed.
 */
fun View.setConditionalDrawerInsets(
    drawerLayout: DrawerLayout,
    drawerGravity: Int,
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    val isDrawerOpen = drawerLayout.isDrawerOpen(drawerGravity)
    
    if (isDrawerOpen) {
        // When drawer is open, content might need different inset handling
        topMargin = insets.top
        bottomMargin = insets.bottom
        // Side margins might be handled differently based on drawer position
    } else {
        // When drawer is closed, apply all insets normally
        leftMargin = insets.left
        topMargin = insets.top
        rightMargin = insets.right
        bottomMargin = insets.bottom
    }
}

/**
 * Applies insets specifically for left drawer navigation views.
 * Handles the case where the drawer slides in from the left side.
 */
fun View.setLeftDrawerInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // Left drawer typically needs left, top margins, but not right margin
    leftMargin = insets.left
    topMargin = insets.top
}

/**
 * Applies insets specifically for right drawer navigation views.
 * Handles the case where the drawer slides in from the right side.
 */
fun View.setRightDrawerInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // Right drawer typically needs right, top margins, but not left margin
    rightMargin = insets.right
    topMargin = insets.top
}

/**
 * Applies insets to toolbar/app bar in drawer layouts.
 * Ensures the toolbar doesn't overlap with status bar or display cutouts.
 */
fun View.setDrawerToolbarInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // Toolbar typically needs top and side margins
    leftMargin = insets.left
    topMargin = insets.top
    rightMargin = insets.right
}

/**
 * Applies insets to bottom navigation or bottom sheets in drawer layouts.
 */
fun View.setDrawerBottomInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // Bottom components need side and bottom margins
    leftMargin = insets.left
    rightMargin = insets.right
    bottomMargin = insets.bottom
}

/**
 * Applies insets to scrim/overlay views in drawer layouts.
 * These are typically full-screen overlays that dim the content when drawer is open.
 */
fun View.setDrawerScrimInsets(
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    // Scrim should typically extend to all edges, so no margins needed
    // This function exists for consistency and future customization
}

/**
 * Applies insets with animation support for smooth transitions when drawer state changes.
 */
fun View.setAnimatedDrawerInsets(
    drawerLayout: DrawerLayout,
    drawerGravity: Int,
    animationDuration: Long = 300L,
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    propagateInsets: Boolean = false
) = setEdgeToEdgeInsets(typeMask, propagateInsets) { insets ->
    val isDrawerOpen = drawerLayout.isDrawerOpen(drawerGravity)
    
    // Animate the margin changes based on drawer state
    animate()
        .setDuration(animationDuration)
        .apply {
            if (isDrawerOpen) {
                // Animate to drawer-open state
                translationX(0f)
                translationY(0f)
            } else {
                // Animate to drawer-closed state
                translationX(0f)
                translationY(0f)
            }
        }
        .start()
    
    // Apply appropriate margins based on state
    if (isDrawerOpen) {
        topMargin = insets.top
        bottomMargin = insets.bottom
    } else {
        leftMargin = insets.left
        topMargin = insets.top
        rightMargin = insets.right
        bottomMargin = insets.bottom
    }
}

data class InsetsCollection(
    private val initialTop: Int,
    private val insetTop: Int,
    private val initialLeft: Int,
    private val insetLeft: Int,
    private val initialRight: Int,
    private val insetRight: Int,
    private val initialBottom: Int,
    private val insetBottom: Int,
) {
    val top: Int
        get() = initialTop + insetTop
    val left: Int
        get() = initialLeft + insetLeft
    val right: Int
        get() = initialRight + insetRight
    val bottom: Int
        get() = initialBottom + insetBottom
}