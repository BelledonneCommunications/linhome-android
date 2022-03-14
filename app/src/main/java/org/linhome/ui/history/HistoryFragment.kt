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
import org.linhome.GenericFragment
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.FragmentHistoryBinding
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

        binding.loglist.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.loglist.adapter =
            HistoryEventsAdapter(historyViewModel.history.value!!, historyViewModel, this)
        binding.loglist.setHasFixedSize(true)
        showProgress()
        binding.loglist.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                hideProgress()
                binding.loglist.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        historyViewModel.selectedForDeletion.observe(viewLifecycleOwner, Observer { list ->
            if (historyViewModel.editing.value!! && list.isEmpty()) {
                mainactivity.binding.appbar.toolbarRightButton.isEnabled = false
                mainactivity.binding.appbar.toolbarRightButton.alpha = 0.5f
            } else {
                mainactivity.binding.appbar.toolbarRightButton.isEnabled = true
                mainactivity.binding.appbar.toolbarRightButton.alpha = 1.0f
            }
        })

        historyViewModel.history.observe(viewLifecycleOwner, Observer { list ->
            mainactivity.toolbarViewModel.rightButtonVisible.value = !list.isEmpty()
            (binding.loglist.adapter as HistoryEventsAdapter).callLogs = list
            binding.loglist.adapter?.notifyDataSetChanged()
        })

        binding.selectall.setOnClickListener {
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
                    (binding.loglist.adapter as HistoryEventsAdapter).callLogs =
                        historyViewModel.history.value!!
                    binding.loglist.adapter?.notifyDataSetChanged()
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
        Theme.setIcon("icons/delete", mainactivity.binding.appbar.toolbarRightButtonImage)
        Theme.setIcon("icons/cancel", mainactivity.binding.appbar.toolbarLeftButtonImage)
        mainactivity.binding.appbar.toolbarLeftButtonTitle.text = Texts.get("cancel")
        mainactivity.binding.appbar.toolbarRightButtonTitle.text = Texts.get("delete")
        mainactivity.toolbarViewModel.rightButtonVisible.value =
            !historyViewModel.history.value!!.isEmpty()
        coreContext.notificationsManager.dismissMissedCallNotification()
        binding.loglist.adapter?.notifyDataSetChanged()

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
        mainactivity.tabbarViewModel.updateUnreadCount()
        super.onPause()
    }
}
