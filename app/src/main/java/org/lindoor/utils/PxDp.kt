package org.lindoor.utils

import org.lindoor.LindoorApplication

fun pxFromDp(dp: Int): Int {
    return (dp * LindoorApplication.instance.resources.displayMetrics.density).toInt()
}

fun pxFromDp(dp: Float): Float {
    return dp * LindoorApplication.instance.resources.displayMetrics.density
}