package fi.tanskudaa.whatdoin.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fi.tanskudaa.whatdoin.WhatDoin

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { HomeViewModel(whatDoinApplication().activityRepository) }
    }
}

fun CreationExtras.whatDoinApplication(): WhatDoin =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WhatDoin)