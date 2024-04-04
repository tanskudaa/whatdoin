package fi.tanskudaa.whatdoin.ui

import android.os.Environment
import androidx.lifecycle.ViewModel
import fi.tanskudaa.whatdoin.data.Activity
import fi.tanskudaa.whatdoin.data.ActivityRepository
import fi.tanskudaa.whatdoin.data.exportActivitiesToCSVFileInDownloads
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

    fun isDatabaseEmpty() = _currentActivityDatabaseId == null

    fun getOffsetAvailability(): (OffsetMinutes) -> Boolean =
        { offset -> (System.currentTimeMillis() - _currentActivityStartedAtMillis)/60_000L > offset.value }

    suspend fun exportAllToCSVFile(): Boolean =
        exportActivitiesToCSVFileInDownloads(activityRepository.getAllActivities())

    fun updateDurationAndUiState() {
        val deltaMillis = System.currentTimeMillis() - _currentActivityStartedAtMillis
        _uiState.update {
            it.copy(
                formattedActivityDuration = getFormattedDuration(deltaMillis/1000)
            )
        }
    }

    suspend fun updateCurrentActivityDescription(newDescription: String) {
        if (_currentActivityDatabaseId == null) {
            // no activity exists in db to update
            return
        }

        _uiState.update {
            it.copy(
                currentActivityDescription = newDescription
            )
        }
        activityRepository.changeCurrentActivityDescription(
            _currentActivityDatabaseId!!,
            newDescription
        )
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

        _currentActivityDatabaseId = activityRepository.addAsNewCurrentActivity(
            Activity(description = newDescription, startTime = recordedTimeMilllis)
        )
    }

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
