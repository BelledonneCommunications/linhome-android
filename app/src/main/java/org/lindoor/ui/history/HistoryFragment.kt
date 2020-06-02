package org.lindoor.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_history.view.*
import org.lindoor.LindoorFragment
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.store.HistoryEventStore


class HistoryFragment : LindoorFragment() {

    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = org.lindoor.databinding.FragmentHistoryBinding.inflate(inflater, container, false)
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        binding.lifecycleOwner = this
        binding.model = historyViewModel

        binding.root.loglist.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.root.loglist.adapter = CallLogAdapter(historyViewModel.history.value!!)
        binding.root.loglist.setHasFixedSize(true)
        showProgress()
        binding.root.loglist.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                hideProgress()
                binding.root.loglist.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        return binding.root
    }


    override fun onToolbarRightButtonClicked() {

    }

    override fun onToolbarLeftButtonClicked() {
        mainactivity.navController.navigateUp()
    }

    override fun onResume() {
        super.onResume()

        Theme.setIcon("icons/delete", mainactivity.toolbar_right_button_image)
        Theme.setIcon("icons/cancel", mainactivity.toolbar_left_button_image)
        mainactivity.toolbar_left_button_title.text = Texts.get("cancel")
        mainactivity.toolbar_right_button_title.text = Texts.get("delete")

        mainactivity.pauseNavigation()

        mainactivity.toolbarViewModel.rightButtonVisible.value = true
        mainactivity.toolbarViewModel.leftButtonVisible.value = true
    }


    override fun onPause() {
        //HistoryEventStore.markAllAsRead()
        super.onPause()
    }
}
