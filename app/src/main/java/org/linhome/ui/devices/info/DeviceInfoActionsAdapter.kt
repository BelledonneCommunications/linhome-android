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

package org.linhome.ui.devices.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_action_info.view.*
import kotlinx.android.synthetic.main.item_sidemenu.view.icon
import kotlinx.android.synthetic.main.item_sidemenu.view.label
import org.linhome.R
import org.linhome.customisation.ActionTypes
import org.linhome.customisation.Theme
import org.linhome.databinding.ItemActionInfoBinding
import org.linhome.entities.Action

class DeviceInfoActionsAdapter(private val actions: ArrayList<Action>) :
    RecyclerView.Adapter<DeviceInfoActionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_action_info,
            parent,
            false
        ) as ItemActionInfoBinding
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon = itemView.icon
        private val type: TextView = itemView.label
        private val code: TextView = itemView.code
        private val topSeparator = itemView.top_separator

        fun bindItems(action: Action, showTopSeparator:Boolean) {
            type.text = action.typeName()
            code.text = action.code
            topSeparator.visibility = if (showTopSeparator) View.VISIBLE else View.INVISIBLE
            Theme.setIcon(ActionTypes.iconNameForActionType(action.type!!), icon)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(actions[position], position == 0)
    }
}