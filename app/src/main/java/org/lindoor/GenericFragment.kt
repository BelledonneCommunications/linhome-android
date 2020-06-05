package org.lindoor

import android.app.Activity
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import org.lindoor.ui.toolbar.ToobarButtonClickedListener
import org.lindoor.utils.cdlog

abstract class GenericFragment: Fragment(),
    ToobarButtonClickedListener {

    val mainactivity get() = activity as MainActivity

    override fun onResume() {
        super.onResume()
        mainactivity.toobarButtonClickedListener = this
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    override fun onToolbarLeftButtonClicked() {}

    override fun onToolbarRightButtonClicked() {}

    fun hideKeyboard() {
        mainactivity.currentFocus?.also {
            val imm = it.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun showProgress() {
        mainactivity.toolbarViewModel.activityInprogress.value = true
    }
    fun hideProgress() {
        mainactivity.toolbarViewModel.activityInprogress.value = false
    }


}