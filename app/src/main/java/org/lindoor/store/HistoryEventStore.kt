package org.lindoor.store

import org.lindoor.entities.HistoryEvent
import org.lindoor.store.StorageManager.historyEventsXml
import org.lindoor.utils.cdlog
import org.linphone.core.Config
import org.linphone.core.Factory

object HistoryEventStore {

    private var historyEventsConfig: Config

    var historyEvents:HashMap<String,HistoryEvent> // CallId / History event

    init {
        if (!StorageManager.historyEventsXml.exists())
            StorageManager.historyEventsXml.createNewFile()
        historyEventsConfig = Factory.instance().createConfig(null)
        historyEventsConfig.loadFromXmlFile(StorageManager.historyEventsXml.absolutePath)
        historyEvents = readFromXml()
    }

    fun readFromXml():HashMap<String,HistoryEvent> {
        val result= HashMap<String,HistoryEvent>()
        historyEventsConfig.sectionsNamesList.forEach {
            result.put(historyEventsConfig.getString(it,"call_id",null),HistoryEvent(
                it,
                historyEventsConfig.getString(it,"call_id",null),
                historyEventsConfig.getBool(it,"viewed_by_user",false),
                historyEventsConfig.getString(it,"media_file_name",null),
                historyEventsConfig.getString(it,"media_thumbnail_file_name","missing")))
        }
        return result
    }

    fun sync() {
        historyEventsConfig.sectionsNamesList.forEach {
            historyEventsConfig.cleanSection(it)
        }
        historyEvents.forEach { entry ->
            historyEventsConfig.setBool(entry.value.id,"viewed_by_user",entry.value.viewedByUser)
            historyEventsConfig.setString(entry.value.id,"media_file_name",entry.value.mediaFileName)
            historyEventsConfig.setString(entry.value.id,"media_thumbnail_file_name",entry.value.mediaThumbnailFileName)
            historyEventsConfig.setString(entry.value.id,"call_id",entry.value.callId)
        }
        historyEventsXml.writeText(historyEventsConfig.dumpAsXml())
    }

    fun persistHistoryEvent(entry:HistoryEvent) {
        entry.callId?.let {
            historyEvents.put(it,entry)
            sync()
        }
    }

    fun removeHistoryEvent(entry:HistoryEvent) {
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

    fun removeHistoryEventByCallId(callId:String) {
        findHistoryEventByCallId(callId)?.also {
            removeHistoryEvent(it)
        }
    }

    fun findHistoryEventByCallId(callId:String):HistoryEvent? {
        return historyEvents.get(callId)
    }

    fun markAllAsRead(){
        historyEvents.forEach{
            it.value.viewedByUser = true
        }
        sync()
    }

}