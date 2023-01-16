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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.linhome.LinhomeApplication
import org.linhome.linphonecore.extensions.historyEvent
import org.linphone.core.Player
import org.linphone.core.PlayerListener
import org.linphone.core.tools.Log


class PlayerViewModelFactory(private val callId: String, private val player: Player) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(callId, player) as T
    }
}

class PlayerViewModel(val callId: String, val player: Player) : ViewModel() {

    val historyEvent =
        LinhomeApplication.coreContext.core.findCallLogFromCallId(callId)?.historyEvent()
    val playing = MutableLiveData(false)
    val position = MutableLiveData(0)
    val resetEvent = MutableLiveData(false)
    val seekPosition = MutableLiveData(0)
    val trackingAllowed = MutableLiveData(false)

    var targetSeek: Int = 0

    val duration: Int
        get() {
            return player.duration
        }


    private val listener = PlayerListener {
        playing.value = false
        resetEvent.value = true
        targetSeek = 0
        seek()
    }

    init {
        player.addListener(listener)
        if (historyEvent != null) {
            player.open(historyEvent.mediaFileName)
            Log.i(historyEvent.mediaFileName,LinhomeApplication.coreContext.core.config.dump())
            trackingAllowed.value = LinhomeApplication.coreContext.core.config.getString("recording_formats", historyEvent.mediaFileName,"")?.lowercase()?.contains("h26") != true
        }
    }

    override fun onCleared() {
        player.close()
        player.removeListener(listener)
        super.onCleared()
    }

    fun togglePlay() {
        if (playing.value!!)
            player.pause()
        else
            player.start()
        playing.value = !playing.value!!
    }

    fun pausePlay() {
        if (playing.value!!) {
            player.pause()
            playing.value = false
        }
    }

    fun playFromStart() {
        targetSeek = 0
        seek()
        player.start()
        playing.value = true
        resetEvent.value = true
    }


    fun seek() {
        player.close()
        if (historyEvent != null) {
            player.open(historyEvent.mediaFileName)
        }
        player.seek(targetSeek)
        if (playing.value!!)
            player.start()
        updatePosition()
        seekPosition.value = targetSeek
    }

    fun updatePosition() {
            position.value = player.currentPosition
    }

    fun close() {
        player.close()
        player.removeListener(listener)
    }

}
