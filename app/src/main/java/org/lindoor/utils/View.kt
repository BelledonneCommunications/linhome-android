package org.lindoor.utils

import android.view.View

fun View.toogleVisible() {
    if (visibility == View.VISIBLE)
        visibility = View.INVISIBLE
    else
        visibility = View.VISIBLE
}

fun View.toogleGone() {
    if (visibility == View.VISIBLE)
        visibility = View.GONE
    else
        visibility = View.VISIBLE
}