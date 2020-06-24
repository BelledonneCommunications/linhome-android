package org.lindoor.ui.call

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lindoor.MainActivity
import org.lindoor.R
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ActivityCallOutgoingBinding
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.cdlog
import org.linphone.core.Call


class CallOutgoingActivity : CallGenericActivity() {

    lateinit var binding: ActivityCallOutgoingBinding
    private lateinit var callViewModel: CallViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_call_outgoing
        ) as ActivityCallOutgoingBinding
        binding.lifecycleOwner = this

        call?.also {
            callViewModel =
                ViewModelProvider(this, CallViewModelFactory(it))[CallViewModel::class.java]
            binding.callmodel = callViewModel
            callViewModel.callState.observe(this, Observer { callState ->
                when (callState) {
                    Call.State.OutgoingInit, Call.State.OutgoingProgress, Call.State.OutgoingRinging -> return@Observer
                    Call.State.Error -> {
                        DialogUtil.toast("unable_to_call_device", long = true)
                        finish()
                    }
                    else -> finish()
                }
            })

        } ?: finish()
    }

}
