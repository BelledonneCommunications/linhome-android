package org.lindoor.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_history_call_log.view.*
import org.lindoor.BR
import org.lindoor.R
import org.lindoor.databinding.ItemHistoryCallLogBinding
import org.lindoor.utils.databindings.roundRectWithColor
import org.linphone.core.CallLog

class CallLogAdapter(val callLogs: List<CallLog>) :
    RecyclerView.Adapter<CallLogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_history_call_log,
            parent,
            false
        ) as ItemHistoryCallLogBinding
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return callLogs.size
    }

    override fun getItemId(position: Int): Long {
        return callLogs.get(position).startDate
    }

    class ViewHolder(val binding: ItemHistoryCallLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(callLog: CallLog,showDate:Boolean) {
            binding.setVariable(BR.model,CallLogViewModel(callLog,showDate))
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val callLog = callLogs.get(position)
        holder.bind(callLog,position == 0 || (callLogs.get(position-1).startDate / 86400 != callLog.startDate/86400))
    }
}