package org.lindoor.ui.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.lindoor.LindoorApplication
import org.lindoor.linphonecore.extensions.historyEvent
import org.linphone.core.Player
import org.linphone.core.PlayerListener
import org.linphone.core.tools.Log
import java.util.*


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
    val reset = MutableLiveData(false)


    lateinit var date: Date

    val duration: Int
        get() {
            if (isClosed())
                if (historyEvent != null) {
                    player.open(historyEvent.mediaFileName)
                }
            return player.duration
        }


    private val listener = PlayerListener {
        Log.i("[Recording] End of file reached")
        playing.value = false
        reset.value = true
        seek(0)
    }

    init {
        player.addListener(listener)
    }

    override fun onCleared() {
        if (!isClosed()) player.close()
        player.removeListener(listener)

        super.onCleared()
    }

    fun togglePlay() {
        if (isClosed())
            if (historyEvent != null) {
                player.open(historyEvent.mediaFileName)
            }
        if (playing.value!!)
            player.pause()
        else
            player.start()
        playing.value = !playing.value!!
    }

    fun playFromStart() {
        if (isClosed())
            if (historyEvent != null) {
                player.open(historyEvent.mediaFileName)
            }
        seek(0)
        player.start()
        playing.value = true
        reset.value = true
    }


    fun seek(p: Int) {
        if (!isClosed()) {
            if (playing.value!!)
                player.pause()
            player.seek(p)
            if (playing.value!!)
                player.start()
            updatePosition()
        }
    }

    fun updatePosition() {
        if (!isClosed()) {
            position.value = player.currentPosition
        }
    }

    private fun isClosed(): Boolean {
        return player.state == Player.State.Closed
    }
}
