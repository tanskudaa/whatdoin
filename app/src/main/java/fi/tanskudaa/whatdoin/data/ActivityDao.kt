package fi.tanskudaa.whatdoin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity ORDER BY id DESC LIMIT 1")
    suspend fun getLatestEntry(): Activity?

    @Query("SELECT * FROM activity WHERE id = :id")
    suspend fun getById(id: Long): Activity?

    @Query("SELECT * FROM activity ORDER BY id ASC")
    suspend fun getAll(): List<Activity>

    @Insert
    suspend fun insert(activity: Activity)

    @Update
    suspend fun update(activity: Activity)
}