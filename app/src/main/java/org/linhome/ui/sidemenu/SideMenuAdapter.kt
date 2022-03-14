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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.ItemSidemenuBinding

class MenuOption(val textKey: String, val iconFile: String, val targetFragmentId: Int)
class SideMenuAdapter(private val options: ArrayList<MenuOption>, navController: NavController) :
    RecyclerView.Adapter<SideMenuAdapter.ViewHolder>() {

    val navigationController = navController

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_sidemenu,
            parent,
            false
        ) as ItemSidemenuBinding
        return ViewHolder(binding, this)
    }

    override fun getItemCount(): Int {
        return options.size
    }

    class ViewHolder(itemView: ItemSidemenuBinding, adapter: SideMenuAdapter) : RecyclerView.ViewHolder(itemView.root) {
        private val optionIconIV = itemView.icon
        private val optionLabelTV: TextView = itemView.label
        private val optionsAdapter = adapter

        init {
            itemView.bottomSeparator.setBackgroundColor(Theme.getColor("color_h"))
            itemView.root.background = Theme.selectionEffectAsStateListDrawable("sidemenu_option")
        }

        fun bindItems(option: MenuOption) {
            optionLabelTV.text = Texts.get(option.textKey)
            Theme.setIcon(option.iconFile, optionIconIV)
            itemView.setOnClickListener {
                optionsAdapter.navigationController.navigate(option.targetFragmentId)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(options[position])
    }
}