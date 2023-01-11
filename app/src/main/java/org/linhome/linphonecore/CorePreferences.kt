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
package org.linhome.linphonecore

import android.content.Context
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.compatibility.Compatibility
import org.linhome.entities.Action
import org.linphone.core.Config
import org.linphone.mediastream.Log
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import org.linhome.linphonecore.extensions.getString

class CorePreferences constructor(private val context: Context) {
    private var _config: Config? = null
    var config: Config
        get() = _config ?: coreContext.core.config
        set(value) {
            _config = value
        }


    /* Linhome */
    var showLatestSnapshot: Boolean
        get() {
            return config.getBool("devices", "latest_snapshot", true)
        }
        set(value) {
            config.setBool("devices", "latest_snapshot", value)
        }


    // Default actions to be displayed when no device is associated with the call. Taken from configuration.
    //[default_actions]
    //default_actions_method_type=[method_dtmf_sip_info|method_dtmf_rfc_4733|method_sip_message]
    //action_1_type=[action_open_door|action_open_gate|action_lightup|action_unlock] (match name in shared theme action_types.xml)
    //action_1_code=yyyy (dtmf code)
    //action_2... up to 3 actions

    val defaultActionsMethodType: String?
        get() = config.getString("default_actions", "method_type", null)

    val defaultAction1: Action?
        get() = config.getString("default_actions", "action_1_type", null)?.let { type ->
            config.getString("default_actions", "action_1_code", null)?.let { code ->
                Action(type,code)
            } ?: null
        } ?:null

    val defaultAction2: Action?
        get() = config.getString("default_actions", "action_2_type", null)?.let { type ->
            config.getString("default_actions", "action_2_code", null)?.let { code ->
                Action(type,code)
            } ?: null
        } ?:null

    val defaultAction3: Action?
        get() = config.getString("default_actions", "action_3_type", null)?.let { type ->
            config.getString("default_actions", "action_3_code", null)?.let { code ->
                Action(type,code)
            } ?: null
        } ?:null


    // Todo - review necessary portion (copied from Linphone)
    /* App settings */

    var debugLogs: Boolean
        get() = config.getBool("app", "debug", true)
        set(value) {
            config.setBool("app", "debug", value)
        }

    var autoStart: Boolean
        get() = config.getBool("app", "auto_start", true)
        set(value) {
            config.setBool("app", "auto_start", value)
        }

    var keepServiceAlive: Boolean
        get() = config.getBool("app", "keep_service_alive", false)
        set(value) {
            config.setBool("app", "keep_service_alive", value)
        }


    /* Audio */

    val availableAudioCodecs = arrayOf("pcmu","pcma","opus","g729")
    val enabledVideoCodecsByDefault = arrayOf("h264")
    val enabledAudioCodecsByDefault = arrayOf("pcmu","pcma","opus")

    /* Video */

    var deviceName: String
        get() = config.getString("app", "device_name", nonNullDefault = Compatibility.getDeviceName(context))
        set(value) = config.setString("app", "device_name", value)



    /* Call */

    var vibrateWhileIncomingCall: Boolean
        get() = config.getBool("app", "incoming_call_vibration", true)
        set(value) {
            config.setBool("app", "incoming_call_vibration", value)
        }

    var autoAnswerEnabled: Boolean
        get() = config.getBool("app", "auto_answer", false)
        set(value) {
            config.setBool("app", "auto_answer", value)
        }

    var autoAnswerDelay: Int
        get() = config.getInt("app", "auto_answer_delay", 0)
        set(value) {
            config.setInt("app", "auto_answer_delay", value)
        }

    var showCallOverlay: Boolean
        get() = config.getBool("app", "call_overlay", false)
        set(value) {
            config.setBool("app", "call_overlay", value)
        }

    var xmlRpcServerUrl: String?
        get() = config.getString("assistant", "xmlrpc_url", null)
        set(value) {
            config.setString("assistant", "xmlrpc_url", value)
        }

    var passwordAlgo: String?
        get() = config.getString("assistant", "password_algo", null)
        set(value) {
            config.setString("assistant", "password_algo", value)
        }

    var loginDomain: String?
        get() = config.getString("assistant", "domain", null)
        set(value) {
            config.setString("assistant", "domain", value)
        }


    /* Assets stuff */

    val configPath: String
        get() = context.filesDir.absolutePath + "/.linphonerc"

    val factoryConfigPath: String
        get() = context.filesDir.absolutePath + "/linphonerc"


    val linhomeAccountDefaultValuesPath: String
        get() = context.filesDir.absolutePath + "/assistant_linhome_account_default_values"

    val sipAccountDefaultValuesPath: String
        get() = context.filesDir.absolutePath + "/assistant_sip_account_default_values"

    val ringtonePath: String
        get() = context.filesDir.absolutePath + "/share/sounds/linphone/rings/notes_of_the_optimistic.mkv"


    fun copyAssetsFromPackage() {
        copy("linphonerc_default", configPath)
        copy("linphonerc_factory", factoryConfigPath, true)
        copy("assistant_linhome_account_default_values", linhomeAccountDefaultValuesPath, true)
        copy("assistant_sip_account_default_values", sipAccountDefaultValuesPath, true)
        copy("nowebcamCIF.jpg", context.filesDir.absolutePath+"/nowebcamCIF.jpg", true)
        copy("bell.wav", context.filesDir.absolutePath+"/bell.wav", true)


    }

    fun getString(resource: Int): String {
        return context.getString(resource)
    }

    private fun copy(from: String, to: String, overrideIfExists: Boolean = false) {
        val outFile = File(to)
        if (outFile.exists()) {
            if (!overrideIfExists) {
                Log.i("[Preferences] File $to already exists")
                return
            }
        }
        Log.i("[Preferences] Overriding $to by $from asset")

        val outStream = FileOutputStream(outFile)
        val inFile = context.assets.open(from)
        val buffer = ByteArray(1024)
        var length: Int = inFile.read(buffer)

        while (length > 0) {
            outStream.write(buffer, 0, length)
            length = inFile.read(buffer)
        }

        inFile.close()
        outStream.flush()
        outStream.close()
    }

    fun encryptedPass(user: String, clearPass: String): String {
        val md = MessageDigest.getInstance(passwordAlgo?.toUpperCase())
        return BigInteger(
            1,
            md.digest(("${user}:${loginDomain}:${clearPass}").toByteArray())
        ).toString(16).padStart(32, '0')
    }
}
