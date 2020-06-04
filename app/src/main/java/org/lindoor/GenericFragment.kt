package org.lindoor

import android.app.Activity
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import org.lindoor.ui.toolbar.ToobarButtonClickedListener

abstract class GenericFragment: Fragment(),
    ToobarButtonClickedListener {

    val mainactivity get() = activity as MainActivity

    override fun onResume() {
        super.onResume()
        mainactivity.toobarButtonClickedListener = this
        view?.setOnTouchListener (object : View.OnTouchListener { // Handles close side menu when clicking outside of it
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (mainactivity.sideMenuOpened()) {
                    val r = Rect(0, 0, 0, 0)
                    v?.getHitRect(r)
                    if (r.contains(event!!.x.toInt(), event.y.toInt())) {
                        mainactivity.navControllerSideMenu.navigateUp()
                        return false
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
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