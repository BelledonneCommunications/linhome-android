package org.lindoor.ui.widgets

import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.item_sidemenu.view.bottom_separator
import kotlinx.android.synthetic.main.item_sidemenu.view.icon
import kotlinx.android.synthetic.main.item_sidemenu.view.label
import kotlinx.android.synthetic.main.item_spinner.view.*
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ItemSpinnerBinding

class SpinnerItem (val textKey: String, val iconFile: String? = null, val backingKey:String? = null)
class LSpinnerAdapter(private val options: ArrayList<SpinnerItem>,val spinner:Spinner) :
    SpinnerAdapter {

    var height:Int? = null

    class ViewHolder(itemView: View) {
        val optionIconIV = itemView.icon
        val optionLabelTV:TextView = itemView.label
        val optionArrow:ImageView = itemView.arrow
        val optionSeparator:View = itemView.bottom_separator

        init {
            itemView.bottom_separator.setBackgroundColor(Theme.getColor("color_h"))
            itemView.background = Theme.selectionEffectAsStateListDrawable("dropdown_list")
        }
    }

    override fun isEmpty(): Boolean {
        return options.size == 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position,convertView,parent,true)
    }

    fun getView(position: Int, convertView: View?, parent: ViewGroup?, isDropDown:Boolean): View {
        var viewHolder:ViewHolder
        var view = convertView
        if (view == null) {
            view = (DataBindingUtil.inflate(LayoutInflater.from(parent?.context),R.layout.item_spinner,parent,false) as ItemSpinnerBinding).root
            height = view.getLayoutParams().height
            viewHolder = ViewHolder(view)
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
            spinner.setSelection(position)
        }
        view.isPressed = true
        if (isDropDown)
            view.background = Theme.roundRectInputBackgroundWithColorKeyAndRadius("color_i","user_input_corner_radius")
        if (!isDropDown && position == 0) {
            view.getLayoutParams().height = 1
            view.visibility = View.GONE
        } else {
            view.getLayoutParams().height = this.height!!
            view.visibility = View.VISIBLE
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
        return getView(position,convertView,parent,false)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
    }

    override fun getCount(): Int {
        return options.size
    }
}