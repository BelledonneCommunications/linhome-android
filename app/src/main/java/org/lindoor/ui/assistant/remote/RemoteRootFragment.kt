package org.lindoor.ui.assistant.remote

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_assistant_remote_qr.view.*
import kotlinx.android.synthetic.main.fragment_assistant_remote_root.view.*
import kotlinx.android.synthetic.main.fragment_assistant_remote_root.view.infobutton
import kotlinx.android.synthetic.main.fragment_assistant_remote_root.view.infotext
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.GenericFragment
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantRemoteRootBinding.inflate
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.extensions.toogleVisible
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class RemoteRootFragment : GenericFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflate(inflater, container, false)

        binding.root.url.root.setOnClickListener {
            mainactivity.navController.navigate(R.id.navigation_assistant_remote_url)
        }

        binding.root.qr.root.setOnClickListener {
            openQRCodeViewWithPermissionCheck()
        }

        binding.root.infobutton.setOnClickListener {
            binding.root.infotext.toogleVisible()
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun openQRCodeView() {
        mainactivity.navController.navigate(R.id.navigation_assistant_remote_qr)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
        DialogUtil.error("camera_permission_denied")
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
        DialogUtil.error("camera_permission_denied_dont_ask_again")
    }


}
