package org.lindoor.ui.devices

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
import org.lindoor.BR
import org.lindoor.GenericFragment
import org.lindoor.LindoorApplication
import org.lindoor.R
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ItemDeviceBinding
import org.lindoor.entities.Device
import org.lindoor.store.DeviceStore
import org.lindoor.utils.DialogUtil
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
    val selectedDevice: MutableLiveData<Device>,
    private val lindoorFragment: GenericFragment
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
            selectedDevice: MutableLiveData<Device>,
            lindoorFragment: GenericFragment
        ) {
            binding.lifecycleOwner = lindoorFragment
            binding.setVariable(BR.device, device)
            binding.setVariable(BR.selectedDevice, selectedDevice)
            binding.executePendingBindings()
            view.setOnClickListener {
                selectedDevice.value = device
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(devices.value!![position], selectedDevice, lindoorFragment)
        if (LindoorApplication.instance.tablet() && position == 0 && selectedDevice.value == null)
            selectedDevice.value = devices.value!![position]
    }

}