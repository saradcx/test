import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// BUG 1 FIX: Use ViewCompat and WindowInsetsCompat for better compatibility
fun AppCompatActivity.applyWindowInsetsToRoot() {
    val rootView = findViewById<View>(android.R.id.content) as? ViewGroup
    rootView ?: return

    // BUG 2 FIX: Use ViewCompat.setOnApplyWindowInsetsListener for better compatibility
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
        // BUG 3 FIX: Use proper insets types and don't apply padding to content root
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
        
        // Combine system bars and display cutout insets
        val left = maxOf(systemBars.left, displayCutout.left)
        val top = maxOf(systemBars.top, displayCutout.top)
        val right = maxOf(systemBars.right, displayCutout.right)
        val bottom = maxOf(systemBars.bottom, displayCutout.bottom)
        
        // Instead of applying padding to root, store insets for child views to use
        view.tag = CustomInsets(left, top, right, bottom)
        
        // Return the insets unchanged so child views can handle them appropriately
        insets
    }
    
    // Request initial insets application
    ViewCompat.requestApplyInsets(rootView)
}

// IMPROVED: Better extension function for getting system bar insets
fun WindowInsetsCompat.getSystemBarInsets(): CustomInsets {
    val systemBars = getInsets(WindowInsetsCompat.Type.systemBars())
    val displayCutout = getInsets(WindowInsetsCompat.Type.displayCutout())
    
    return CustomInsets(
        left = maxOf(systemBars.left, displayCutout.left),
        top = maxOf(systemBars.top, displayCutout.top),
        right = maxOf(systemBars.right, displayCutout.right),
        bottom = maxOf(systemBars.bottom, displayCutout.bottom)
    )
}

// IMPROVED: Extension to apply insets to specific views that need padding
fun View.applySystemBarInsets(
    applyLeft: Boolean = true,
    applyTop: Boolean = true,
    applyRight: Boolean = true,
    applyBottom: Boolean = true
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemInsets = insets.getSystemBarInsets()
        
        view.setPadding(
            if (applyLeft) systemInsets.left else 0,
            if (applyTop) systemInsets.top else 0,
            if (applyRight) systemInsets.right else 0,
            if (applyBottom) systemInsets.bottom else 0
        )
        
        insets
    }
}

data class CustomInsets(val left: Int, val top: Int, val right: Int, val bottom: Int)

// ADDITIONAL: Helper function for edge-to-edge setup
fun AppCompatActivity.enableEdgeToEdge() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(false)
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }
}
