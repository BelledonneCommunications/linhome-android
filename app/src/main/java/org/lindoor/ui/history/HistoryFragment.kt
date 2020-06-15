package org.lindoor.ui.history

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.GenericFragment
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.FragmentHistoryBinding
import org.lindoor.store.HistoryEventStore
import org.lindoor.utils.DialogUtil


class HistoryFragment : GenericFragment() {

    private lateinit var historyViewModel: HistoryViewModel

    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        binding.lifecycleOwner = this
        binding.model = historyViewModel

        binding.root.loglist.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.root.loglist.adapter =
            HistoryEventsAdapter(historyViewModel.history.value!!, historyViewModel, this)
        binding.root.loglist.setHasFixedSize(true)
        showProgress()
        binding.root.loglist.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                hideProgress()
                binding.root.loglist.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        historyViewModel.selectedForDeletion.observe(viewLifecycleOwner, Observer { list ->
            if (historyViewModel.editing.value!! && list.isEmpty()) {
                mainactivity.toolbar_right_button.isEnabled = false
                mainactivity.toolbar_right_button.alpha = 0.5f
            } else {
                mainactivity.toolbar_right_button.isEnabled = true
                mainactivity.toolbar_right_button.alpha = 1.0f
            }
        })

        historyViewModel.history.observe(viewLifecycleOwner, Observer { list ->
            mainactivity.toolbarViewModel.rightButtonVisible.value = !list.isEmpty()
            (binding.root.loglist.adapter as HistoryEventsAdapter).callLogs = list
            binding.root.loglist.adapter?.notifyDataSetChanged()
        })

        binding.root.selectall.root.setOnClickListener {
            historyViewModel.toggleSelectAllForDeletion()
        }

        return binding.root
    }


    override fun onToolbarRightButtonClicked() {
        if (historyViewModel.editing.value!!) {
            DialogUtil.confirm(
                "delete_history_confirm_message",
                { _: DialogInterface, _: Int ->
                    historyViewModel.deleteSelection()
                    (binding.root.loglist.adapter as HistoryEventsAdapter).callLogs =
                        historyViewModel.history.value!!
                    binding.root.loglist.adapter?.notifyDataSetChanged()
                    mainactivity.tabbarViewModel.updateUnreadCount()
                    exitEdition()
                }, (historyViewModel.selectedForDeletion.value!!.size).toString()
            )
        } else
            enterEdition()
    }

    override fun onToolbarLeftButtonClicked() {
        exitEdition()
    }

    override fun onResume() {
        super.onResume()
        Theme.setIcon("icons/delete", mainactivity.toolbar_right_button_image)
        Theme.setIcon("icons/cancel", mainactivity.toolbar_left_button_image)
        mainactivity.toolbar_left_button_title.text = Texts.get("cancel")
        mainactivity.toolbar_right_button_title.text = Texts.get("delete")
        mainactivity.toolbarViewModel.rightButtonVisible.value =
            !historyViewModel.history.value!!.isEmpty()
        coreContext.notificationsManager.dismissMissedCallNotification()

    }

    fun enterEdition() {
        mainactivity.toolbarViewModel.leftButtonVisible.value = true
        historyViewModel.editing.value = true
        historyViewModel.notifyDeleteSelectionListUpdated()
    }

    fun exitEdition() {
        mainactivity.toolbarViewModel.leftButtonVisible.value = false
        historyViewModel.editing.value = false
        historyViewModel.selectedForDeletion.value!!.clear()
        historyViewModel.notifyDeleteSelectionListUpdated()
    }


    override fun onPause() {
        HistoryEventStore.markAllAsRead()
        mainactivity.tabbarViewModel.updateUnreadCount()
        super.onPause()
    }
}
