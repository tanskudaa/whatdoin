package fi.tanskudaa.whatdoin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

const val DATABASE_VERSION = 1

@Database(
    entities = [Activity::class],
    version = DATABASE_VERSION,
    exportSchema = true
)
abstract class ActivityDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var Instance: ActivityDatabase? = null

        fun getDatabase(context: Context): ActivityDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ActivityDatabase::class.java, "app_db")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}