package dev.daryl.mlkitbarcodescanner.utils

import android.content.Context
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.camera.core.AspectRatio
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun <T> mutableEventFlow(): MutableSharedFlow<T> {
    return MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1
    )
}

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
