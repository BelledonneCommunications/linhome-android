package org.lindoor.ui.player

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.SystemClock
import android.view.TextureView
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_player.view.*
import kotlinx.android.synthetic.main.chunk_player_controls.view.*
import org.lindoor.GenericActivity
import org.lindoor.LindoorApplication
import org.lindoor.R
import org.lindoor.databinding.ActivityPlayerBinding
import org.lindoor.linphonecore.extensions.historyEvent
import org.linphone.core.AudioDevice
import org.linphone.core.Player
import org.linphone.core.tools.Log


class PlayerActivity : GenericActivity(allowsLandscapeOnSmartPhones = true) {


    lateinit var binding: ActivityPlayerBinding
    lateinit var playerViewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions


        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_player) as ActivityPlayerBinding
        binding.lifecycleOwner = this
        intent.getStringExtra("callId")?.also { callId ->
            LindoorApplication.coreContext.core.findCallLogFromCallId(callId)
                ?.historyEvent()?.also { event ->
                    var speakerCard: String? = null
                    var earpieceCard: String? = null
                    for (device in LindoorApplication.coreContext.core.audioDevices) {
                        if (device.hasCapability(AudioDevice.Capabilities.CapabilityPlay)) {
                            if (device.type == AudioDevice.Type.Speaker) {
                                speakerCard = device.id
                            } else if (device.type == AudioDevice.Type.Earpiece) {
                                earpieceCard = device.id
                            }
                        }
                    }

                    LindoorApplication.coreContext.core.createLocalPlayer(
                        speakerCard ?: earpieceCard,
                        null,
                        null
                    )?.also { player ->
                        playerViewModel =
                            ViewModelProvider(
                                this@PlayerActivity,
                                PlayerViewModelFactory(callId, player)
                            )[PlayerViewModel::class.java]
                        binding.model = playerViewModel
                        binding.root.cancel.setOnClickListener {
                            binding.root.cancel.alpha = 0.3f
                            finish()
                        }
                        setupPlayerControl(binding.root, playerViewModel)
                        if (event.hasVideo) {
                            setTextureView(binding.root.video,player)
                        }
                    }
            }
        } ?: run {
            this.finish()
        }
    }


    private fun setupPlayerControl(view: View, model: PlayerViewModel) {
        model.playing.observe(this, Observer { playing ->
            if (playing) {
                view.timer.start()
            } else {
                view.timer.stop()
            }
        })

        model.resetEvent.observe(this, Observer { reset ->
            if (reset)
                view.timer.base = SystemClock.elapsedRealtime()
        })

        model.seekPosition.observe(this, Observer { p ->
            view.timer.base = SystemClock.elapsedRealtime() - p
        })

        view.timer.setOnChronometerTickListener {
            model.updatePosition()
        }

    }

    fun setTextureView(textureView: TextureView, player: Player) {
        Log.i("[Player] Is TextureView available? ${textureView.isAvailable}")
        if (textureView.isAvailable) {
            player.setWindowId(textureView.surfaceTexture)
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) { }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) { }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    return true
                }

                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                    Log.i("[Player] Surface texture should be available now")
                    player.setWindowId(textureView.surfaceTexture)
                    playerViewModel.playFromStart()
                }
            }
        }
    }

}