package org.lindoor.ui.sidemenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_sidemenu.view.*
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ItemSidemenuBinding

class SideMenuAdapter(private val options: ArrayList<OptionMenu>, navController:NavController) : RecyclerView.Adapter<SideMenuAdapter.ViewHolder>() {

    val navigationController = navController

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_sidemenu,parent,false) as ItemSidemenuBinding
        return ViewHolder(binding.root,this)
    }

    override fun getItemCount(): Int {
        return options.size
    }

    class ViewHolder(itemView: View,adapter:SideMenuAdapter) : RecyclerView.ViewHolder(itemView) {
        private val optionIconIV = itemView.icon
        private val optionLabelTV:TextView = itemView.label
        private val optionsAdapter = adapter

        init {
            itemView.bottom_separator.setBackgroundColor(Theme.getColor("color_h"))
            itemView.background = Theme.selectionEffectAsStateListDrawable("sidemenu_option")
        }

        fun bindItems(option: OptionMenu) {
            optionLabelTV.text = Texts.get(option.textKey)
            Theme.setImage(option.iconFile,optionIconIV)
            itemView.setOnClickListener {
                optionsAdapter.navigationController.navigate(option.targetFragmentId)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(options[position])
    }
}