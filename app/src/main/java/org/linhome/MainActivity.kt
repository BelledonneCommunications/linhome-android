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
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.ActivityMainBinding
import org.linhome.entities.LinhomeAccount
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
    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        applyCommonTheme()


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        toolbarViewModel = ViewModelProvider(this).get(ToolbarViewModel::class.java)
        binding.toolbarviewmodel = toolbarViewModel

        tabbarViewModel = ViewModelProvider(this).get(TabbarViewModel::class.java)
        binding.tabbarviewmodel = tabbarViewModel

        setSupportActionBar(binding.appbar.toolbar)

        navController = findNavController(R.id.navigation_host_fragment)
        navControllerSideMenu = findNavController(R.id.host_fragment_sidemenu)

        applyTheme()

        binding.appbar.contentmain.tabbarDevices.setOnClickListener {
            if (tabBarLayoutClicked(binding.appbar.contentmain.tabbarDevices, binding.appbar.contentmain.tabbarHistory)) {
                navController.popBackStack()
                navController.navigate(R.id.navigation_devices, null)
            }
        }
        binding.appbar.contentmain.tabbarHistory.setOnClickListener {
            if (tabBarLayoutClicked(binding.appbar.contentmain.tabbarHistory, binding.appbar.contentmain.tabbarDevices)) {
                navController.popBackStack()
                navController.navigate(R.id.navigation_history, null)
            }
        }
        tabBarLayoutClicked(binding.appbar.contentmain.tabbarDevices, binding.appbar.contentmain.tabbarHistory)

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
            binding.appbar.toolbarTitle.text = titleForNavigationFragment(destination.id)
        }

        navControllerSideMenu.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_sidemenu -> {
                    enterNonRootFragment()
                    binding.appbar.toolbarTitle.text = null
                }
                else -> {
                    enterRootFragment()
                    binding.appbar.toolbarTitle.text =
                        titleForNavigationFragment(navController.currentDestination?.id)
                }
            }
        }

        binding.appbar.toolbarRightButton.setOnClickListener {
            toobarButtonClickedListener?.onToolbarRightButtonClicked()
        }
        binding.appbar.toolbarLeftButton.setOnClickListener {
            toobarButtonClickedListener?.onToolbarLeftButtonClicked()
        }
        binding.appbar.toolbarBurgerButton.setOnClickListener {
            onToolbarBurgerButtonClicked()
        }
        binding.appbar.toolbarBackButton.setOnClickListener {
            onToolbarBackButtonClicked()
        }

        if (!LinhomeAccount.configured() && (savedInstanceState == null || !savedInstanceState.getBoolean("ignored_no_account"))) {
            navController.navigate(R.id.navigation_assistant_root)
        }

        if (savedInstanceState != null && savedInstanceState?.getBoolean("history_opened") == true) {
            binding.appbar.contentmain.tabbarHistory.performClick()
        }


        if (StorageManager.storePrivately)
            startWihInternalStorageWithPermissionCheck()
        else
            startWihExternalStorageWithPermissionCheck()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("ignored_no_account",navController.currentDestination?.id != R.id.navigation_assistant_root)
        outState.putBoolean("history_opened",navController.currentDestination?.id == R.id.navigation_history)
    }

    private fun enterNonRootFragment() {
        toolbarViewModel.backButtonVisible.value = true
        toolbarViewModel.burgerButtonVisible.value = false
        binding.appbar.contentmain.tabbar.visibility = View.GONE
    }

    private fun enterRootFragment() {
        binding.appbar.contentmain.tabbar.visibility = View.VISIBLE
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
        binding.appbar.toolbar.setBackgroundColor(Theme.getColor("color_a"))
        binding.appbar.contentmain.tabbar.setBackgroundColor(Theme.getColor("color_j"))
        binding.appbar.progress.indeterminateTintList = Theme.buildSingleColorStateList("color_a")
        binding.appbar.progress.setBackgroundColor(Theme.getColor("color_j"))
    }

    fun tabBarLayoutClicked(clicked: ViewGroup, unclicked: ViewGroup): Boolean {
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

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_PHONE_NUMBERS,Manifest.permission.POST_NOTIFICATIONS)
    fun startWihExternalStorage() { LinhomeApplication.coreContext.initPhoneStateListener()}

    @NeedsPermission(Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_PHONE_NUMBERS,Manifest.permission.POST_NOTIFICATIONS)
    fun startWihInternalStorage() { LinhomeApplication.coreContext.initPhoneStateListener()}

    // Notifications permissions

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied(Manifest.permission.POST_NOTIFICATIONS)
    fun onNotificationDenied() { DialogUtil.error("notifications_permission_denied") }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OnNeverAskAgain(Manifest.permission.POST_NOTIFICATIONS)
    fun onNotificationsDeniedNeverAsk() { DialogUtil.error("notifications_permission_denied_dont_ask_again") }

    // Telephony related permissions

    @RequiresApi(Build.VERSION_CODES.O)
    @OnPermissionDenied(Manifest.permission.READ_PHONE_NUMBERS)
    fun onNumbersDenied() { DialogUtil.error("phone_state_permission_denied") }

    @RequiresApi(Build.VERSION_CODES.O)
    @OnNeverAskAgain(Manifest.permission.READ_PHONE_NUMBERS)
    fun onNumbersDeniedNeverAsk() { DialogUtil.error("phone_state_permission_denied_dont_ask_again") }

    @OnPermissionDenied(Manifest.permission.READ_PHONE_STATE)
    fun onPhoneDenied() { DialogUtil.error("phone_state_permission_denied") }

    @OnNeverAskAgain(Manifest.permission.READ_PHONE_STATE)
    fun onPhoneDeniedNeverAsk() { DialogUtil.error("phone_state_permission_denied_dont_ask_again") }

    // Storage related permissions

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onStorageDenied() { DialogUtil.error("write_external_storage_permission_denied") }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onStorageDeniedNeverAsk() { DialogUtil.error("write_external_storage_permission_denied_dont_ask_again") }


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

    override fun onResume() {
        super.onResume()
        if (LinhomeApplication.coreContext.closeAppUponCallFinish) {
            LinhomeApplication.coreContext.closeAppUponCallFinish = false
            finish()
        }
    }

}
