package fi.tanskudaa.whatdoin.data

class ActivityRepository(private val activityDao: ActivityDao) {
    suspend fun getCurrentActivity(): Activity? =
        activityDao.getLatestEntry()

    suspend fun getAllActivities(): List<Activity> =
        activityDao.getAll()

    /**
     * Returns inserted activity id
     */
    suspend fun addAsNewCurrentActivity(activity: Activity): Long =
        activityDao.insert(activity)

    suspend fun changeCurrentActivityDescription(id: Long, newDescription: String) {
        val existingActivity = activityDao.getById(id)

        if (existingActivity == null) {
            // this method call failing has potential for inivisble data loss, so it is better to crash than to
            // disregard. actual in-use failure shouldn't be possible.
            throw IllegalArgumentException()
        }
        /* else */
        activityDao.update(existingActivity.copy(
            description = newDescription
        ))
    }

    suspend fun updateActivityPostscript(id: Long, newPostscript: String) {
        val existingActivity = activityDao.getById(id)

        if (existingActivity == null) {
            // this method call failing has potential for inivisble data loss, so it is better to crash than to
            // disregard. actual in-use failure shouldn't be possible.
            throw IllegalArgumentException()
        }
        /* else */
        activityDao.update(existingActivity.copy(
            postscript = newPostscript
        ))
    }
}