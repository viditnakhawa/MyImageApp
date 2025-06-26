package com.viditnakhawa.myimageapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viditnakhawa.myimageapp.data.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val hasCompletedOnboarding: StateFlow<Boolean> = dataStoreRepository.readOnboardingStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = false // Assume not completed until DataStore confirms
        )

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            dataStoreRepository.saveOnboardingStatus(true)
        }
    }
}