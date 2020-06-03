package org.lindoor.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import java.io.File


fun File.existsAndIsNotEmpty():Boolean {
    return exists() &&  length() != 0L
}