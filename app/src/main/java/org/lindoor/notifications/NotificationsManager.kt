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
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.MainActivity
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.linphonecore.CoreService
import org.linphone.compatibility.Compatibility
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.tools.Log


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

    private val listener: CoreListenerStub = object : CoreListenerStub() {
        override fun onCallStateChanged(
            core: Core?,
            call: Call?,
            state: Call.State?,
            message: String?
        ) {
            if (call == null) return
            Log.i("[Notifications Manager] Call state changed [$state]")

            when (state) {
                //Call.State.IncomingEarlyMedia, Call.State.IncomingReceived -> displayIncomingCallNotification(call)
                //Call.State.End, Call.State.Error -> dismissCallNotification(call)
                Call.State.Released -> {
                    if (call.callLog?.status == Call.Status.Missed) {
                  //      displayMissedCallNotification(call)
                    }
                }
                //else -> displayCallNotification(call)
            }
        }


    }

    init {
        notificationManager.cancelAll()

        Compatibility.createNotificationChannels(context, notificationManager)
    }

    fun onCoreReady() {
        coreContext.core.addListener(listener)
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
        coreContext.core.removeListener(listener)
    }

    private fun notify(id: Int, notification: Notification) {
        Log.i("[Notifications Manager] Notifying $id")
        notificationManager.notify(id, notification)
    }

    fun cancel(id: Int) {
        Log.i("[Notifications Manager] Canceling $id")
        notificationManager.cancel(id)
    }

    fun getSipUriForChatNotificationId(notificationId: Int): String? {
        for (address in chatNotificationsMap.keys) {
            if (chatNotificationsMap[address]?.notificationId == notificationId) {
                return address
            }
        }
        return null
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
                    //else -> displayCallNotification(call, true)
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
            //.setSmallIcon(R.drawable.topbar_service_notification)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .build()
    }
    /*

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

    private fun displayIncomingCallNotification(call: Call, useAsForeground: Boolean = false) {
        val address = call.remoteAddress.asStringUriOnly()
        val notifiable = getNotifiableForCall(call)

        val roundPicture = ImageUtils.getRoundBitmapFromUri(context, pictureUri)
        val displayName = contact?.fullName ?: LinphoneUtils.getDisplayName(call.remoteAddress)

        val incomingCallNotificationIntent = Intent(context, IncomingCallActivity::class.java)
        incomingCallNotificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0, incomingCallNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationLayoutHeadsUp = RemoteViews(context.packageName, R.layout.call_incoming_notification_heads_up)
        notificationLayoutHeadsUp.setTextViewText(R.id.caller, displayName)
        notificationLayoutHeadsUp.setTextViewText(R.id.sip_uri, address)
        notificationLayoutHeadsUp.setTextViewText(R.id.incoming_call_info, context.getString(R.string.incoming_call_notification_title))

        if (roundPicture != null) {
            notificationLayoutHeadsUp.setImageViewBitmap(R.id.caller_picture, roundPicture)
        }

        val notification = NotificationCompat.Builder(context, context.getString(R.string.notification_channel_incoming_call_id))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSmallIcon(R.drawable.topbar_call_notification)
            .setContentTitle(displayName)
            .setContentText(context.getString(R.string.incoming_call_notification_title))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setShowWhen(true)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context, R.color.primary_color))
            .setFullScreenIntent(pendingIntent, true)
            .addAction(getCallDeclineAction(notifiable.notificationId))
            .addAction(getCallAnswerAction(notifiable.notificationId))
            .setCustomHeadsUpContentView(notificationLayoutHeadsUp)
            .build()
        notify(notifiable.notificationId, notification)

        if (useAsForeground) {
            startForeground(notifiable.notificationId, notification)
        }
    }

    fun displayMissedCallNotification(call: Call) {
        val missedCallCount: Int = call.core.missedCallsCount
        val body: String
        if (missedCallCount > 1) {
            body = context.getString(R.string.missed_calls_notification_body)
                .format(missedCallCount)
            Log.i("[Notifications Manager] Updating missed calls notification count to $missedCallCount")
        } else {
            val contact: Contact? = coreContext.contactsManager.findContactByAddress(call.remoteAddress)
            body = context.getString(R.string.missed_call_notification_body)
                .format(contact?.fullName ?: LinphoneUtils.getDisplayName(call.remoteAddress))
            Log.i("[Notifications Manager] Creating missed call notification")
        }

        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.main_nav_graph)
            .setDestination(R.id.masterCallLogsFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(
            context, context.getString(R.string.notification_channel_incoming_call_id))
            .setContentTitle(context.getString(R.string.missed_call_notification_title))
            .setContentText(body)
            .setSmallIcon(R.drawable.call_status_missed)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setNumber(missedCallCount)
            .setColor(ContextCompat.getColor(context, R.color.notification_led_color))
            .build()
        notify(MISSED_CALLS_NOTIF_ID, notification)
    }

    fun dismissMissedCallNotification() {
        cancel(MISSED_CALLS_NOTIF_ID)
    }

    fun displayCallNotification(call: Call, useAsForeground: Boolean = false) {
        val notifiable = getNotifiableForCall(call)

        val contact: Contact? = coreContext.contactsManager.findContactByAddress(call.remoteAddress)
        val pictureUri = contact?.getContactThumbnailPictureUri()
        val roundPicture = ImageUtils.getRoundBitmapFromUri(context, pictureUri)
        val displayName = contact?.fullName ?: LinphoneUtils.getDisplayName(call.remoteAddress)

        val stringResourceId: Int
        val iconResourceId: Int
        val callActivity: Class<*>
        when (call.state) {
            Call.State.Paused, Call.State.Pausing, Call.State.PausedByRemote -> {
                callActivity = CallActivity::class.java
                stringResourceId = R.string.call_notification_paused
                iconResourceId = R.drawable.topbar_call_notification
            }
            Call.State.OutgoingRinging, Call.State.OutgoingProgress, Call.State.OutgoingInit, Call.State.OutgoingEarlyMedia -> {
                callActivity = OutgoingCallActivity::class.java
                stringResourceId = R.string.call_notification_outgoing
                iconResourceId = R.drawable.topbar_call_notification
            }
            else -> {
                callActivity = CallActivity::class.java
                stringResourceId = R.string.call_notification_active
                iconResourceId = if (call.currentParams.videoEnabled()) {
                    R.drawable.topbar_videocall_notification
                } else {
                    R.drawable.topbar_call_notification
                }
            }
        }

        val callNotificationIntent = Intent(context, callActivity)
        callNotificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0, callNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(
            context, context.getString(R.string.notification_channel_service_id))
            .setContentTitle(contact?.fullName ?: displayName)
            .setContentText(context.getString(stringResourceId))
            .setSmallIcon(iconResourceId)
            .setLargeIcon(roundPicture)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context, R.color.notification_led_color))
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
            R.drawable.call_audio_start,
            context.getString(R.string.incoming_call_notification_answer_action_label),
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
            R.drawable.call_hangup,
            context.getString(R.string.incoming_call_notification_hangup_action_label),
            hangupPendingIntent
        ).build()
    }


     */


}
