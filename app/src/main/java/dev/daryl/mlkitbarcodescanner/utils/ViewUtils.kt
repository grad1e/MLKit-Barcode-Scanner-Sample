package dev.daryl.mlkitbarcodescanner.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.core.view.*
import androidx.databinding.BindingAdapter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun DisplayMetrics.getAspectRatio(): Int {
    val ratio43Value = 4.0 / 3.0
    val ratio169Value = 16.0 / 9.0

    val previewRatio = max(this.widthPixels, this.heightPixels).toDouble() / min(
        this.widthPixels,
        this.heightPixels
    )
    if (abs(previewRatio - ratio43Value) <= abs(previewRatio - ratio169Value)) {
        return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

@BindingAdapter("topInsetMargin")
fun View.setTopInsetMargin(value: Boolean) {
    if (value) {
        val initialTopInset = this.marginTop
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updateLayoutParams {
                (this as? ViewGroup.MarginLayoutParams)?.updateMargins(top = topInset + initialTopInset)
            }
            insets
        }
    }
}

@BindingAdapter("bottomInsetMargin")
fun View.setBottomInsetMargin(value: Boolean) {
    if (value) {
        val initialBottomInset = this.marginBottom
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()).bottom
            view.updateLayoutParams {
                (this as? ViewGroup.MarginLayoutParams)?.updateMargins(bottom = bottomInset + initialBottomInset)
            }
            insets
        }
    }
}

@BindingAdapter("topInsetPadding")
fun View.setTopInsetPadding(value: Boolean) {
    if (value) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.updatePadding(top = topInset)
            insets
        }
    }
}

@BindingAdapter("bottomInsetPadding")
fun View.setBottomInsetPadding(value: Boolean) {
    if (value) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()).bottom
            v.updatePadding(bottom = bottomInset)
            insets
        }
    }
}
