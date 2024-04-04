package fi.tanskudaa.whatdoin.data

import android.os.Environment
import fi.tanskudaa.whatdoin.ui.getFormattedDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

suspend fun exportActivitiesToCSVFileInDownloads(activities: List<Activity>): Boolean {
    val exportTimeFormatted = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
    )

    withContext(Dispatchers.IO) {
        val target = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val f = File(target, "whatdoin_export_$exportTimeFormatted.csv")

        val writer = BufferedWriter(f.writer(charset = Charsets.UTF_8))
        writer.write("\"Started at\",Activity,Duration,\"Thoughts afterwards\"")
        writer.newLine()

        activities.forEachIndexed { index, activity ->
            if (index + 1 >= activities.size) {
                return@forEachIndexed
            }

            val durationFormatted = getFormattedDuration(
                (activities[index + 1].startTime - activity.startTime) / 1000
            )
            val startTimeFormatted = LocalDateTime.ofEpochSecond(
                activity.startTime / 1000, 0, OffsetDateTime.now().offset
            ).format(DateTimeFormatter.ISO_DATE_TIME)

            fun String.sanitizeForCSVWrite() = this.replace("\"", "\"\"")

            val sanitizedDescription = activity.description.sanitizeForCSVWrite()
            val sanitizedPostscript = activity.postscript.sanitizeForCSVWrite()

            writer.write("$startTimeFormatted,\"$sanitizedDescription\",\"$durationFormatted\",\"$sanitizedPostscript\"")
            writer.newLine()
        }

        writer.flush()
    }

    return true
}