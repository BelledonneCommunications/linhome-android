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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_player.view.*
import kotlinx.android.synthetic.main.chunk_player_controls.view.*
import kotlinx.android.synthetic.main.item_history.*
import org.linhome.GenericActivity
import org.linhome.LinhomeApplication
import org.linhome.R
import org.linhome.databinding.ActivityPlayerBinding
import org.linhome.linphonecore.extensions.historyEvent
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
                        binding.root.cancel.setOnClickListener {
                            binding.root.cancel.alpha = 0.3f
                            finish()
                        }
                        setupPlayerControl(binding.root, playerViewModel)
                        if (event.hasVideo) {
                            setTextureView(binding.root.video, player,seekTo,playing)
                        }
                    }
            }

        } ?: run {
            this.finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
        outState.putInt("position",  playerViewModel.position.value!!)
        outState.putBoolean("playing",  playerViewModel.playing.value!!)
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

    fun setTextureView(textureView: TextureView, player: Player, seekTo:Int, playing:Boolean) {
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
                    if (seekTo == 0)
                        playerViewModel.playFromStart()
                    else {
                        playerViewModel.targetSeek = seekTo
                        playerViewModel.seekPosition.value = seekTo
                        playerViewModel.playing.value = true
                        playerViewModel.seek()
                        if (!playing)
                            playerViewModel.togglePlay()
                    }
                }
            }
        }
    }


    override fun onPause() {
        playerViewModel?.pausePlay()
        super.onPause()
    }

    override fun onDestroy() {
        playerViewModel?.close()
        super.onDestroy()
    }

}