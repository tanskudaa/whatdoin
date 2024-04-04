package fi.tanskudaa.whatdoin.ui

fun getFormattedDuration(durationSeconds: Long): String {
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds / 60) % 60
    val seconds = durationSeconds % 60

    var result = ""
    if (hours > 0) result += "${hours}h"
    if (minutes > 0) result+= " ${minutes}m"
    result += " ${seconds}s"

    return result.trim()
}

