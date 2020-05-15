package org.lindoor.ui.sidemenu

import androidx.lifecycle.ViewModel
import org.lindoor.R

class SideMenuViewModel : ViewModel() {
    var sideMenuOptions: ArrayList<OptionMenu> = ArrayList()
    var sideMenuDisconnectOption: OptionMenu
    
    init {
        sideMenuOptions.add(OptionMenu("menu_assistant","icons/assistant",R.id.navigation_assistant_root))
        // Section 07 Specs removed -  sideMenuOptions.add(OptionMenu("menu_account","icons/account",R.id.navigation_account))
        sideMenuOptions.add(OptionMenu("menu_settings","icons/settings",R.id.navigation_settings))
        sideMenuOptions.add(OptionMenu("menu_about","icons/about",R.id.navigation_about))
        sideMenuDisconnectOption = OptionMenu("menu_disconnect","icons/disconnect",R.id.navigation_disconnect)
    }

}