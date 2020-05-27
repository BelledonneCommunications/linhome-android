package org.lindoor.ui.sidemenu

import androidx.lifecycle.ViewModel
import org.lindoor.R

class SideMenuViewModel : ViewModel() {
    var sideMenuOptions: ArrayList<MenuOption> = ArrayList()
    var sideMenuDisconnectOption: MenuOption
    
    init {
        sideMenuOptions.add(MenuOption("menu_assistant","icons/assistant",R.id.navigation_assistant_root))
        sideMenuOptions.add(MenuOption("menu_account","icons/account",R.id.navigation_account))
        sideMenuOptions.add(MenuOption("menu_settings","icons/settings",R.id.navigation_settings))
        sideMenuOptions.add(MenuOption("menu_about","icons/about",R.id.navigation_about))
        sideMenuDisconnectOption = MenuOption("menu_disconnect","icons/disconnect",R.id.navigation_disconnect)
    }

}