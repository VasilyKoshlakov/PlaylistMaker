package com.practicum.playlistmaker.domain.api

interface SettingsInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
    fun getShareMessage(): String
    fun getSupportEmail(): String
    fun getEmailSubject(): String
    fun getEmailBody(): String
    fun getUserAgreementUrl(): String
    fun getEmailChooserTitle(): String
    fun getEmailClientNotFoundMessage(): String
    fun getBrowserNotFoundMessage(): String
}