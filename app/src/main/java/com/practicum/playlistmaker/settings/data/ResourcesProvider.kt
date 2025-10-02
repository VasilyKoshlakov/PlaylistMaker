package com.practicum.playlistmaker.settings.data

import android.content.Context
import com.practicum.playlistmaker.R

class ResourcesProvider(private val context: Context) {
    fun getShareMessage(): String = context.getString(R.string.share_message)
    fun getSupportEmail(): String = context.getString(R.string.support_email)
    fun getEmailSubject(): String = context.getString(R.string.email_subject)
    fun getEmailBody(): String = context.getString(R.string.email_body)
    fun getUserAgreementUrl(): String = context.getString(R.string.user_agreement_url)
    fun getEmailChooserTitle(): String = context.getString(R.string.email_chooser_title)
    fun getEmailClientNotFoundMessage(): String = context.getString(R.string.not_found_email_client)
    fun getBrowserNotFoundMessage(): String = context.getString(R.string.not_found_browser)
}