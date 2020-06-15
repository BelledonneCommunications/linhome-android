/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
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
package org.lindoor.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.TextureView
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lindoor.LindoorApplication
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.MainActivity
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.linphonecore.CoreService
import org.lindoor.linphonecore.extensions.extendedAcceptEarlyMedia
import org.lindoor.linphonecore.extensions.historyEvent
import org.lindoor.store.DeviceStore
import org.lindoor.ui.call.CallInProgressActivity
import org.lindoor.ui.call.CallIncomingActivity
import org.lindoor.ui.call.CallOutgoingActivity
import org.lindoor.utils.cdlog
import org.lindoor.utils.extensions.existsAndIsNotEmpty
import org.lindoor.utils.pxFromDp
import org.linphone.compatibility.Compatibility
import org.linphone.core.Call
import org.linphone.core.CallListenerStub
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.tools.Log
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer


private class Notifiable(val notificationId: Int) {
    val messages: ArrayList<NotifiableMessage> = arrayListOf()

    var isGroup: Boolean = false
    var groupTitle: String? = null
    var localIdentity: String? = null
    var myself: String? = null
}

private class NotifiableMessage(
    var message: String,
    val sender: String,
    val time: Long,
    val senderAvatar: Bitmap? = null,
    var filePath: Uri? = null,
    var fileMime: String? = null
)

