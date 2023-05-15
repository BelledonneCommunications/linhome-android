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

package org.linhome.ui.player

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.SystemClock
import android.view.TextureView
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.linhome.GenericActivity
import org.linhome.LinhomeApplication
import org.linhome.R
import org.linhome.databinding.ActivityPlayerBinding
import org.linhome.linphonecore.extensions.historyEvent
import org.linhome.store.HistoryEventStore
import org.linhome.ui.call.CallGenericActivity
import org.linhome.ui.call.CallGenericActivity.Companion.computePercentageWidth
import org.linhome.utils.getImageDimension
import org.linphone.core.Player
import org.linphone.core.tools.Log


class PlayerActivity : GenericActivity() {

    lateinit var binding: ActivityPlayerBinding
    lateinit var playerViewModel: PlayerViewModel
    var playingOnPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        var seekTo = 0
        var playing = true
        if (savedInstanceState != null) {
            seekTo = savedInstanceState.getInt("position")
            playing = savedInstanceState.getBoolean("playing")
        }

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_player) as ActivityPlayerBinding
        binding.lifecycleOwner = this
        intent.getStringExtra("callId")?.also { callId ->
            LinhomeApplication.coreContext.core.findCallLogFromCallId(callId)
                ?.historyEvent()?.also { event ->
                    LinhomeApplication.coreContext.getPlayer()?.also { player ->
                        viewModelStore.clear()
                        playerViewModel =
                            ViewModelProvider(
                                this,
                                PlayerViewModelFactory(callId, player)
                            )[PlayerViewModel::class.java]
                        binding.model = playerViewModel
                        binding.cancel.setOnClickListener {
                            binding.cancel.alpha = 0.3f
                            finish()
                        }
                        setupPlayerControl(binding, playerViewModel)
                        if (event.hasVideo) {
                            setTextureView(binding.video, player, seekTo, playing)
                        }
                        HistoryEventStore.markAsRead(event.id)
                        binding.chunkPlayerControls?.play?.setOnClickListener {
                            togglePlay()
                        }
                        event.mediaThumbnail.absolutePath.getImageDimension()?.also { size ->
                            binding.video.updateLayoutParams<ConstraintLayout.LayoutParams> {
                                dimensionRatio = "H,${size.width}:${size.height}"
                                matchConstraintPercentWidth = CallGenericActivity.computePercentageWidth(size,200) //  240Dp left for buttons and header
                            }                        }
                    }
            }

        } ?: run {
            this.finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.clear()
        outState.putInt("position", playerViewModel.position.value!!)
        outState.putBoolean("playing", playingOnPause)
        super.onSaveInstanceState(outState)
    }

    private fun setupPlayerControl(view: ActivityPlayerBinding, model: PlayerViewModel) {
        model.playing.observe(this, Observer { playing ->
            if (playing) {
                view.chunkPlayerControls?.timer?.start()
            } else {
                view.chunkPlayerControls?.timer?.stop()
            }
        })

        model.playing.observe(this) { p ->
            view.chunkPlayerControls?.timer?.base = SystemClock.elapsedRealtime() - playerViewModel.position.value!!
        }

        model.userTrackingPosition.observe(this) { p ->
            if (model.userTracking.value == true) {
                binding.chunkPlayerControls?.timer?.text =
                    String.format("%02d:%02d", (p / 1000) / 60, (p / 1000) % 60);
            }
        }

        view.chunkPlayerControls?.timer?.setOnChronometerTickListener {
            model.updatePosition()
        }
        view.chunkPlayerControls?.seek?.setOnTouchListener({ view, motionEvent -> false })

    }

    fun setTextureView(textureView: TextureView, player: Player, seekTo: Int, playing: Boolean) {
        Log.i("[Player] Is TextureView available? ${textureView.isAvailable}")
        if (textureView.isAvailable) {
            player.setWindowId(textureView.surfaceTexture)
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) { }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) { }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    player.close()
                    player.setWindowId(null)
                    return true
                }

                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    Log.i("[Player] Surface texture should be available now seekTo = ${seekTo}")
                    player.setWindowId(textureView.surfaceTexture)
                    playerViewModel.playFromStart()
                }
            }
        }
    }


    override fun onPause() {
        playingOnPause = playerViewModel.playing.value!!
        playerViewModel?.pausePlay()
        super.onPause()
    }

    override fun onDestroy() {
        playerViewModel?.close()
        super.onDestroy()
    }

    fun togglePlay() {
        if (playerViewModel.playing.value == true)
            binding.chunkPlayerControls?.timer?.stop()
        else {
            binding.chunkPlayerControls?.timer?.base = SystemClock.elapsedRealtime() - playerViewModel.position.value!!
            binding.chunkPlayerControls?.timer?.start()
        }
        playerViewModel.togglePlay()
    }

}