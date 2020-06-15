package org.lindoor.utils

import org.lindoor.customisation.Texts
import java.text.SimpleDateFormat
import java.util.*


object DateUtil {
    val historyTimePattern = "yyyy-MM-dd"

    fun todayYesterdayRealDay(epochTimeDayUnit: Long): String {
        val simpleDateFormat = SimpleDateFormat(historyTimePattern, Locale.getDefault())
        if (epochTimeDayUnit == Date().time / 86400000) {
            return Texts.get("today")
        } else if (epochTimeDayUnit == Date().time / 86400000 - 1) {
            return Texts.get("yesterday")
        } else
            return simpleDateFormat.format(Date(epochTimeDayUnit * 86400000))
    }
}

