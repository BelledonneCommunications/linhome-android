package org.lindoor.ui.call

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.chunk_call_device_icon_or_video.view.*
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.R
import org.lindoor.databinding.ActivityCallInProgressBinding
import org.linphone.core.Call


class CallInProgressActivity : AppCompatActivity () {

    lateinit var binding : ActivityCallInProgressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(uiOptions)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_in_progress) as ActivityCallInProgressBinding
        binding.lifecycleOwner = this

        intent.getSerializableExtra("call")?.let {
            val callViewModel = ViewModelProvider(this, CallViewModelFactory(it as Call))[CallViewModel::class.java]
            binding.callmodel = callViewModel
            callViewModel.callState?.observe(this, Observer { callState ->
                when (callState) {
                    Call.State.End -> finish()
                }
            })
        }  ?: finish()
    }

    override fun onResume() {
        super.onResume()
        coreContext.core.nativeVideoWindowId = binding.root.video
    }

    override fun onPause() {
        coreContext.core.nativeVideoWindowId = null
        super.onPause()
    }

}
