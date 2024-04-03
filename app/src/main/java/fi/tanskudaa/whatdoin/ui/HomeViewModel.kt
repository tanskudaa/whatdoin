package fi.tanskudaa.whatdoin.ui

import android.os.Environment
import androidx.lifecycle.ViewModel
import fi.tanskudaa.whatdoin.data.Activity
import fi.tanskudaa.whatdoin.data.ActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

enum class OffsetMinutes(val value: Int) {
    ZERO(0),
    FIVE(5),
    TEN(10),
    FIFTEEN(15),
}

data class HomeUiState(
    val currentActivityDescription: String = "",
    val formattedActivityDuration: String = "",
)

class HomeViewModel(private val activityRepository: ActivityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var _currentActivityStartedAtMillis = 0L
    private var _currentActivityDatabaseId: Long? = null

    private fun getFormattedDuration(durationSeconds: Long): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds / 60) % 60
        val seconds = durationSeconds % 60

        var result = ""
        if (hours > 0) result += "${hours}h"
        if (minutes > 0) result+= " ${minutes}m"
        result += " ${seconds}s"

        return result.trim()
    }

    fun updateDurationAndUiState() {
        val deltaMillis = System.currentTimeMillis() - _currentActivityStartedAtMillis
        _uiState.update {
            it.copy(
                formattedActivityDuration = getFormattedDuration(deltaMillis/1000)
            )
        }
    }

    suspend fun exportAllToCSVFile(): Boolean {
        val allActivities = activityRepository.getAllActivities()

        val exportTimeFormatted = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        )

        withContext(Dispatchers.IO) {
            val target = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val f = File(target, "whatdoin_export_$exportTimeFormatted.csv")

            val writer = BufferedWriter(f.writer(charset = Charsets.UTF_8))
            writer.write("\"Started at\",Activity,Duration,\"Thoughts afterwards\"")
            writer.newLine()

            allActivities.forEachIndexed { index, activity ->
                if (index + 1 >= allActivities.size) {
                    return@forEachIndexed
                }

                val durationFormatted = getFormattedDuration(
                    (allActivities[index + 1].startTime - activity.startTime)/1000
                )
                val startTimeFormatted = LocalDateTime.ofEpochSecond(
                    activity.startTime/1000, 0, OffsetDateTime.now().offset
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

    suspend fun updatePostscriptAndSwitchToNewActivity(
        postscript: String,
        newDescription: String,
        offset: OffsetMinutes)
    {
        // update current activity's postscript, if exists
        if (_currentActivityDatabaseId != null) {
            activityRepository.updateActivityPostscript(
                _currentActivityDatabaseId!!,
                postscript
            )
        }

        val offsetMillis = offset.value * 60_000L
        val recordedTimeMilllis = System.currentTimeMillis() - offsetMillis

        _uiState.update {
            it.copy(
                currentActivityDescription = newDescription,
            )
        }
        _currentActivityStartedAtMillis = recordedTimeMilllis
        updateDurationAndUiState()

        activityRepository.addAsNewCurrentActivity(
            Activity(description = newDescription, startTime = recordedTimeMilllis)
        )
    }

    suspend fun updateCurrentActivityDescription(newDescription: String) {
        _uiState.update {
            it.copy(
                currentActivityDescription = newDescription
            )
        }
        activityRepository.changeCurrentActivityDescription(newDescription)
    }

    fun getOffsetAvailability(): (OffsetMinutes) -> Boolean =
        { offset -> (System.currentTimeMillis() - _currentActivityStartedAtMillis)/60_000L > offset.value }

    init {
        val latestActivityFromDb = runBlocking {
            activityRepository.getCurrentActivity()
        }

        if (latestActivityFromDb == null) { // first start/empty database
            /* _currentActivityDatabaseId = null */
            _currentActivityStartedAtMillis = System.currentTimeMillis()
            _uiState.update { it.copy(currentActivityDescription = "???") }
        } else {
            _currentActivityDatabaseId = latestActivityFromDb.id
            _currentActivityStartedAtMillis = latestActivityFromDb.startTime
            _uiState.update { it.copy(currentActivityDescription = latestActivityFromDb.description) }
        }

        updateDurationAndUiState()
    }
}
