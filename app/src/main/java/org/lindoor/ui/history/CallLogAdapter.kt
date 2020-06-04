package org.lindoor.ui.history

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.lindoor.BR
import org.lindoor.GenericFragment
import org.lindoor.R
import org.lindoor.databinding.ItemHistoryCallLogBinding
import org.lindoor.ui.player.PlayerActivity
import org.linphone.core.CallLog

class CallLogAdapter(var callLogs: MutableList<CallLog>, val historyViewModel: HistoryViewModel, val lindoorFragment: GenericFragment) :
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
        fun bind(callLog: CallLog,showDate:Boolean, historyViewModel: HistoryViewModel,lindoorFragment: GenericFragment) {
            val entryViewModel = CallLogViewModel(callLog,showDate,historyViewModel)
            binding.lifecycleOwner = lindoorFragment
            binding.setVariable(BR.model,entryViewModel)
            binding.setVariable(BR.historymodel,historyViewModel)
            binding.executePendingBindings()
            entryViewModel.viewMedia.observe(lindoorFragment, Observer { play ->
                if (play)
                    playMedia(callLog,lindoorFragment)
            })
        }
        fun playMedia(callLog:CallLog,lindoorFragment: GenericFragment) {
            val intent = Intent(lindoorFragment.activity, PlayerActivity::class.java)
            intent.putExtra("callId",callLog.callId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            lindoorFragment.activity?.startActivity(intent)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val callLog = callLogs.get(position)
        holder.bind(callLog,position == 0 || (callLogs.get(position-1).startDate / 86400 != callLog.startDate/86400),historyViewModel,lindoorFragment)
    }



}