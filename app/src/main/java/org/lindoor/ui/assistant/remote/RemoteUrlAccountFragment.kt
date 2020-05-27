package org.lindoor.ui.assistant.remote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_assistant_remote_url.*
import kotlinx.android.synthetic.main.fragment_assistant_remote_url.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorFragment
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantRemoteUrlBinding
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.invisible
import org.lindoor.utils.toogleVisible
import org.linphone.core.ConfiguringState

class RemoteUrlAccountFragment :LindoorFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantRemoteUrlBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(RemoteAnyAccountViewModel::class.java)
        binding.model = model
        binding.validators  = ValidatorFactory.Companion

        binding.root.apply.root.setOnClickListener{
            binding.root.url.validate()
            if (model.valid()) {
                apply.root.isEnabled = false
                model.configurationResult.observe(viewLifecycleOwner, Observer { status ->
                    hideProgress()
                    apply.root.isEnabled = true
                    when (status) {
                        ConfiguringState.Successful -> {
                            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                            DialogUtil.info("lindoor_account_remote_url_success")
                        }
                        ConfiguringState.Failed -> {
                            DialogUtil.error("lindoor_account_remote_url_failed")
                        }
                        ConfiguringState.Skipped -> {
                            DialogUtil.error("lindoor_account_remote_url_skipped")
                        }
                    }
                })
                hideKeyboard()
                showProgress()
                coreContext.core.provisioningUri = model.url.first.value
                coreContext.core.stop()
                coreContext.core.start()
            }
        }

        binding.root.infobutton.setOnClickListener {
            binding.root.infotext.toogleVisible()
        }

        binding.root.setOnClickListener {
            binding.root.infotext.invisible()
        }

        return binding.root
    }

}

