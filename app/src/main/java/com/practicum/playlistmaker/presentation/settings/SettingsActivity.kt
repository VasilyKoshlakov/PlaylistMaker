package com.practicum.playlistmaker.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.AppCreator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SettingsInteractor

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsInteractor = AppCreator.provideSettingsInteractor()

        val settingsBackButton = findViewById<ImageView>(R.id.button_back_settings)
        val shareAppButton = findViewById<ImageView>(R.id.share_app_button)
        val writeToSupportButton = findViewById<ImageView>(R.id.write_to_support_button)
        val userAgreementButton = findViewById<ImageView>(R.id.user_agreement_button)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)
        themeSwitcher.isChecked = settingsInteractor.isDarkTheme()

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            settingsInteractor.setDarkTheme(isChecked)
        }

        settingsBackButton.setOnClickListener {
            finish()
        }

        shareAppButton.setOnClickListener {
            shareApp()
        }
        writeToSupportButton.setOnClickListener {
            writeToSupport()
        }

        userAgreementButton.setOnClickListener {
            openUserAgreement()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, settingsInteractor.getShareMessage())
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun writeToSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(settingsInteractor.getSupportEmail()))
            putExtra(Intent.EXTRA_SUBJECT, settingsInteractor.getEmailSubject())
            putExtra(Intent.EXTRA_TEXT, settingsInteractor.getEmailBody())
        }

        try {
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    settingsInteractor.getEmailChooserTitle()
                )
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, settingsInteractor.getEmailClientNotFoundMessage(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openUserAgreement() {
        val agreementUrl = settingsInteractor.getUserAgreementUrl()
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementUrl))

        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, settingsInteractor.getBrowserNotFoundMessage(), Toast.LENGTH_SHORT).show()
        }
    }
}
