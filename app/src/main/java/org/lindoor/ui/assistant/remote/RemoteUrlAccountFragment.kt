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
import org.lindoor.GenericFragment
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantRemoteUrlBinding
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.extensions.invisible
import org.lindoor.utils.extensions.toogleVisible
import org.linphone.core.ConfiguringState

class RemoteUrlAccountFragment : GenericFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantRemoteUrlBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(RemoteAnyAccountViewModel::class.java)
        binding.model = model
        binding.validators = ValidatorFactory.Companion

        binding.root.apply.root.setOnClickListener {
            binding.root.url.validate()
            if (model.valid()) {
                apply.root.isEnabled = false
                model.configurationResult.observe(viewLifecycleOwner, Observer { status ->
                    hideProgress()
                    apply.root.isEnabled = true
                    when (status) {
                        ConfiguringState.Failed -> {
                            DialogUtil.error("remote_configuration_failed")
                        }
                        ConfiguringState.Skipped -> {
                            DialogUtil.error("remote_configuration_skipped")
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

        model.pushReady.observe(viewLifecycleOwner, Observer { status ->
            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
            if (status) {
                DialogUtil.info("remote_configuration_success")
            } else
                DialogUtil.error("failed_creating_pushgateway")
        })

        binding.root.infobutton.setOnClickListener {
            binding.root.infotext.toogleVisible()
        }

        binding.root.setOnClickListener {
            binding.root.infotext.invisible()
        }

        return binding.root
    }

}

