package org.lindoor.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View


fun View.toogleVisible() {
    animate()
        .alpha(if (visibility == View.VISIBLE) 0f else 1f)
        .setDuration(250)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (visibility == View.VISIBLE)
                    visibility = View.INVISIBLE
                else
                    visibility = View.VISIBLE
            }
        })
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.toogleGone() {
    if (visibility == View.VISIBLE)
        visibility = View.GONE
    else
        visibility = View.VISIBLE
}