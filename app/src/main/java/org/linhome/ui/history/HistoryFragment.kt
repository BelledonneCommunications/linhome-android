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

package org.linhome.ui.history

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
import org.linhome.GenericFragment
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.FragmentHistoryBinding
import org.linhome.store.HistoryEventStore
import org.linhome.utils.DialogUtil


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
