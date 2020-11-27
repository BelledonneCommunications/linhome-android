/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linhome-android
 * (see https://www.linhome.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.linhome

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.content_main.*
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.ActivityMainBinding
import org.linhome.entities.Account
import org.linhome.store.StorageManager
import org.linhome.ui.tabbar.TabbarViewModel
import org.linhome.ui.toolbar.ToobarButtonClickedListener
import org.linhome.ui.toolbar.ToolbarViewModel
import org.linhome.utils.DialogUtil
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions


@RuntimePermissions
class MainActivity : GenericActivity() {

    lateinit var navController: NavController
    lateinit var navControllerSideMenu: NavController
    lateinit var toolbarViewModel: ToolbarViewModel
    lateinit var tabbarViewModel: TabbarViewModel


    var toobarButtonClickedListener: ToobarButtonClickedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        applyCommonTheme()


        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        toolbarViewModel = ViewModelProvider(this).get(ToolbarViewModel::class.java)
        binding.toolbarviewmodel = toolbarViewModel

        tabbarViewModel = ViewModelProvider(this).get(TabbarViewModel::class.java)
        binding.tabbarviewmodel = tabbarViewModel

        setSupportActionBar(toolbar)

        navController = findNavController(R.id.navigation_host_fragment)
        navControllerSideMenu = findNavController(R.id.host_fragment_sidemenu)

        applyTheme()

        tabbar_devices.setOnClickListener {
            if (tabBarLayoutClicked(tabbar_devices, tabbar_history)) {
                navController.popBackStack()
                navController.navigate(R.id.navigation_devices, null)
            }
        }
        tabbar_history.setOnClickListener {
            if (tabBarLayoutClicked(tabbar_history, tabbar_devices)) {
                navController.popBackStack()
                navController.navigate(R.id.navigation_history, null)
            }
        }
        tabBarLayoutClicked(tabbar_devices, tabbar_history)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (sideMenuOpened()) {
                navControllerSideMenu.navigateUp()
            }
            when (destination.id) {
                R.id.navigation_devices, R.id.navigation_history -> {
                    enterRootFragment()
                }
                else -> {
                    enterNonRootFragment()
                }
            }
            toolbar_title.text = titleForNavigationFragment(destination.id)
        }

        navControllerSideMenu.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_sidemenu -> {
                    enterNonRootFragment()
                    toolbar_title.text = null
                }
                else -> {
                    enterRootFragment()
                    toolbar_title.text =
                        titleForNavigationFragment(navController.currentDestination?.id)
                }
            }
        }

        toolbar_right_button.setOnClickListener {
            toobarButtonClickedListener?.onToolbarRightButtonClicked()
        }
        toolbar_left_button.setOnClickListener {
            toobarButtonClickedListener?.onToolbarLeftButtonClicked()
        }
        toolbar_burger_button.setOnClickListener {
            onToolbarBurgerButtonClicked()
        }
        toolbar_back_button.setOnClickListener {
            onToolbarBackButtonClicked()
        }

        if (!Account.configured() && (savedInstanceState == null || !savedInstanceState.getBoolean("ignored_no_account"))) {
            navController.navigate(R.id.navigation_assistant_root)
        }

        if (savedInstanceState != null && savedInstanceState?.getBoolean("history_opened")) {
            tabbar_history.performClick()
        }


        if (!StorageManager.storePrivately)
            startWithPermissionCheck()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("ignored_no_account",navController.currentDestination?.id != R.id.navigation_assistant_root)
        outState.putBoolean("history_opened",navController.currentDestination?.id == R.id.navigation_history)
    }

    private fun enterNonRootFragment() {
        toolbarViewModel.backButtonVisible.value = true
        toolbarViewModel.burgerButtonVisible.value = false
        tabbar.visibility = View.GONE
    }

    private fun enterRootFragment() {
        tabbar.visibility = View.VISIBLE
        toolbarViewModel.backButtonVisible.value = false
        toolbarViewModel.burgerButtonVisible.value = true
        toolbarViewModel.leftButtonVisible.value = false
        toolbarViewModel.rightButtonVisible.value = false
    }

    fun pauseNavigation() {
        toolbarViewModel.backButtonVisible.value = false
    }

    fun resumeNavigation() {
        toolbarViewModel.backButtonVisible.value = true
        toolbarViewModel.leftButtonVisible.value = false
    }


    private fun applyTheme() { //Todo move into bindings when validated
        toolbar.setBackgroundColor(Theme.getColor("color_a"))
        tabbar.setBackgroundColor(Theme.getColor("color_j"))
        toolbar.progress.indeterminateTintList = Theme.buildSingleColorStateList("color_a")
        toolbar.progress.setBackgroundColor(Theme.getColor("color_j"))
    }

    private fun tabBarLayoutClicked(clicked: ViewGroup, unclicked: ViewGroup): Boolean {
        if (clicked.isSelected)
            return false
        clicked.isSelected = true
        unclicked.isSelected = false
        Theme.alphAnimate(clicked, 1f)
        Theme.alphAnimate(unclicked, 0.3f)
        return true
    }


    override fun onBackPressed() {
        if (navControllerSideMenu.currentDestination?.id == R.id.fragment_sidemenu) {
            navControllerSideMenu.navigateUp()
        } else {
            super.onBackPressed()
        }
    }

    private fun onToolbarBurgerButtonClicked() {
        navControllerSideMenu.navigate(R.id.open_side_menu, null)

    }

    private fun onToolbarBackButtonClicked() {
        if (sideMenuOpened()) {
            navControllerSideMenu.navigateUp()
        } else
            navController.navigateUp()
    }


    fun sideMenuOpened(): Boolean {
        return navControllerSideMenu.currentDestination?.id != R.id.fragment_empty
    }

    private fun titleForNavigationFragment(fragmentId: Int?): String? {
        return when (fragmentId) {
            R.id.navigation_assistant_root,
            R.id.navigation_assistant_create_linhome,
            R.id.navigation_assistant_login_linhome,
            R.id.navigation_assistant_login_sip,
            R.id.navigation_assistant_remote_root,
            R.id.navigation_assistant_remote_qr,
            R.id.navigation_assistant_remote_url -> Texts.get("assistant")
            R.id.navigation_devices, R.id.navigation_device_edit, R.id.navigation_device_info -> Texts.get(
                "devices"
            )
            R.id.navigation_history -> Texts.get("history")
            R.id.navigation_about -> Texts.get("about")
            R.id.navigation_settings -> Texts.get("settings")
            R.id.navigation_account -> Texts.get("menu_account")
            else -> null
        }
    }


    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.size > 0)
            onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun start() {
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onStorageDenied() {
        DialogUtil.error("write_external_storage_permission_denied")
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onStorageDeniedNeverAsk() {
        DialogUtil.error("write_external_storage_permission_denied_dont_ask_again")
    }


    fun applyCommonTheme() {
        window.also { window ->
            window.statusBarColor = Theme.getColor("color_a")
            window.navigationBarColor = Theme.getColor("color_j")
        }
    }


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (sideMenuOpened()) {
            if (event != null) {
                val overFlowPct =
                    if (LinhomeApplication.instance.smartPhone()) 0.75
                    else if (LinhomeApplication.instance.landcape()) 0.3
                    else 0.5

                if (event.x.toInt() > overFlowPct * window.decorView.width) {
                    navControllerSideMenu.navigateUp()
                    return false
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


}
