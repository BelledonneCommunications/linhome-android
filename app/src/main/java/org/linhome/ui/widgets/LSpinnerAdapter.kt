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

package org.linhome.ui.widgets

import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.ItemSpinnerBinding

class SpinnerItem(val textKey: String, val iconFile: String? = null, val backingKey: String? = null)
class LSpinnerAdapter(private val options: ArrayList<SpinnerItem>, val spinner: Spinner, val lSpinner:LSpinner, var selectedIndex: Int?) :
    SpinnerAdapter {

    var height: Int? = null

    class ViewHolder(itemView: ItemSpinnerBinding) {
        val optionIconIV = itemView.icon
        val optionLabelTV: TextView = itemView.label
        val optionArrow: ImageView = itemView.arrow
        val optionSeparator: View = itemView.bottomSeparator

        init {
            itemView.bottomSeparator.setBackgroundColor(Theme.getColor("color_h"))
            itemView.root.background = Theme.selectionEffectAsStateListDrawable("dropdown_list")
        }
    }

    override fun isEmpty(): Boolean {
        return options.size == 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position, convertView, parent, true)
    }


    fun getView(position: Int, convertView: View?, parent: ViewGroup?, isDropDown: Boolean): View {
        var viewHolder: ViewHolder
        var view = convertView
        if (view == null) {
            val binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent?.context),
                R.layout.item_spinner,
                parent,
                false
            ) as ItemSpinnerBinding
            view = binding.root
            height = view.getLayoutParams().height
            viewHolder = ViewHolder(binding)
            view.tag = viewHolder
        } else
            viewHolder = view.tag as ViewHolder
        val option = getItem(position) as SpinnerItem
        viewHolder.optionLabelTV.text = Texts.get(option.textKey)
        viewHolder.optionArrow.visibility = if (isDropDown) View.VISIBLE else View.GONE
        viewHolder.optionSeparator.visibility = if (isDropDown) View.GONE else View.VISIBLE
        if (option.iconFile == null) {
            viewHolder.optionIconIV.visibility = View.GONE
        } else {
            Theme.setIcon(option.iconFile, viewHolder.optionIconIV)
        }
        view.setOnClickListener {
            spinner.performClick()
            if (lSpinner.opened) {
                lSpinner.onItemSelected(position)
            } else {
                lSpinner.opened = true
            }
            spinner.setSelection(position)
        }
        view.isPressed = true
        if (isDropDown)
            view.background = Theme.roundRectInputBackgroundWithColorKeyAndRadius(
                "color_i",
                "user_input_corner_radius"
            )
        if (!isDropDown && position == 0) {
            view.getLayoutParams().height = 1
            view.visibility = View.GONE
        } else {
            view.layoutParams.height = this.height!!
            view.visibility = View.VISIBLE
        }

        if (lSpinner.opened && position == selectedIndex) {
            view.background = Theme.roundRectInputBackgroundWithColorKeyAndRadius(
                "color_i",
                "user_input_corner_radius"
            )
        }

        return view
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItem(position: Int): Any {
        return options[position]
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position, convertView, parent, false)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
    }

    override fun getCount(): Int {
        return options.size
    }
}

fun indexByBackingKey(key: String?, items: ArrayList<SpinnerItem>): Int {
    items.forEachIndexed { index, spinnerItem ->
        if (spinnerItem.backingKey == key)
            return index
    }
    return 0
}