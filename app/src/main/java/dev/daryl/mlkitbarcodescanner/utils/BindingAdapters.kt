package dev.daryl.mlkitbarcodescanner.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import androidx.databinding.BindingAdapter

/**
 * These bindingAdapters are used for setting up Window Insets
 */
@BindingAdapter("insetMarginTop", "insetMarginBottom", requireAll = false)
fun View.setInsetMargin(marginTop: Boolean, marginBottom: Boolean) {
    if (marginTop) {
        val initialTopInset = this.marginTop
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updateLayoutParams {
                (this as? ViewGroup.MarginLayoutParams)?.updateMargins(top = topInset + initialTopInset)
            }
            insets
        }
    }
    if (marginBottom) {
        val initialBottomInset = this.marginBottom
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val bottomInset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()).bottom
            view.updateLayoutParams {
                (this as? ViewGroup.MarginLayoutParams)?.updateMargins(bottom = bottomInset + initialBottomInset)
            }
            insets
        }
    }
}

@BindingAdapter("insetPaddingTop", "insetPaddingBottom", requireAll = false)
fun View.setInsetPadding(paddingTop: Boolean, paddingBottom: Boolean) {
    if (paddingTop) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updatePadding(top = topInset)
            insets
        }
    }
    if (paddingBottom) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val bottomInset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()).bottom
            v.updatePadding(bottom = bottomInset)
            insets
        }
    }
}
