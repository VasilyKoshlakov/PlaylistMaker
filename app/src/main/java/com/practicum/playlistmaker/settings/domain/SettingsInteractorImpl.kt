package com.practicum.playlistmaker.settings.domain

class SettingsInteractorImpl(
    private val darkThemeProvider: () -> Boolean,
    private val darkThemeSetter: (Boolean) -> Unit,
    private val shareMessageProvider: () -> String,
    private val supportEmailProvider: () -> String,
    private val emailSubjectProvider: () -> String,
    private val emailBodyProvider: () -> String,
    private val userAgreementUrlProvider: () -> String,
    private val emailChooserTitleProvider: () -> String,
    private val emailClientNotFoundMessageProvider: () -> String,
    private val browserNotFoundMessageProvider: () -> String
) : SettingsInteractor {

    override fun isDarkTheme(): Boolean = darkThemeProvider()

    override fun setDarkTheme(enabled: Boolean) = darkThemeSetter(enabled)

    override fun getShareMessage(): String = shareMessageProvider()
    override fun getSupportEmail(): String = supportEmailProvider()
    override fun getEmailSubject(): String = emailSubjectProvider()
    override fun getEmailBody(): String = emailBodyProvider()
    override fun getUserAgreementUrl(): String = userAgreementUrlProvider()
    override fun getEmailChooserTitle(): String = emailChooserTitleProvider()
    override fun getEmailClientNotFoundMessage(): String = emailClientNotFoundMessageProvider()
    override fun getBrowserNotFoundMessage(): String = browserNotFoundMessageProvider()
}