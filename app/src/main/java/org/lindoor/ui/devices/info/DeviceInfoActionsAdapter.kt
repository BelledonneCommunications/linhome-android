package org.lindoor.ui.devices.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_action_info.view.*
import kotlinx.android.synthetic.main.item_sidemenu.view.icon
import kotlinx.android.synthetic.main.item_sidemenu.view.label
import org.lindoor.R
import org.lindoor.customisation.ActionTypes
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ItemActionInfoBinding
import org.lindoor.entities.Action

class DeviceInfoActionsAdapter(private val actions: ArrayList<Action>) : RecyclerView.Adapter<DeviceInfoActionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_action_info,parent,false) as ItemActionInfoBinding
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon = itemView.icon
        private val type:TextView = itemView.label
        private val code:TextView = itemView.code

        fun bindItems(action: Action) {
            type.text = action.typeName()
            code.text = action.code
            Theme.setIcon(ActionTypes.iconNameForActionType(action.type!!), icon)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(actions[position])
    }
}