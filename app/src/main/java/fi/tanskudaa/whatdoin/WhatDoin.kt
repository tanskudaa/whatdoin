package fi.tanskudaa.whatdoin

import android.app.Application
import fi.tanskudaa.whatdoin.data.ActivityDatabase
import fi.tanskudaa.whatdoin.data.ActivityRepository

class WhatDoin : Application() {
    lateinit var activityRepository: ActivityRepository

    override fun onCreate() {
        super.onCreate()
        activityRepository = ActivityRepository(ActivityDatabase.getDatabase(this).activityDao())
    }
}