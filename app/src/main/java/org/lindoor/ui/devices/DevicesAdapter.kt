package org.lindoor.ui.devices

import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.item_device.view.*
import org.lindoor.R
import org.lindoor.customisation.DeviceTypes
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ItemDeviceBinding
import org.lindoor.entities.Device
import org.lindoor.managers.DeviceManager
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.databindings.pxFromDp
import org.linphone.compatibility.Compatibility


class SwipeToDeleteCallback(var adapter: DevicesAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    var background: Drawable? = null
    var deleteViewMargin = 0
    var initiated = false
    lateinit var deleteIcon:PictureDrawable

    private fun init() {
        background = Theme.roundRectInputBackgroundWithColorKeyAndRadius("color_e")
        deleteIcon = Theme.svgAsPictureDrawable("icons/delete")
        Compatibility.setColorFilter(deleteIcon,Theme.getColor("color_c"))
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
            myDx = - itemView.width.toFloat() +20

        background!!.setBounds(
            itemView.right + dX.toInt()+deleteViewMargin,
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background!!.draw(c!!)

        val itemHeight = itemView.bottom - itemView.top

        val redWidth = itemView.right - (itemView.right + myDx)
        val xMarkLeft = itemView.right + myDx  +redWidth/2- deleteIcon!!.intrinsicWidth/2 + deleteViewMargin/2
        val xMarkRight = itemView.right + myDx  + redWidth/2+deleteIcon!!.intrinsicWidth/2 + deleteViewMargin/2
        val xMarkTop = itemView.top + (itemHeight - deleteIcon!!.intrinsicHeight) / 2
        val xMarkBottom = xMarkTop + deleteIcon!!.intrinsicHeight
        deleteIcon!!.setBounds(xMarkLeft.toInt(), xMarkTop, xMarkRight.toInt(), xMarkBottom)
        deleteIcon!!.draw(c)
        super.onChildDraw(
            c,
            recyclerView!!, viewHolder, dX, dY, actionState, isCurrentlyActive
        )
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
       val device = adapter.devices.value!!.get(viewHolder.adapterPosition)
        DialogUtil.confirm(
            "delete_device_confirm_message",
            { dialog: DialogInterface, which: Int ->
               DeviceManager.removeDevice(device)
                adapter.devices.value = DeviceManager.devices
                adapter.notifyDataSetChanged()
            },device.name,
            { dialog: DialogInterface, which: Int ->
                adapter.notifyDataSetChanged()
            }
        )
    }
}

class DevicesAdapter(val devices: MutableLiveData<ArrayList<Device>>, recyclerView: RecyclerView) :
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
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return devices.value!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val background = itemView.device_row
        private val name = itemView.name
        private val typeIcon = itemView.type_icon
        private val address = itemView.address
        private val call = itemView.call
        private val deviceImage = itemView.device_image
        private val view = itemView

        fun bindItems(device: Device) {
            name.text = device.name
            address.text = device.address
            if (device.type != null) {
                Theme.setIcon(DeviceTypes.iconNameForDeviceType(device.type!!), typeIcon)
                typeIcon.visibility = View.VISIBLE
                if (device.supportsVideo()) {
                    Theme.setIcon("icons/eye", call)
                } else if (device.supportsAudio())
                    Theme.setIcon("icons/phone", call)
            } else {
                typeIcon.visibility = View.GONE
                Theme.setIcon("icons/eye", call)
            }
            call.setOnClickListener {
                device.call()
            }

            device.firstImageFileName?.also {
                deviceImage.visibility = View.VISIBLE
                Theme.glidegeneric.load(it).transform(RoundedCorners(50)).into(deviceImage)
                view.layoutParams.height = pxFromDp(180) // Todo themize
            }

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(devices.value!![position])
    }
}