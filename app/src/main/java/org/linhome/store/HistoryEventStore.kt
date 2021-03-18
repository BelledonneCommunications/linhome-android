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

package org.linhome.store

import org.linhome.entities.HistoryEvent
import org.linhome.store.StorageManager.historyEventsXml
import org.linphone.core.Config
import org.linphone.core.Factory


object HistoryEventStore {

    private var historyEventsConfig: Config

    var historyEvents: HashMap<String, HistoryEvent> // CallId / History event

    init {
        if (!StorageManager.historyEventsXml.exists())
            StorageManager.historyEventsXml.createNewFile()
        historyEventsConfig = Factory.instance().createConfig(null)
        historyEventsConfig.loadFromXmlFile(StorageManager.historyEventsXml.absolutePath)
        historyEvents = readFromXml()
    }

    fun readFromXml(): HashMap<String, HistoryEvent> {
        val result = HashMap<String, HistoryEvent>()
        historyEventsConfig.sectionsNamesList.forEach {
            result.put(
                historyEventsConfig.getString(it, "call_id", null)!!, HistoryEvent(
                    it,
                    historyEventsConfig.getString(it, "call_id", null),
                    historyEventsConfig.getBool(it, "viewed_by_user", false),
                    historyEventsConfig.getString(it, "media_file_name", null)!!,
                    historyEventsConfig.getString(it, "media_thumbnail_file_name", null)!!,
                    historyEventsConfig.getBool(it, "has_video", false)
                )
            )
        }
        return result
    }

    fun sync() {
        historyEventsConfig.sectionsNamesList.forEach {
            historyEventsConfig.cleanSection(it)
        }
        historyEvents.forEach { entry ->
            historyEventsConfig.setBool(entry.value.id, "viewed_by_user", entry.value.viewedByUser)
            historyEventsConfig.setString(
                entry.value.id,
                "media_file_name",
                entry.value.mediaFileName
            )
            historyEventsConfig.setString(
                entry.value.id,
                "media_thumbnail_file_name",
                entry.value.mediaThumbnailFileName
            )
            historyEventsConfig.setString(entry.value.id, "call_id", entry.value.callId)
            historyEventsConfig.setBool(entry.value.id, "has_video", entry.value.hasVideo)
        }
        historyEventsXml.writeText(historyEventsConfig.dumpAsXml())
    }

    fun persistHistoryEvent(entry: HistoryEvent) {
        entry.callId?.let {
            historyEvents.put(it, entry)
            sync()
        }
    }

    fun removeHistoryEvent(entry: HistoryEvent) {
        entry.media.also {
            if (it.exists())
                it.delete()
        }
        entry.mediaThumbnail.also {
            if (it.exists())
                it.delete()
        }
        historyEvents.remove(entry.callId)
        sync()
    }

    fun removeHistoryEventByCallId(callId: String) {
        findHistoryEventByCallId(callId)?.also {
            removeHistoryEvent(it)
        }
    }

    fun findHistoryEventByCallId(callId: String): HistoryEvent? {
        return historyEvents.get(callId)
    }

    fun markAsRead(historyEventId: String) {
        historyEvents.filter { it.value.id ==  historyEventId }.forEach { event ->
            event.value.viewedByUser = true
            persistHistoryEvent(event.value)
        }
    }

}