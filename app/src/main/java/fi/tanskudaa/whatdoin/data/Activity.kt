package fi.tanskudaa.whatdoin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val startTime: Long, // unix time
)