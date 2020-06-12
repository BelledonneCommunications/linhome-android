package org.lindoor

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.content_main.*
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ActivityMainBinding
import org.lindoor.entities.Account
import org.lindoor.store.StorageManager
import org.lindoor.ui.tabbar.TabbarViewModel
import org.lindoor.ui.toolbar.ToobarButtonClickedListener
import org.lindoor.ui.toolbar.ToolbarViewModel
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.cdlog
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
        val uiOptions: Int =  View.SYSTEM_UI_FLAG_VISIBLE
        decorView.setSystemUiVisibility(uiOptions)
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        applyCommonTheme()


        val binding:ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
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
            if (tabBarLayoutClicked(tabbar_devices,tabbar_history))
                navController.navigate(R.id.navigation_devices, null)
        }
        tabbar_history.setOnClickListener {
            if (tabBarLayoutClicked(tabbar_history,tabbar_devices))
                navController.navigate(R.id.navigation_history, null)
        }
        tabBarLayoutClicked(tabbar_devices,tabbar_history)

        navController.addOnDestinationChangedListener {
                _, destination, _ ->
            if (sideMenuOpened()) {
                navControllerSideMenu.navigateUp()
            }
            when (destination.id) {
                R.id.navigation_devices,R.id.navigation_history -> {
                    enterRootFragment()
                } else -> {
                    enterNonRootFragment()
                }
            }
            toolbar_title.text = titleForNavigationFragment(destination.id)
        }

        navControllerSideMenu.addOnDestinationChangedListener {
                _, destination, _ ->
            when (destination.id) {
                R.id.fragment_sidemenu -> {
                    enterNonRootFragment()
                    toolbar_title.text = null
                } else -> {
                    enterRootFragment()
                    toolbar_title.text = titleForNavigationFragment(navController.currentDestination?.id)
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

        if (!Account.configured()) {
            navController.navigate(R.id.navigation_assistant_root)
        }

        if (!StorageManager.storePrivately)
            startWithPermissionCheck()

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

    private fun tabBarLayoutClicked(clicked: ViewGroup, unclicked: ViewGroup):Boolean {
        if (clicked.isSelected)
            return false
        clicked.isSelected = true
        unclicked.isSelected = false
        Theme.alphAnimate(clicked,1f)
        Theme.alphAnimate(unclicked,0.3f)
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

    private fun titleForNavigationFragment(fragmentId:Int?): String? {
        return when (fragmentId) {
            R.id.navigation_assistant_root,
            R.id.navigation_assistant_create_lindoor,
            R.id.navigation_assistant_login_lindoor,
            R.id.navigation_assistant_login_sip,
            R.id.navigation_assistant_remote_root,
            R.id.navigation_assistant_remote_qr,
            R.id.navigation_assistant_remote_url -> Texts.get("assistant")
            R.id.navigation_devices, R.id.navigation_device_edit, R.id.navigation_device_info -> Texts.get("devices")
            R.id.navigation_history -> Texts.get("history")
            R.id.navigation_about -> Texts.get("about")
            R.id.navigation_settings -> Texts.get("settings")
            else -> null
        }
    }



    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.size > 0)
            onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun start() {}

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onStorageDenied() {
        DialogUtil.error("write_external_storage_permission_denied")
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onStorageDeniedNeverAsk() {
        DialogUtil.error("write_external_storage_permission_denied_dont_ask_again")
    }


    fun applyCommonTheme() {
        getWindow().also { window ->
            window.setStatusBarColor(Theme.getColor("color_a"))
            window.setNavigationBarColor(Theme.getColor("color_j"))
        }
    }


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (sideMenuOpened()) {
            if (event != null) {
                if (event.x.toInt() > 0.75 * getWindow().getDecorView().getWidth()) {
                    navControllerSideMenu.navigateUp()
                    return false
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


}
