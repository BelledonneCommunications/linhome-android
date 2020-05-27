package org.lindoor.ui.assistant.remote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_assistant_remote_qr.view.*
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorFragment
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantRemoteQrBinding
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.toogleVisible
import org.linphone.core.ConfiguringState
import org.linphone.core.tools.Log

class RemoteQrAccountFragment :LindoorFragment() {

    lateinit var binding:FragmentAssistantRemoteQrBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAssistantRemoteQrBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val model = ViewModelProvider(this).get(RemoteAnyAccountViewModel::class.java)
        model.configurationResult.observe(viewLifecycleOwner, Observer { status ->
            hideProgress()
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
        model.qrCodeFound.observe(viewLifecycleOwner, Observer { url ->
            showProgress()
            coreContext.core.provisioningUri = url
            coreContext.core.stop()
            coreContext.core.start()
        })

        binding.root.infobutton.setOnClickListener {
            binding.root.infotext.toogleVisible()
        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        setBackCamera()
        coreContext.core.nativePreviewWindowId = binding.qrcode
        coreContext.core.enableQrcodeVideoPreview(true)
        coreContext.core.enableVideoPreview(true)
    }

    private fun setBackCamera() {
        for (camera in coreContext.core.videoDevicesList) {
            if (camera.contains("Back")) {
                Log.i("[QR Code] Found back facing camera: $camera")
                coreContext.core.videoDevice = camera
                return
            }
        }

        val first = coreContext.core.videoDevicesList[0]
        Log.i("[QR Code] Using first camera found: $first")
        coreContext.core.videoDevice = first
    }

    override fun onPause() {
        coreContext.core.nativePreviewWindowId = null
        coreContext.core.enableQrcodeVideoPreview(false)
        coreContext.core.enableVideoPreview(false)
        super.onPause()
    }



}



