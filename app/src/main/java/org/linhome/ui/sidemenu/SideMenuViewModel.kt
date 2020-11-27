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

package org.linhome.ui.sidemenu

import androidx.lifecycle.ViewModel
import org.linhome.R

class SideMenuViewModel : ViewModel() {
    var sideMenuOptions: ArrayList<MenuOption> = ArrayList()
    var sideMenuDisconnectOption: MenuOption

    init {
        sideMenuOptions.add(
            MenuOption(
                "menu_assistant",
                "icons/assistant",
                R.id.navigation_assistant_root
            )
        )
        sideMenuOptions.add(MenuOption("menu_account", "icons/account", R.id.navigation_account))
        sideMenuOptions.add(MenuOption("menu_settings", "icons/settings", R.id.navigation_settings))
        sideMenuOptions.add(MenuOption("menu_about", "icons/about", R.id.navigation_about))
        sideMenuDisconnectOption =
            MenuOption("menu_disconnect", "icons/disconnect", R.id.navigation_disconnect)
    }

}