package fi.tanskudaa.whatdoin.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

const val DATABASE_VERSION = 2

@Database(
    version = DATABASE_VERSION,
    entities = [Activity::class],
    autoMigrations = [
        AutoMigration(1, 2)
                     ],
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