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

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sidemenu.view.*
import kotlinx.android.synthetic.main.item_sidemenu.view.*
import org.linhome.MainActivity
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.FragmentSidemenuBinding
import org.linhome.entities.Account
import org.linhome.utils.DialogUtil

class SideMenuFragment : Fragment() {

    private lateinit var sideMenuViewModel: SideMenuViewModel

    private fun getMainActivity(): MainActivity {
        return activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sideMenuViewModel =
            ViewModelProvider(this).get(SideMenuViewModel::class.java)

        val binding = FragmentSidemenuBinding.inflate(inflater, container, false)


        binding.root.list.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.root.list.adapter =
            SideMenuAdapter(sideMenuViewModel.sideMenuOptions, getMainActivity().navController)

        binding.root.label.text = Texts.get(sideMenuViewModel.sideMenuDisconnectOption.textKey)
        Theme.setIcon(sideMenuViewModel.sideMenuDisconnectOption.iconFile, binding.root.icon)
        binding.root.top_separator.setBackgroundColor(Theme.getColor("color_h"))
        binding.root.top_separator.visibility = View.VISIBLE
        binding.root.bottom_separator.visibility = View.GONE
        binding.root.disconnect.background =
            Theme.selectionEffectAsStateListDrawable("sidemenu_option")
        binding.root.setBackgroundColor(Theme.getColor("color_b"))

        binding.root.disconnect.setOnClickListener {
            DialogUtil.confirm(
                "menu_disconnect",
                "disconnect_confirm_message",
                { _: DialogInterface, _: Int ->
                    Account.disconnect()
                    getMainActivity().navControllerSideMenu.navigateUp()
                    getMainActivity().navController.navigate(R.id.navigation_assistant_root)
                })
        }

        return binding.root
    }


}
