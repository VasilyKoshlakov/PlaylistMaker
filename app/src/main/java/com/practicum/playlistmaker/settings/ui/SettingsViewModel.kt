package com.practicum.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.settings.domain.SettingsInteractor

class SettingsViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    private val _settingsState = MutableLiveData<SettingsState>()
    val settingsState: LiveData<SettingsState> = _settingsState

    init {
        _settingsState.value = SettingsState(
            isDarkTheme = settingsInteractor.isDarkTheme()
        )
    }

    fun setDarkTheme(enabled: Boolean) {
        settingsInteractor.setDarkTheme(enabled)
        _settingsState.value = _settingsState.value?.copy(isDarkTheme = enabled)
    }

    fun getShareMessage(): String = settingsInteractor.getShareMessage()
    fun getSupportEmail(): String = settingsInteractor.getSupportEmail()
    fun getEmailSubject(): String = settingsInteractor.getEmailSubject()
    fun getEmailBody(): String = settingsInteractor.getEmailBody()
    fun getUserAgreementUrl(): String = settingsInteractor.getUserAgreementUrl()
    fun getEmailChooserTitle(): String = settingsInteractor.getEmailChooserTitle()
    fun getEmailClientNotFoundMessage(): String = settingsInteractor.getEmailClientNotFoundMessage()
    fun getBrowserNotFoundMessage(): String = settingsInteractor.getBrowserNotFoundMessage()
}