class NotificationsManager(private val context: Context) {
    companion object {
        const val INTENT_NOTIF_ID = "NOTIFICATION_ID"
        const val INTENT_HANGUP_CALL_NOTIF_ACTION = "org.lindoor.HANGUP_CALL_ACTION"
        const val INTENT_ANSWER_CALL_NOTIF_ACTION = "org.lindoor.ANSWER_CALL_ACTION"
        const val INTENT_LOCAL_IDENTITY = "LOCAL_IDENTITY"

        private const val SERVICE_NOTIF_ID = 1
        private const val MISSED_CALLS_NOTIF_ID = 2
    }


    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }
    private val chatNotificationsMap: HashMap<String, Notifiable> = HashMap()
    private val callNotificationsMap: HashMap<String, Notifiable> = HashMap()

    private var lastNotificationId: Int = 5
    private var currentForegroundServiceNotificationId: Int = 0
    private var serviceNotification: Notification? = null
    var service: CoreService? = null

    private val coreListener: CoreListenerStub = object : CoreListenerStub() {
        override fun onCallStateChanged(
            core: Core?,
            call: Call?,
            state: Call.State?,
            message: String?
        ) {
            if (call == null) return
            Log.i("[Notifications Manager] Call state changed [$state]")

            when (state) {
                Call.State.IncomingEarlyMedia, Call.State.IncomingReceived -> displayIncomingCallNotification(call)
                Call.State.End, Call.State.Error -> dismissCallNotification(call)
                Call.State.Released -> {
                    if (call.callLog?.status == Call.Status.Missed) {
                        displayMissedCallNotification(call)
                    }
                }
                else -> displayCallNotification(call)
            }
        }
    }



    var fileLenght = 0L
    var incomingCallSnapshotTimer : Timer? = null
    lateinit private  var notificationBuilder: NotificationCompat.Builder
    private var callListener = object : CallListenerStub() {
        override fun onStateChanged(call: Call?, cstate: Call.State?, message: String?) {
            if (cstate != Call.State.IncomingEarlyMedia || cstate != Call.State.IncomingReceived)
                incomingCallSnapshotTimer?.cancel()
        }
        override fun onNextVideoFrameDecoded(call: Call?) {
            cdlog("got a frame")
            if (call != null) {
                call.takeVideoSnapshot(call.callLog.historyEvent().mediaThumbnail.absolutePath)
                    GlobalScope.launch(context = Dispatchers.Main) {
                        delay(1000)
                        call.callLog.historyEvent().mediaThumbnail.also {
                            if (it.existsAndIsNotEmpty() && it.length() != fileLenght && !LindoorApplication.someActivityRunning) {
                                fileLenght = it.length()
                                val notificationLayoutHeadsUp = fillIncomingRemoteViewsForCall(call,true)
                                val awt = AppWidgetTarget(
                                    context.applicationContext,
                                    R.id.caller_picture,
                                    notificationLayoutHeadsUp,
                                    0)
                                Glide.with(context.applicationContext).asBitmap().load(it).into(awt)
                                notificationBuilder.setCustomHeadsUpContentView(notificationLayoutHeadsUp)
                                val notification = notificationBuilder.build()
                                if (!LindoorApplication.someActivityRunning && call.state == Call.State.IncomingEarlyMedia)
                                    notify(getNotifiableForCall(call).notificationId, notification)
                            }
                        }
                    }
            }
        }
    }

    init {
        notificationManager.cancelAll()

        Compatibility.createNotificationChannels(context, notificationManager)
    }

    fun onCoreReady() {
        coreContext.core.addListener(coreListener)
    }

    fun destroy() {
        // Don't use cancelAll to keep message notifications !
        // When a message is received by a push, it will create a CoreService
        // but it might be getting killed quite quickly after that
        // causing the notification to be missed by the user...
        Log.i("[Notifications Manager] Getting destroyed, clearing foreground Service & call notifications")

        if (currentForegroundServiceNotificationId > 0) {
            notificationManager.cancel(currentForegroundServiceNotificationId)
        }

        for (notifiable in callNotificationsMap.values) {
            notificationManager.cancel(notifiable.notificationId)
        }

        stopForegroundNotification()
        coreContext.core.removeListener(coreListener)
    }

    private fun notify(id: Int, notification: Notification) {
        Log.i("[Notifications Manager] Notifying $id")
        notificationManager.notify(id, notification)
    }

    fun cancel(id: Int) {
        Log.i("[Notifications Manager] Canceling $id")
        notificationManager.cancel(id)
    }

    fun getSipUriForCallNotificationId(notificationId: Int): String? {
        for (address in callNotificationsMap.keys) {
            if (callNotificationsMap[address]?.notificationId == notificationId) {
                return address
            }
        }
        return null
    }

    /* Service related */

    fun startForeground() {
        val coreService = service
        if (coreService != null) {
            startForeground(coreService, useAutoStartDescription = false)
        } else {
            Log.e("[Notifications Manager] Can't start service as foreground, no service!")
        }
    }

    fun startCallForeground(coreService: CoreService) {
        service = coreService
        when {
            currentForegroundServiceNotificationId != 0 -> {
                Log.e("[Notifications Manager] There is already a foreground service notification")
            }
            coreContext.core.callsNb > 0 -> {
                // When this method will be called, we won't have any notification yet
                val call = coreContext.core.currentCall ?: coreContext.core.calls[0]
                when (call.state) {
                    Call.State.IncomingReceived, Call.State.IncomingEarlyMedia -> {
                        //displayIncomingCallNotification(call, true)
                    }
                    else -> displayCallNotification(call, true)
                }
            }
        }
    }

    fun startForeground(coreService: CoreService, useAutoStartDescription: Boolean = true) {
        Log.i("[Notifications Manager] Starting Service as foreground")
        if (serviceNotification == null) {
            createServiceNotification(useAutoStartDescription)
        }
        currentForegroundServiceNotificationId = SERVICE_NOTIF_ID
        coreService.startForeground(currentForegroundServiceNotificationId, serviceNotification)
        service = coreService
    }

    private fun startForeground(notificationId: Int, callNotification: Notification) {
        if (currentForegroundServiceNotificationId == 0 && service != null) {
            Log.i("[Notifications Manager] Starting Service as foreground using call notification")
            currentForegroundServiceNotificationId = notificationId
            service?.startForeground(currentForegroundServiceNotificationId, callNotification)
        }
   }

    private fun stopForegroundNotification() {
        if (service != null) {
            Log.i("[Notifications Manager] Stopping Service as foreground")
            service?.stopForeground(true)
            currentForegroundServiceNotificationId = 0
        }
    }

    fun stopForegroundNotificationIfPossible() {
        if (service != null && currentForegroundServiceNotificationId == SERVICE_NOTIF_ID && !corePreferences.keepServiceAlive) {
            stopForegroundNotification()
        }
    }

    fun stopCallForeground() {
        if (service != null && currentForegroundServiceNotificationId != SERVICE_NOTIF_ID && !corePreferences.keepServiceAlive) {
            stopForegroundNotification()
        }
    }



    private fun createServiceNotification(useAutoStartDescription: Boolean = false) {
        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.fragments_graph)
            .setDestination(R.id.navigation_devices)
            .createPendingIntent()

        serviceNotification = NotificationCompat.Builder(context,Texts.get("notification_channel_service_id"))
            .setContentTitle(Texts.get("service_name"))
            .setContentText(if (useAutoStartDescription)Texts.get("service_auto_start_description") else Texts.get("service_description"))
            .setSmallIcon(R.drawable.ic_lindoor_icon)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context, R.color.color_a))
            .build()
    }

    /* Call related */

    private fun getNotifiableForCall(call: Call): Notifiable {
        val address = call.remoteAddress.asStringUriOnly()
        var notifiable: Notifiable? = callNotificationsMap[address]
        if (notifiable == null) {
            notifiable = Notifiable(lastNotificationId)
            lastNotificationId += 1
            callNotificationsMap[address] = notifiable
        }
        return notifiable
    }


    private fun fillIncomingRemoteViewsForCall(call:Call, hasSnapShot:Boolean):RemoteViews {
        val address = call.remoteAddress.asStringUriOnly()
        val displayName = DeviceStore.findDeviceByAddress(call.remoteAddress)?.name ?: call.remoteAddress.username
        val notificationLayoutHeadsUp =
            if (hasSnapShot)
                RemoteViews(context.packageName, R.layout.call_incoming_notification_heads_up_snapshot)
            else
                RemoteViews(context.packageName, R.layout.call_incoming_notification_heads_up)

        notificationLayoutHeadsUp.setTextViewText(R.id.caller, displayName)
        notificationLayoutHeadsUp.setTextViewText(R.id.sip_uri, address)
        notificationLayoutHeadsUp.setTextViewText(R.id.incoming_call_info, Texts.get("notif_incoming_call_title"))
        return notificationLayoutHeadsUp
    }


    private fun displayIncomingCallNotification(call: Call, useAsForeground: Boolean = false) {

        val notifiable = getNotifiableForCall(call)
        val displayName = DeviceStore.findDeviceByAddress(call.remoteAddress)?.name ?: call.remoteAddress.username

        val incomingCallNotificationIntent = Intent(context, CallIncomingActivity::class.java)
        incomingCallNotificationIntent.putExtra("callId",call.callLog.callId)
        incomingCallNotificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0, incomingCallNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        notificationBuilder = NotificationCompat.Builder(context, Texts.get("notification_channel_incoming_call_id"))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSmallIcon(R.drawable.notification_phone)
            .setContentTitle(displayName)
            .setContentText(Texts.get("notif_incoming_call_title"))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setShowWhen(true)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context, R.color.color_a))
            .setFullScreenIntent(pendingIntent, true)
            .addAction(getCallDeclineAction(notifiable.notificationId))
            .addAction(getCallAnswerAction(notifiable.notificationId))
            .setCustomHeadsUpContentView(fillIncomingRemoteViewsForCall(call,false))

        val notification = notificationBuilder.build()

        if (!LindoorApplication.someActivityRunning)
            notify(notifiable.notificationId, notification)


        if (useAsForeground) {
            startForeground(notifiable.notificationId, notification)
        }

        val fakeTextureView = TextureView(LindoorApplication.instance.applicationContext)
        coreContext.core.nativeVideoWindowId = fakeTextureView
        call.addListener(callListener)
        call.extendedAcceptEarlyMedia()
        scheduleSnapShots(call)
    }


    fun scheduleSnapShots(call:Call) {
        incomingCallSnapshotTimer = fixedRateTimer("timer",false,1000,1000){
            GlobalScope.launch(context = Dispatchers.Main) {
                if (call.state == Call.State.IncomingEarlyMedia) {
                    call.requestNotifyNextVideoFrameDecoded()
                    cdlog("requesting video frame")
                }
            }
        }
    }


    fun displayMissedCallNotification(call: Call) {
        val missedCallCount: Int = call.core.missedCallsCount
        val body: String
        if (missedCallCount > 1) {
            body = Texts.get("notif_missed_calls",missedCallCount.toString())
            Log.i("[Notifications Manager] Updating missed calls notification count to $missedCallCount")
        } else {
            val name = DeviceStore.findDeviceByAddress(call.remoteAddress)?.name?: call.remoteAddress.username
            body = Texts.get("notif_missed_call",name)
            Log.i("[Notifications Manager] Creating missed call notification")
        }

        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.fragments_graph)
            .setDestination(R.id.navigation_history)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(
            context,  Texts.get("notification_channel_incoming_call_id"))
            .setContentTitle(Texts.get("notif_missed_call_title"))
            .setContentText(body)
            .setSmallIcon(R.drawable.notification_missed)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setNumber(missedCallCount)
            .setColor(Theme.getColor("color_s"))
            .build()
        notify(MISSED_CALLS_NOTIF_ID, notification)
    }

    fun dismissMissedCallNotification() {
        cancel(MISSED_CALLS_NOTIF_ID)
    }

    fun displayCallNotification(call: Call, useAsForeground: Boolean = false) {
        val notifiable = getNotifiableForCall(call)

        val stringResourceId: String
        val iconResourceId: Int
        val callActivity: Class<*>
        when (call.state) {
            Call.State.OutgoingRinging, Call.State.OutgoingProgress, Call.State.OutgoingInit, Call.State.OutgoingEarlyMedia -> {
                callActivity = CallOutgoingActivity::class.java
                stringResourceId = Texts.get("notif_outgoing_call")
                iconResourceId = R.drawable.notification_phone
            }
            else -> {
                callActivity = CallInProgressActivity::class.java
                stringResourceId = Texts.get("notif_in_call")
                iconResourceId = R.drawable.notification_phone
            }
        }

        val callNotificationIntent = Intent(context, callActivity)
        callNotificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0, callNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)



        val notification = NotificationCompat.Builder(
            context, Texts.get("notification_channel_service_id"))
            .setContentTitle(DeviceStore.findDeviceByAddress(call.remoteAddress)?.name ?: call.remoteAddress.username)
            .setContentText(stringResourceId)
            .setSmallIcon(iconResourceId)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setOngoing(true)
            .setColor(Theme.getColor("color_s"))
            .addAction(getCallDeclineAction(notifiable.notificationId))
            .build()
        notify(notifiable.notificationId, notification)

        if (useAsForeground) {
            startForeground(notifiable.notificationId, notification)
        }
    }

    private fun dismissCallNotification(call: Call) {
        val address = call.remoteAddress?.asStringUriOnly()
        val notifiable: Notifiable? = callNotificationsMap[address]
        if (notifiable != null) {
            cancel(notifiable.notificationId)
            callNotificationsMap.remove(address)
        }
    }


    /* Notifications actions */

    private fun getCallAnswerAction(callId: Int): NotificationCompat.Action {

        val answerIntent = Intent(context, NotificationBroadcastReceiver::class.java)
        answerIntent.action = INTENT_ANSWER_CALL_NOTIF_ACTION
        answerIntent.putExtra(INTENT_NOTIF_ID, callId)

        val answerPendingIntent = PendingIntent.getBroadcast(
            context, callId, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Action.Builder(
            R.drawable.notification_phone,
            Texts.get("call_button_accept"),
            answerPendingIntent
        ).build()
    }

    private fun getCallDeclineAction(callId: Int): NotificationCompat.Action {
        val hangupIntent = Intent(context, NotificationBroadcastReceiver::class.java)
        hangupIntent.action = INTENT_HANGUP_CALL_NOTIF_ACTION
        hangupIntent.putExtra(INTENT_NOTIF_ID, callId)

        val hangupPendingIntent = PendingIntent.getBroadcast(
            context, callId, hangupIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Action.Builder(
            R.drawable.notification_decline,
            Texts.get("call_button_decline"),
            hangupPendingIntent
        ).build()
    }



}
