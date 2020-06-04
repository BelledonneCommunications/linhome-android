package org.lindoor.utils

import android.graphics.BitmapFactory
import org.lindoor.LindoorApplication
import java.io.File


fun pxFromDp(dp: Int): Int {
    return (dp * LindoorApplication.instance.resources.displayMetrics.density).toInt()
}

fun pxFromDp(dp: Float): Float {
    return dp * LindoorApplication.instance.resources.displayMetrics.density
}

fun getImageDimension(file: File): IntArray {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(file.absolutePath, options)
    return intArrayOf(options.outWidth, options.outHeight)
}