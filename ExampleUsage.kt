import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // STEP 1: Enable edge-to-edge BEFORE setting content view
        enableEdgeToEdge()
        
        setContentView(R.layout.activity_main)
        
        // STEP 2: Set up root insets handling
        applyWindowInsetsToRoot()
        
        // STEP 3: Apply insets to specific views that need them
        setupViewInsets()
    }
    
    private fun setupViewInsets() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        
        // Toolbar needs top insets for status bar, but not bottom
        toolbar?.applySystemBarInsets(
            applyLeft = true,
            applyTop = true,
            applyRight = true,
            applyBottom = false
        )
        
        // Bottom navigation needs bottom insets for navigation bar, but not top
        bottomNav?.applySystemBarInsets(
            applyLeft = true,
            applyTop = false,
            applyRight = true,
            applyBottom = true
        )
        
        // RecyclerView might need left/right insets for display cutouts
        // but should scroll under system bars
        recyclerView?.applySystemBarInsets(
            applyLeft = true,
            applyTop = false,
            applyRight = true,
            applyBottom = false
        )
        
        // For content that should scroll under system bars,
        // you might want to add padding to the first/last items instead:
        recyclerView?.clipToPadding = false
    }
}

// Alternative approach for RecyclerView with proper edge-to-edge scrolling
fun RecyclerView.setupEdgeToEdgeScrolling() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        
        // Apply left/right insets as padding
        view.setPadding(systemBars.left, 0, systemBars.right, 0)
        
        // Apply top/bottom insets as margin to first/last items via ItemDecoration
        if (itemDecorationCount == 0) {
            addItemDecoration(EdgeToEdgeItemDecoration(systemBars.top, systemBars.bottom))
        }
        
        insets
    }
}

class EdgeToEdgeItemDecoration(
    private val topInset: Int,
    private val bottomInset: Int
) : RecyclerView.ItemDecoration() {
    
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        
        if (position == 0) {
            outRect.top = topInset
        }
        
        if (position == itemCount - 1) {
            outRect.bottom = bottomInset
        }
    }
}
