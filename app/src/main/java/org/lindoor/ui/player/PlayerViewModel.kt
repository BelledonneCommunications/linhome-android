package org.lindoor.ui.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.lindoor.LindoorApplication
import org.lindoor.linphonecore.extensions.historyEvent
import org.linphone.core.Player
import org.linphone.core.PlayerListener


class PlayerViewModelFactory(private val callId: String, private val player: Player) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PlayerViewModel(callId, player) as T
    }
}

class PlayerViewModel(val callId: String, val player: Player) : ViewModel() {

    val historyEvent =
        LindoorApplication.coreContext.core.findCallLogFromCallId(callId)?.historyEvent()
    val playing = MutableLiveData(false)
    val position: MutableLiveData<Int> = MutableLiveData(0)
    val resetEvent = MutableLiveData(false)
    val seekPosition = MutableLiveData(0)

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

}
