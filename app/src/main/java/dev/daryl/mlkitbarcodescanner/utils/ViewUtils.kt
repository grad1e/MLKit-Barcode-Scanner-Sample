package dev.daryl.mlkitbarcodescanner.utils

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.PixelCopy
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

fun overlayBitmap(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
    val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
    val canvas = Canvas(bmOverlay)
    canvas.drawBitmap(bmp1, Matrix(), null)
    canvas.drawBitmap(bmp2, Matrix(), null)
    return bmOverlay
}

fun darkenBitMap(bm: Bitmap): Bitmap {
    val canvas = Canvas(bm)
    val p = Paint(Color.RED)
    // ColorFilter filter = new LightingColorFilter(0xFFFFFFFF , 0x00222222); // lighten
    val filter: ColorFilter = LightingColorFilter(-0x808081, 0x00000000) // darken
    p.colorFilter = filter
    canvas.drawBitmap(bm, Matrix(), p)
    return bm
}

fun getScreenShotFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
    activity.window?.let { window ->
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewInWindow)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PixelCopy.request(
                    window,
                    Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + view.width,
                        locationOfViewInWindow[1] + view.height
                    ),
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        }
                        // possible to handle other result codes ...
                    },
                    Handler(Looper.getMainLooper())
                )
            } else {
                getScreenShot(view)
            }
        } catch (e: IllegalArgumentException) {
            // PixelCopy may throw IllegalArgumentException, make sure to handle it
            e.printStackTrace()
        }
    }
}

// deprecated version
/*  Method which will return Bitmap after taking screenshot. We have to pass the view which we want to take screenshot.  */
private fun getScreenShot(view: View): Bitmap {
    val screenView = view.rootView
    screenView.isDrawingCacheEnabled = true
    val bitmap = Bitmap.createBitmap(screenView.drawingCache)
    screenView.isDrawingCacheEnabled = false
    return bitmap
}
