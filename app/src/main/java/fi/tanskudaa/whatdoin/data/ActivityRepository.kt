package fi.tanskudaa.whatdoin.data

class ActivityRepository(private val activityDao: ActivityDao) {
    suspend fun getCurrentActivity(): Activity? =
        activityDao.getLatestEntry()

    suspend fun getAllActivities(): List<Activity> =
        activityDao.getAll()

    suspend fun addAsNewCurrentActivity(activity: Activity) =
        activityDao.insert(activity)

    suspend fun changeCurrentActivityDescription(newDescription: String) {
        val existingActivity = activityDao.getLatestEntry()

        if (existingActivity == null) {
            return
        }
        /* else */
        activityDao.update(existingActivity.copy(
            description = newDescription
        ))
    }
}