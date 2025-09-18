package com.practicum.playlistmaker.domain.impl

import android.content.Context
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SettingsInteractor

class SettingsInteractorImpl(private val context: Context) : SettingsInteractor {
    override fun isDarkTheme(): Boolean = App.isDarkTheme()
    override fun setDarkTheme(enabled: Boolean) = App.setDarkTheme(enabled)
    override fun getShareMessage(): String = context.getString(R.string.share_message)
    override fun getSupportEmail(): String = context.getString(R.string.support_email)
    override fun getEmailSubject(): String = context.getString(R.string.email_subject)
    override fun getEmailBody(): String = context.getString(R.string.email_body)
    override fun getUserAgreementUrl(): String = context.getString(R.string.user_agreement_url)
    override fun getEmailChooserTitle(): String = context.getString(R.string.email_chooser_title)
    override fun getEmailClientNotFoundMessage(): String = "На устройстве не найден почтовый клиент"
    override fun getBrowserNotFoundMessage(): String = "На устройстве не найден браузер"
}