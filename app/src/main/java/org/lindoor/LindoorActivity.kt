package org.lindoor

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.lindoor.customisation.Theme
import org.lindoor.utils.DialogUtil

abstract class LindoorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        val decorView: View = window.decorView
        val uiOptions: Int =  View.SYSTEM_UI_FLAG_VISIBLE
        decorView.setSystemUiVisibility(uiOptions)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        applyCommonTheme()
    }

    override fun onResume() {
        super.onResume()
        DialogUtil.context = this
    }

    override fun onPause() {
        DialogUtil.context = null
        super.onPause()
    }

    fun applyCommonTheme() {
        getWindow().also { window ->
            window.setStatusBarColor(Theme.getColor("color_a"))
            window.setNavigationBarColor(Theme.getColor("color_j"))
        }
    }



}