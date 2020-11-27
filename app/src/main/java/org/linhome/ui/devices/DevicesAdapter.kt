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

package org.linhome.ui.devices

import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.linhome.BR
import org.linhome.GenericFragment
import org.linhome.LinhomeApplication
import org.linhome.R
import org.linhome.customisation.Theme
import org.linhome.databinding.ItemDeviceBinding
import org.linhome.entities.Device
import org.linhome.store.DeviceStore
import org.linhome.utils.DialogUtil
import org.linphone.compatibility.Compatibility


class SwipeToDeleteCallback(private var adapter: DevicesAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    var background: Drawable? = null
    private var deleteViewMargin = 0
    private var initiated = false
    private lateinit var deleteIcon: PictureDrawable

    private fun init() {
        background = Theme.roundRectInputBackgroundWithColorKeyAndRadius(
            "color_e",
            "device_in_device_list_corner_radius"
        )
        deleteIcon = Theme.svgAsPictureDrawable("icons/delete")
        Compatibility.setColorFilter(deleteIcon, Theme.getColor("color_c"))
        deleteViewMargin = 20
        initiated = true
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        if (viewHolder.adapterPosition == -1) {
            return
        }
        if (!initiated) {
            init()
        }
        var myDx = dX
        if (-myDx > itemView.width + 20)
            myDx = -itemView.width.toFloat() + 20

        background!!.setBounds(
            itemView.right + dX.toInt() + deleteViewMargin,
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background!!.draw(c)

        val itemHeight = itemView.bottom - itemView.top

        val redWidth = itemView.right - (itemView.right + myDx)
        val xMarkLeft =
            itemView.right + myDx + redWidth / 2 - deleteIcon.intrinsicWidth / 2 + deleteViewMargin / 2
        val xMarkRight =
            itemView.right + myDx + redWidth / 2 + deleteIcon.intrinsicWidth / 2 + deleteViewMargin / 2
        val xMarkTop = itemView.top + (itemHeight - deleteIcon.intrinsicHeight) / 2
        val xMarkBottom = xMarkTop + deleteIcon.intrinsicHeight
        deleteIcon.setBounds(xMarkLeft.toInt(), xMarkTop, xMarkRight.toInt(), xMarkBottom)
        deleteIcon.draw(c)
        super.onChildDraw(
            c,
            recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
        )
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val device = adapter.devices.value!![viewHolder.adapterPosition]
        DialogUtil.confirm(
            "delete_device_confirm_message",
            { _: DialogInterface, _: Int ->
                DeviceStore.removeDevice(device)
                adapter.devices.value = DeviceStore.devices
                if (DeviceStore.devices.size == 0)
                    adapter.selectedDevice.value = null
                adapter.notifyDataSetChanged()
            }, device.name,
            { _: DialogInterface, _: Int ->
                adapter.notifyDataSetChanged()
            }
        )
    }
}

class DevicesAdapter(
    val devices: MutableLiveData<ArrayList<Device>>,
    recyclerView: RecyclerView,
    var selectedDevice: MutableLiveData<Device?>,
    private val linhomeFragment: GenericFragment
) :
    RecyclerView.Adapter<DevicesAdapter.ViewHolder>() {

    init {
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(this))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_device,
            parent,
            false
        ) as ItemDeviceBinding
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return devices.value!!.size
    }

    class ViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {

        private val view = itemView

        fun bindItems(
            device: Device,
            selectedDevice: MutableLiveData<Device?>,
            linhomeFragment: GenericFragment
        ) {
            binding.lifecycleOwner = linhomeFragment
            binding.setVariable(BR.device, device)
            binding.setVariable(BR.selectedDevice, selectedDevice)
            binding.executePendingBindings()
            view.setOnClickListener {
                if (selectedDevice.value == null || device != selectedDevice.value!!)
                    selectedDevice.value = device
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(devices.value!![position], selectedDevice, linhomeFragment)
        if (LinhomeApplication.instance.tablet() && position == 0 && selectedDevice.value == null)
            selectedDevice.value = devices.value!![position]
    }

}